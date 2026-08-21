"""
usage_report.py — LLM 工数集計スクリプト（jq 不使用・Python3）

使い方:
  python3 .claude/scripts/usage_report.py --case {案件キー} [--specs-dir specs/] [--repo-root .] [--out {path}]

動作:
  1. 案件リポ側 specs/{案件キー}/ ＋ 各スタックリポ側 {スタック}/specs/{案件キー}/
     （案件リポ配下にチェックアウトされているもの）を横断的に走査し、全 llm-usage.jsonl を収集する
     （SS/PG-Plan/PG/PT-Plan/PT 工程分はスタックリポ側に配置される）
  2. exchange_uuid で重複排除
  3. 工程別・手戻り別・Milestone 合計を集計
  4. Markdown テーブルを stdout または --out ファイルに出力

チェックアウトされていないスタックリポ・ブランチがある場合は、事前に
`git clone`/`git fetch` するか `git show feature/{案件キー}:{パス}` で
該当ファイルをローカルの対応パスに取得してから本スクリプトを実行する。

ブランチ → 工程の対応は PHASE_MAP に従う。
-sp- を含むブランチは手戻り（rework）として分離集計。
"""
import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path

PHASE_MAP = {
    "sa":     "SA",
    "ui":     "UI",
    "ssplan": "SS-Plan",
    "ss":     "SS",
    "pgplan": "PG-Plan",
    "pg":     "PG",
    "ptplan": "PT-Plan",
    "pt":     "PT",
}

# スタックリポの配置ディレクトリ（CLAUDE.md §3 スタック配置ルール準拠）。
# 案件リポ配下にチェックアウトされている場合、各スタックの specs/{案件キー}/ も走査対象にする。
STACK_DIRS = ["bs", "us-mpa", "us-api", "frontend", "batch"]


def find_usage_files(specs_dir: Path, case_key: str):
    """specs/{case_key}/ 配下の全 llm-usage.jsonl を返す（案件リポ側）。"""
    case_dir = specs_dir / case_key
    if not case_dir.exists():
        return []
    return list(case_dir.rglob("llm-usage.jsonl"))


def find_stack_usage_files(repo_root: Path, case_key: str):
    """{スタック}/specs/{case_key}/ 配下の全 llm-usage.jsonl を返す（スタックリポ側）。

    案件リポ配下にチェックアウトされているスタックリポ（bs/ 等）のみが対象。
    チェックアウトされていないスタックは呼び出し側で git clone/fetch や
    `git show` で取得してから再実行する。
    """
    files = []
    for stack in STACK_DIRS:
        case_dir = repo_root / stack / "specs" / case_key
        if not case_dir.exists():
            continue
        files.extend(case_dir.rglob("llm-usage.jsonl"))
    return files


def parse_phase_from_branch(branch: str, case_key: str):
    """ブランチ名から工程とsp IDを解析する。"""
    prefix = f"feature/{case_key}-"
    if not branch.startswith(prefix):
        return "unknown", None
    rest = branch[len(prefix):]

    # 手戻りブランチ: {phase}-{fid}-sp-{id} または {phase}-sp-{id}
    sp_idx = rest.find("-sp-")
    if sp_idx >= 0:
        sp_id = rest[sp_idx + 4:]  # "sp-001" の連番部分
        phase_part = rest[:sp_idx]
        # phase_part から phase トークンを取得
        for tok, label in PHASE_MAP.items():
            if phase_part == tok or phase_part.startswith(tok + "-"):
                return label, f"sp-{sp_id}"
        return "unknown", f"sp-{sp_id}"

    # 正準ブランチ: {phase} または {phase}-{fid}
    for tok, label in PHASE_MAP.items():
        if rest == tok or rest.startswith(tok + "-"):
            return label, None
    return "unknown", None


def load_and_deduplicate(files):
    """全ファイルを読み込み exchange_uuid で重複排除したレコードリストを返す。"""
    seen = set()
    records = []
    for f in files:
        try:
            with open(f, "r", encoding="utf-8") as fh:
                for line in fh:
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        rec = json.loads(line)
                    except Exception:
                        continue
                    uid = rec.get("exchange_uuid")
                    if uid and uid in seen:
                        continue
                    if uid:
                        seen.add(uid)
                    records.append(rec)
        except Exception:
            continue
    return records


def aggregate(records, case_key):
    """工程別・手戻り別に集計する。"""
    phase_totals = defaultdict(lambda: {"exchanges": 0, "input": 0, "output": 0, "total": 0, "duration_sec": 0.0})
    rework_totals = defaultdict(lambda: {"exchanges": 0, "input": 0, "output": 0, "total": 0, "duration_sec": 0.0})

    for rec in records:
        branch = rec.get("branch", "")
        phase, sp_id = parse_phase_from_branch(branch, case_key)
        bucket = phase_totals if sp_id is None else rework_totals
        key = phase if sp_id is None else sp_id

        bucket[key]["exchanges"] += 1
        bucket[key]["input"]    += rec.get("input_tokens", 0) or 0
        bucket[key]["output"]   += rec.get("output_tokens", 0) or 0
        bucket[key]["total"]    += rec.get("total_tokens", 0) or 0
        dur = rec.get("duration_sec")
        if dur is not None:
            bucket[key]["duration_sec"] += float(dur)

    return phase_totals, rework_totals


def fmt_num(n):
    return f"{n:,}"


def fmt_sec(s):
    return f"{int(s):,}"


def render_report(case_key, phase_totals, rework_totals):
    lines = [f"## LLM 工数サマリ（{case_key}）\n"]

    # 工程別集計
    order = ["SA", "UI", "SS-Plan", "SS", "PG-Plan", "PG", "PT-Plan", "PT", "unknown"]
    lines.append("### 工程別集計（正準工程）\n")
    lines.append("| 工程 | exchanges | input | output | total | duration_sec |")
    lines.append("|---|---|---|---|---|---|")
    phase_sum = {"exchanges": 0, "input": 0, "output": 0, "total": 0, "duration_sec": 0.0}
    for phase in order:
        if phase not in phase_totals:
            continue
        d = phase_totals[phase]
        lines.append(f"| {phase} | {fmt_num(d['exchanges'])} | {fmt_num(d['input'])} | {fmt_num(d['output'])} | {fmt_num(d['total'])} | {fmt_sec(d['duration_sec'])} |")
        for k in phase_sum:
            phase_sum[k] += d[k]
    lines.append(f"| **合計** | **{fmt_num(phase_sum['exchanges'])}** | **{fmt_num(phase_sum['input'])}** | **{fmt_num(phase_sum['output'])}** | **{fmt_num(phase_sum['total'])}** | **{fmt_sec(phase_sum['duration_sec'])}** |")

    lines.append("")

    # 手戻り別集計
    if rework_totals:
        lines.append("### 手戻り別集計\n")
        lines.append("| 手戻りID | exchanges | input | output | total | duration_sec |")
        lines.append("|---|---|---|---|---|---|")
        rework_sum = {"exchanges": 0, "input": 0, "output": 0, "total": 0, "duration_sec": 0.0}
        for sp_id, d in sorted(rework_totals.items()):
            lines.append(f"| {sp_id} | {fmt_num(d['exchanges'])} | {fmt_num(d['input'])} | {fmt_num(d['output'])} | {fmt_num(d['total'])} | {fmt_sec(d['duration_sec'])} |")
            for k in rework_sum:
                rework_sum[k] += d[k]
        lines.append(f"| **合計** | **{fmt_num(rework_sum['exchanges'])}** | **{fmt_num(rework_sum['input'])}** | **{fmt_num(rework_sum['output'])}** | **{fmt_num(rework_sum['total'])}** | **{fmt_sec(rework_sum['duration_sec'])}** |")
        lines.append("")
    else:
        rework_sum = {"exchanges": 0, "input": 0, "output": 0, "total": 0, "duration_sec": 0.0}

    # Milestone サマリ
    grand = {k: phase_sum[k] + rework_sum[k] for k in phase_sum}
    lines.append("### Milestone 合計\n")
    lines.append("| 区分 | exchanges | total_tokens | duration_sec |")
    lines.append("|---|---|---|---|")
    lines.append(f"| 正準工程 | {fmt_num(phase_sum['exchanges'])} | {fmt_num(phase_sum['total'])} | {fmt_sec(phase_sum['duration_sec'])} |")
    lines.append(f"| 手戻り   | {fmt_num(rework_sum['exchanges'])} | {fmt_num(rework_sum['total'])} | {fmt_sec(rework_sum['duration_sec'])} |")
    lines.append(f"| **総計** | **{fmt_num(grand['exchanges'])}** | **{fmt_num(grand['total'])}** | **{fmt_sec(grand['duration_sec'])}** |")

    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description="LLM 工数集計")
    parser.add_argument("--case", required=True, help="案件キー")
    parser.add_argument("--specs-dir", default="specs", help="案件リポ側 specs/ ディレクトリのパス（デフォルト: specs）")
    parser.add_argument("--repo-root", default=".", help="スタックリポ（bs/ 等）をチェックアウトしている案件リポのルート（デフォルト: 現在ディレクトリ）")
    parser.add_argument("--out", help="出力先ファイル（省略時は stdout）")
    args = parser.parse_args()

    specs_dir = Path(args.specs_dir)
    repo_root = Path(args.repo_root)
    files = find_usage_files(specs_dir, args.case) + find_stack_usage_files(repo_root, args.case)
    if not files:
        print(
            f"[usage_report] llm-usage.jsonl が見つかりません: {specs_dir / args.case} "
            f"（および {repo_root}/{{{','.join(STACK_DIRS)}}}/specs/{args.case}）。"
            f"チェックアウトされていないスタックリポがある場合は git clone/fetch するか "
            f"`git show feature/{args.case}:{{path}}` で取得してから再実行してください。",
            file=sys.stderr,
        )
        sys.exit(1)

    records = load_and_deduplicate(files)
    phase_totals, rework_totals = aggregate(records, args.case)
    report = render_report(args.case, phase_totals, rework_totals)

    if args.out:
        out_path = Path(args.out)
        out_path.parent.mkdir(parents=True, exist_ok=True)
        with open(out_path, "w", encoding="utf-8") as f:
            f.write(report)
        print(f"[usage_report] 出力: {args.out}", file=sys.stderr)
    else:
        print(report)


if __name__ == "__main__":
    main()
