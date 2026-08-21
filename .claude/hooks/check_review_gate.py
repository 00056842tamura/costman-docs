"""
工程間整合性レビュー実施ゲート — 補助的安全網（PreToolUse フック）

`gh pr create` または `.claude/scripts/gh/pr-open.sh` を含む Bash コマンド実行の直前に、
現在ブランチが SS（詳細設計）/ PG（実装）/ PT（単体テスト）の機能ID別ブランチであれば、
対応する整合性レビュー（工程別に下記「対象工程・判定対象」を参照）が実行された形跡として
無条件に生成されるはずの成果物が issue スコープ配下に存在するかを確認し、存在しなければ
列挙して sys.exit(2) でブロックします。

対象工程・判定対象（無条件に生成されるはずの成果物の存在確認のみ）:
  - SS : `{issue_id}_{機能ID}/review-report.md` ＋ `adr/ADR-SS-{n}*.md`（最低1件）
  - PG : `{issue_id}_{機能ID}/springer-review-report.md`（backend系機能ID）
         `{issue_id}_{機能ID}/reacter-code-review-report.md`（frontend系機能ID＝`front-`始まり）
         ADR は不要（PG工程の整合性チェック=`springer-review`Step2.6・`reacter-code-review`は
         ADR起票を要求しない）
  - PT : `{issue_id}_{機能ID}/consistency-report.md`（backend/frontend共通・ADR不要）

設計上の限界（意図的な範囲限定。誤解を防ぐため明記する）:
  - 判定できるのは「無条件に生成されるはずの成果物の存在」のみ。外部IF定義書のように
    生成条件が可変な成果物（外部連携の有無等）は、本フックがその要否を判断できないため
    対象外とする。
  - 各成果物の内容（findingが実際に解消されたか等の質的な正しさ）も対象外とする。
    「レビューが実施された形跡があること」のみを保証する簡易ゲートであり、
    レビューの質そのものを保証するものではない。

主たる強制力は各生成スキル（`/detailed-design-gen` 等）の完了報告と工程手順書にあります。
本フックは `check_backlog_gate.py` と同種の、コマンド文字列パターンマッチに
依存するベストエフォートの仕組みです（回避可能）。
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
# 注: check_backlog_gate.py・log_work.py と同等のロジック。共有ライブラリ化は見送り、
#     本フック内に複製している（個別実装で十分というユーザー判断）。
BRANCH_PATTERN = re.compile(
    r"^feature/(?P<case>.+?)-(?P<phase>ssplan|pgplan|ptplan|sa|ui|ss|pg|pt)"
    r"(?:-(?P<fid>(?:bs|batch|us-intra|us-inter|front-intra|front-inter)-\d{3}))?$"
)

# 本フックが対象とする工程（機能ID別ブランチを持つ実施工程のみ。計画工程は対象外）
TARGET_PHASES = {"ss", "pg", "pt"}
PHASE_SPECS_DIR_NAME = {
    "ss": "detail-design",
    "pg": "implementation",
    "pt": "unit-test",
}
REVIEW_SKILL_HINT = {
    "ss": "対応する /detailed-design-review-backend・-frontend・-batch のいずれか",
    "pg": "対応する /springer-review（backend系機能ID）または /reacter-code-review（frontend系機能ID）",
    "pt": "/consistency-check",
}


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

    .claude/hooks/check_review_gate.py → .claude/hooks → .claude → product-root
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

    `check_backlog_gate.py`・`log_work.py` と同一ロジック。
    product_root 配下にネストしたスタックリポ・product_root 外部の git worktree の
    いずれも同一ロジックで解決できる。
    """
    try:
        cwd = Path.cwd().resolve()
    except OSError:
        return product_root
    for candidate in (cwd, *cwd.parents):
        if (candidate / ".git").exists():
            return candidate
    return product_root


def review_artifact_filename(phase, fid):
    """工程・機能IDカテゴリから、無条件に生成されるはずのレビュー成果物ファイル名を返す。"""
    if phase == "ss":
        return "review-report.md"
    if phase == "pg":
        is_frontend = fid.startswith("front-")
        return "reacter-code-review-report.md" if is_frontend else "springer-review-report.md"
    if phase == "pt":
        return "consistency-report.md"
    return None


def missing_artifacts(local_case_dir, phase, fid):
    """issue スコープ配下（*_{fid}/）に、無条件生成されるはずの成果物が
    揃っているかを確認し、欠けているものの説明を返す（空リスト = 全て揃っている）。
    """
    specs_dir_name = PHASE_SPECS_DIR_NAME[phase]
    phase_root = local_case_dir / specs_dir_name
    if not phase_root.is_dir():
        return [f"{phase.upper()} issue ディレクトリが見つかりません: " + str(phase_root)]

    issue_dir = None
    for d in phase_root.iterdir():
        if d.is_dir() and d.name.endswith(f"_{fid}"):
            issue_dir = d
            break
    if issue_dir is None:
        return [f"機能ID {fid} の {phase.upper()} issue ディレクトリが見つかりません: {phase_root}"]

    missing = []
    artifact_name = review_artifact_filename(phase, fid)
    if not (issue_dir / artifact_name).is_file():
        missing.append(str(issue_dir / artifact_name) + "（レビューレポートが存在しません）")

    if phase == "ss":
        adr_dir = issue_dir / "adr"
        adr_ss_files = sorted(adr_dir.glob("ADR-SS-*.md")) if adr_dir.is_dir() else []
        if not adr_ss_files:
            missing.append(str(adr_dir / "ADR-SS-*.md") + "（ADR-SS-{n} が1件も存在しません）")

    return missing


def main():
    try:
        hook_data = read_hook_input()
        command = (
            hook_data.get("tool_input", {}).get("command")
            or hook_data.get("command", "")
        )
        if not command:
            return

        if not TARGET_COMMAND_PATTERN.search(command):
            return
        if DRY_RUN_PATTERN.search(command):
            return

        product_root = find_product_root()
        owning_repo_root = find_owning_repo_root(product_root)
        branch = get_git_branch(owning_repo_root)

        m = BRANCH_PATTERN.match(branch)
        if not m:
            return  # 新ワークフロー形式のブランチでなければスコープ解決不能のため素通り（fail open）

        phase = m.group("phase")
        fid = m.group("fid")
        if phase not in TARGET_PHASES or not fid:
            return  # 対象工程（SS/PG/PT・機能ID別ブランチ）以外は対象外

        case_key = m.group("case")
        local_case_dir = owning_repo_root / "specs" / case_key
        if not local_case_dir.is_dir():
            return

        missing = missing_artifacts(local_case_dir, phase, fid)
        if missing:
            print(
                "[工程間整合性レビューゲート] 整合性レビュー実施の形跡（無条件生成される"
                f"成果物）が確認できないため gh pr create をブロックしました。"
                f"{REVIEW_SKILL_HINT[phase]}を実行してください:",
                file=sys.stderr,
            )
            for item in missing:
                print(f"  {item}", file=sys.stderr)
            print(
                "本ゲートは「レビューが実施された形跡があること」のみを確認する簡易ゲートで"
                "あり、レビューの質そのものは保証しません。外部IF定義書のように生成条件が"
                "可変な成果物は判定対象外です。",
                file=sys.stderr,
            )
            sys.exit(2)

    except Exception:
        # fail-safe: 本フックの不具合が正規の gh pr create を止めてしまう事故を避ける
        pass


if __name__ == "__main__":
    main()
