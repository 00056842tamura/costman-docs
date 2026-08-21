"""
工程内バックログ解消ゲート — 補助的安全網（PreToolUse フック）

`gh pr create` または `.claude/scripts/gh/pr-open.sh` を含む Bash コマンド実行の直前に、
現在ブランチから工程issueスコープ（discussion-log.md・adr/ ディレクトリ）を解決し、
未解決のバックログ項目（書式1のみ。書式3〔承認済み持ち越し〕は対象外）が残っていれば
列挙して sys.exit(2) でブロックします。

主たる強制力は `docs-to-pr` オーケストレーターの Step 0（対話ゲート）にあります。
本フックは「①Step 0 の実行忘れの見落とし防止」「②サブエージェント等が Step 0 を経ずに
直接 gh pr create を実行してしまうケースの防止」を目的とした補助的な安全網であり、
コマンド文字列パターンマッチに依存するベストエフォートの仕組みです（回避可能）。

重要: PreToolUse フックで実際にツール呼び出しをブロックできるのは exit code 2 のみ
（exit 1 は非ブロッキングエラーとして処理が継続される）。本フックは必ず sys.exit(2) を使う。

対象外のコマンド（`gh pr create`/`pr-open.sh` を含まない全ての Bash 呼び出し）では
即座に何もせず終了し、他の Bash 実行（`npm test` 等）への影響を最小化します。
例外発生時は fail-safe（try/except: pass）で素通りさせます（本フックの不具合が
正規の `gh pr create` を止めてしまう事故を避けるための設計。ただしこれは
「hook が確実に機能する保証にはならない」ことの裏返しでもある）。
"""
import os
import re
import sys
import json
import subprocess
from pathlib import Path


# --- 早期リターン用パターン ---
TARGET_COMMAND_PATTERN = re.compile(r"gh\s+pr\s+create|pr-open\.sh")
DRY_RUN_PATTERN = re.compile(r"\bDRY_RUN=1\b")

# feature/{案件キー}-{工程}[-{機能ID}]（案件キー自体がハイフンを含むため工程トークンで切る）
# 機能ID は {カテゴリ}-{3桁連番}（カテゴリ: bs/batch/us-intra/us-inter/front-intra/front-inter）
# 注: log_work.py の BRANCH_PATTERN と同等のロジック。共有ライブラリ化は見送り、
#     本フック内に複製している（既存 log_work.py への回帰リスクを避けるため）。
BRANCH_PATTERN = re.compile(
    r"^feature/(?P<case>.+?)-(?P<phase>ssplan|pgplan|ptplan|sa|ui|ss|pg|pt)"
    r"(?:-(?P<fid>(?:bs|batch|us-intra|us-inter|front-intra|front-inter)-\d{3}))?$"
)

# 工程トークン → (specs ディレクトリ名, area別か, ADR ラベル)
PHASE_MAP = {
    "sa":     ("requirements",        False, "SA"),
    "ui":     ("base-design",         False, "UI"),
    "ssplan": ("detail-design-plan",  False, "SS-Plan"),
    "ss":     ("detail-design",       True,  "SS"),
    "pgplan": ("implementation-plan", False, "PG-Plan"),
    "pg":     ("implementation",      True,  "PG"),
    "ptplan": ("unit-test-plan",      False, "PT-Plan"),
    "pt":     ("unit-test",           True,  "PT"),
}

# SS-Plan/PG-Plan/PT-Plan は ADR を起票しないため Step 0 の ADR チェック対象外
# （discussion-log.md は対象のまま。docs-to-pr.md Step 0 の「対象」定義と同一）
NO_ADR_PHASES = {"ssplan", "pgplan", "ptplan"}

# 未解決項目（書式1）抽出パターン: `- [ ] ...`（書式2の `- [x]` は対象外）
UNRESOLVED_LINE_PATTERN = re.compile(r"^\s*-\s*\[ \]\s*(.+)$")
# 書式3（承認済み持ち越し）を示すマーカー文字列。書式1の抽出結果から除外するために使う。
CARRYOVER_MARKER = "承認済み持ち越し"


def read_hook_input():
    """フック入力 JSON を取得する。標準入力（公式仕様）→ 環境変数の順で試す。"""
    raw = ""
    try:
        raw = sys.stdin.read()
    except Exception:
        raw = ""
    if raw:
        try:
            return json.loads(raw)
        except Exception:
            pass
    env_json = os.environ.get("CLAUDE_TOOL_INPUT") or os.environ.get("CLAUDE_HOOK_INPUT")
    if env_json:
        try:
            return json.loads(env_json)
        except Exception:
            pass
    return {}


def find_product_root():
    """このスクリプトの位置からプロダクトルートを推定。

    .claude/hooks/check_backlog_gate.py → .claude/hooks → .claude → product-root
    """
    return Path(__file__).resolve().parent.parent.parent


def get_git_branch(repo_root=None):
    """現在の Git ブランチを取得。失敗時は空文字を返す（fail open）。"""
    try:
        result = subprocess.run(
            ["git", "branch", "--show-current"],
            capture_output=True, text=True, timeout=2,
            cwd=str(repo_root) if repo_root else None,
        )
        return result.stdout.strip()
    except Exception:
        return ""


def find_owning_repo_root(product_root):
    """現在の作業ディレクトリ（cwd）から最も近い `.git` 境界を遡って発見し、
    そのリポジトリルートを返す（見つからない場合は product_root）。

    `log_work.py` の `find_owning_repo_root` は編集対象ファイルパスから判定するが、
    本フックは Bash コマンドのみを受け取るため、代わりにプロセスの cwd を起点にする。
    ロジック自体（.git を遡って発見する部分）は log_work.py と同一だが、共有モジュール化は
    見送り本フック内に複製している。

    product_root 配下にネストしたスタックリポ（例: product_root/bs/）だけでなく、
    product_root 外部に作成された git worktree（例: C:/wt/bs2/。cwd がそこにある場合）も
    同一ロジックで解決できる（旧実装は STACK_DIRS 名ベースの判定のため後者を解決できなかった）。
    """
    try:
        cwd = Path.cwd().resolve()
    except OSError:
        return product_root
    for candidate in (cwd, *cwd.parents):
        if (candidate / ".git").exists():
            return candidate
    return product_root


def extract_section(text, header):
    """指定した `## {header}` 節の本文を、次の `## ` 見出しまたはファイル末尾までで切り出す。"""
    lines = text.splitlines()
    start = None
    for i, line in enumerate(lines):
        if line.strip() == header:
            start = i + 1
            break
    if start is None:
        return ""
    end = len(lines)
    for i in range(start, len(lines)):
        if lines[i].startswith("## "):
            end = i
            break
    return "\n".join(lines[start:end])


def extract_unresolved(section_text):
    """節本文から書式1（未解決・承認済み持ち越しでない）の行を全件抽出する。

    テンプレートの未記入プレースホルダー（`<!-- ... -->` のみの行）は対象外とする。
    """
    results = []
    for line in section_text.splitlines():
        m = UNRESOLVED_LINE_PATTERN.match(line)
        if not m:
            continue
        rest = m.group(1).strip()
        if not rest:
            continue
        if rest.startswith("<!--") and rest.endswith("-->"):
            continue  # 未記入プレースホルダー
        if CARRYOVER_MARKER in rest:
            continue  # 書式3（承認済み持ち越し）は対象外
        results.append(line.strip())
    return results


def collect_target_files(local_case_dir, phase, fid):
    """discussion-log.md・adr/ADR-{工程}-*.md の対象ファイル一覧を返す（issue スコープ配下）。

    戻り値は (file_path, is_adr) のタプルのリスト。
    """
    phase_dir_name, is_area, phase_label = PHASE_MAP[phase]
    phase_root = local_case_dir / phase_dir_name
    if not phase_root.is_dir():
        return []

    issue_dirs = []
    for d in phase_root.iterdir():
        if not d.is_dir():
            continue
        if is_area and fid and not d.name.endswith(f"_{fid}"):
            continue
        issue_dirs.append(d)

    files = []
    for issue_dir in issue_dirs:
        discussion_log = issue_dir / "discussion-log.md"
        if discussion_log.is_file():
            files.append((discussion_log, False))
        if phase not in NO_ADR_PHASES:
            adr_dir = issue_dir / "adr"
            if adr_dir.is_dir():
                for adr_file in sorted(adr_dir.glob(f"ADR-{phase_label}-*.md")):
                    files.append((adr_file, True))
    return files


def main():
    try:
        hook_data = read_hook_input()
        command = (
            hook_data.get("tool_input", {}).get("command")
            or hook_data.get("command", "")
        )
        if not command:
            return

        # 早期リターン: 対象コマンドでなければ即座に終了（他の全 Bash 呼び出しへの影響を最小化）
        if not TARGET_COMMAND_PATTERN.search(command):
            return
        # DRY_RUN=1（実際には PR を作成しないプレビュー実行）は対象外
        if DRY_RUN_PATTERN.search(command):
            return

        product_root = find_product_root()
        owning_repo_root = find_owning_repo_root(product_root)
        branch = get_git_branch(owning_repo_root)

        m = BRANCH_PATTERN.match(branch)
        if not m:
            return  # 新ワークフロー形式のブランチでなければスコープ解決不能のため素通り（fail open）

        case_key = m.group("case")
        phase = m.group("phase")
        fid = m.group("fid")

        local_case_dir = owning_repo_root / "specs" / case_key
        if not local_case_dir.is_dir():
            return

        target_files = collect_target_files(local_case_dir, phase, fid)
        if not target_files:
            return

        unresolved = []
        for file_path, is_adr in target_files:
            try:
                text = file_path.read_text(encoding="utf-8")
            except OSError:
                continue
            header = "## 未確定事項（バックログ）" if is_adr else "## 未解決事項・TODO"
            section = extract_section(text, header)
            for item in extract_unresolved(section):
                unresolved.append(f"  {file_path}: {item}")

        if unresolved:
            print(
                "[バックログ解消ゲート] 未解決のバックログ項目が残っているため gh pr create を"
                "ブロックしました。先に docs-to-pr Step 0 でユーザーと合意してください:",
                file=sys.stderr,
            )
            for item in unresolved:
                print(item, file=sys.stderr)
            print(
                "全項目が (a) 工程内で解消（- [x] ...（解消: ...））"
                " または (b) ユーザー承認済み持ち越し（- [ ] ...（承認済み持ち越し→...））"
                " のいずれかで記録されるまで PR を作成できません。",
                file=sys.stderr,
            )
            sys.exit(2)

    except Exception:
        # fail-safe: 本フックの不具合が正規の gh pr create を止めてしまう事故を避ける
        pass


if __name__ == "__main__":
    main()
