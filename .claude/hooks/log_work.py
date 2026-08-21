"""
作業ログ自動記録フック — PostToolUse (Write/Edit)

ファイルの作成・編集のたびに、以下へエントリを追記します:

  1. docs/changelog/work-logs/{YYYY-MM-DD}.md   ← 日別の全作業ログ（必ず記録）
  2. specs/{案件キー}/{工程dir}/.../work-log.md ← ブランチが新ワークフロー形式の場合
     （SS 以降は {スタック}/specs/{案件キー}/{工程dir}/.../work-log.md ＝スタックリポ自身の git 管理）

ブランチ命名（新ワークフロー）:
  feature/{案件キー}-{工程}[-{機能ID}]
  例: feature/inventory-2026-001-sa / feature/inventory-2026-001-ss-bs-001
  機能ID は {カテゴリ}-{3桁連番}（カテゴリ: bs/batch/us-intra/us-inter/front-intra/front-inter）

編集対象ファイルが `bs/`・`frontend/` 等の既知スタックディレクトリ配下（かつそのディレクトリが
独立した git リポジトリ）の場合、ブランチはそのスタックリポ自身の現在ブランチを見る
（案件リポの現在ブランチとは異なることがあるため。`find_owning_repo_root` で解決）。

issue-id（work-log の格納先）の解決:
  - 全体ゲート工程(sa/ui/ssplan):
      specs/{案件キー}/meta.md の「issue-id 対応表」から該当工程の issue-id を取得し
      specs/{案件キー}/{工程dir}/{issue_id}/work-log.md に記録
  - 計画工程・スタック単位(pgplan/ptplan):
      同じ meta.md の対応表を「対象スタック」列で絞り込んで issue-id を取得し
      {スタック}/specs/{案件キー}/{工程dir}/{issue_id}/work-log.md に記録
  - area別工程(ss/pg/pt):
      対象スタックはファイルパスから確定済みのため {スタック}/specs/{案件キー}/{工程dir}/ 配下の
      *_{機能ID} ディレクトリを走査し、見つかった work-log.md に記録
  - 解決不能: 当該リポの specs/{案件キー}/work-log.md にフォールバック

加えて、前月以前のデイリーログを月次でアーカイブします（冪等処理）。
ログ自身の更新は記録対象外（無限ループ防止）。エラーは握り潰す silent failure 設計。
"""
import os
import sys
import json
import re
import subprocess
from datetime import datetime
from pathlib import Path


# feature/{案件キー}-{工程}[-{機能ID}]  （案件キー自体がハイフンを含むため工程トークンで切る）
# 機能ID は {カテゴリ}-{3桁連番}（カテゴリ: bs/batch/us-intra/us-inter/front-intra/front-inter）
BRANCH_PATTERN = re.compile(
    r"^feature/(?P<case>.+?)-(?P<phase>ssplan|pgplan|ptplan|sa|ui|ss|pg|pt)"
    r"(?:-(?P<fid>(?:bs|batch|us-intra|us-inter|front-intra|front-inter)-\d{3}))?$"
)
DAILY_LOG_FILENAME_PATTERN = re.compile(r"^(\d{4})-(\d{2})-(\d{2})\.md$")

# 工程トークン → (specs ディレクトリ名, area別か, meta 対応表の工程ラベル)
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

# 工程トークン → 案件リポ側で進むか（False ならスタックリポ側で進む）
CASE_REPO_PHASES = {"sa", "ui", "ssplan"}

# issue-init が着手時に1回だけ書き込むスコープ確定情報（各リポのローカル・未追跡ファイル）。
# 存在する場合、ブランチ名の正規表現解析・案件 meta.md の対応表探索より優先して使用する
# （ADR-010参照。1応答内でissueに属さない付帯編集が混ざった場合の誤解決を避ける）。
ISSUE_SCOPE_MARKER_REL_PATH = ".claude/.issue-scope.json"


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


def get_git_branch(repo_root=None):
    """現在の Git ブランチを取得。失敗時は (no-branch) を返す。

    repo_root 指定時はそのリポジトリ（`git -C {repo_root}` 相当）の現在ブランチを見る。
    スタックリポは案件リポとは別の git リポジトリのため、対象ファイルが
    スタックディレクトリ配下の場合は repo_root にそのスタックのルートを渡す。
    """
    try:
        result = subprocess.run(
            ["git", "branch", "--show-current"],
            capture_output=True, text=True, timeout=2,
            cwd=str(repo_root) if repo_root else None,
        )
        return result.stdout.strip() or "(no-branch)"
    except Exception:
        return "(unknown)"


def get_user_name():
    """git config user.name → USER → USERNAME の順で実装者名を取得。"""
    try:
        result = subprocess.run(
            ["git", "config", "user.name"],
            capture_output=True, text=True, timeout=2
        )
        name = result.stdout.strip()
        if name:
            return name
    except Exception:
        pass
    return os.environ.get("USER") or os.environ.get("USERNAME") or "(unknown)"


def find_product_root():
    """このスクリプトの位置からプロダクトルートを推定。

    .claude/hooks/log_work.py → .claude/hooks → .claude → product-root
    """
    return Path(__file__).resolve().parent.parent.parent


def to_relative_path(target_path, product_root):
    """プロダクトルートからの相対パスに正規化。失敗時は元のパスを返す。"""
    try:
        rel = Path(target_path).resolve().relative_to(product_root)
        return str(rel).replace("\\", "/")
    except (ValueError, OSError):
        return str(target_path).replace("\\", "/")


def find_git_repo_root(start_path):
    """start_path（ファイル or ディレクトリ）から親を遡り、最も近い `.git`
    （通常リポジトリのディレクトリ、または git worktree の gitdir ポインタファイル）
    を持つディレクトリを返す。見つからなければ None。

    product_root 配下にネストしたスタックリポ（例: product_root/bs/）と、
    product_root 外部に作成された git worktree（例: C:/wt/bs2/）の
    いずれも同一ロジックで解決できる（`git -C {path} rev-parse
    --show-toplevel` を使う代替案もあるが、Git Bash と Windows ネイティブ Python の
    パス表記〔POSIX風 `/c/...` と `C:\\...`〕が混在する本環境では素の pathlib による
    走査のほうが後続の比較・結合処理と一貫するため、こちらを採用する）。
    """
    try:
        p = Path(start_path).resolve()
    except (OSError, ValueError):
        return None
    current = p if p.is_dir() else p.parent
    for candidate in (current, *current.parents):
        if (candidate / ".git").exists():
            return candidate
    return None


def find_owning_repo_root(product_root, target_path):
    """編集対象ファイル（target_path）が属する git リポジトリのルートを返す。

    target_path から `find_git_repo_root` で最も近い `.git` 境界を探す。
    見つからない場合、または product_root 自身の外側で境界が見つからない
    異常系では product_root（案件リポ）にフォールバックする。

    既知の制約: 解決したリポジトリが
    product_root 外部の git worktree の場合、`owning_repo_root.name` は
    worktree のディレクトリ名（例: `bs2`）になり、スタック名（例: `bs`）と
    一致しない可能性がある。この値は PG-Plan/PT-Plan の meta.md 対象スタック
    列の絞り込み（`resolve_issue_work_logs` の `stack` 変数）でのみ使われ、
    area別工程（ss/pg/pt）の work-log 解決には影響しない。worktree ディレクトリ名を
    実際のスタック名に正しく対応付ける仕組みは本対応の範囲外とする。
    """
    if not target_path:
        return product_root
    resolved = find_git_repo_root(target_path)
    return resolved if resolved is not None else product_root


def append_log_line(log_file, header_text, line):
    """ログファイルへ追記。ファイルがなければヘッダ付きで新規作成。"""
    log_file.parent.mkdir(parents=True, exist_ok=True)
    if not log_file.exists():
        log_file.write_text(header_text, encoding="utf-8")
    with open(log_file, "a", encoding="utf-8") as f:
        f.write(line)


def archive_old_daily_logs(product_root, current_year_month):
    """前月以前のデイリーログを archive/{YYYY-MM}/ に月次でアーカイブする（冪等）。

    対象は docs/changelog/work-logs/ 直下の {YYYY-MM-DD}.md のみ。
    """
    work_logs_dir = product_root / "docs" / "changelog" / "work-logs"
    if not work_logs_dir.is_dir():
        return

    for log_file in work_logs_dir.iterdir():
        if not log_file.is_file():
            continue
        match = DAILY_LOG_FILENAME_PATTERN.match(log_file.name)
        if not match:
            continue
        file_year_month = f"{match.group(1)}-{match.group(2)}"
        if file_year_month >= current_year_month:
            continue
        archive_subdir = work_logs_dir / "archive" / file_year_month
        archive_subdir.mkdir(parents=True, exist_ok=True)
        target = archive_subdir / log_file.name
        if target.exists():
            continue
        try:
            log_file.rename(target)
        except OSError:
            pass


def resolve_phase_issue_id(meta_file, phase_label, stack=None):
    """案件 meta.md の「issue-id 対応表」から指定工程ラベルの issue-id を取得。無ければ None。

    対応表の想定形式（対象スタック列あり）:
      | 工程 | issue種別 | issue-id | 対象ID | 対象スタック | ブランチ | ステータス |

    stack 指定時（PG-Plan/PT-Plan 等・スタック単位の工程）は「対象スタック」列が
    一致する行に絞り込む（同じ工程ラベルでもスタックごとに別の issue-id を持つため）。
    列数が少ない旧形式（対象スタック列なし）にも後方互換で対応する。
    """
    try:
        text = meta_file.read_text(encoding="utf-8")
    except OSError:
        return None
    for line in text.splitlines():
        stripped = line.strip()
        if not stripped.startswith("|"):
            continue
        cells = [c.strip() for c in stripped.strip("|").split("|")]
        # cells[0]=工程, cells[1]=issue種別, cells[2]=issue-id, cells[3]=対象ID, cells[4]=対象スタック
        if len(cells) >= 3 and cells[0] == phase_label:
            issue_id = cells[2]
            if not issue_id or issue_id in ("", "-"):
                continue
            if stack and len(cells) >= 5:
                cell_stack = cells[4]
                if cell_stack and cell_stack not in ("", "-", "N/A") and cell_stack != stack:
                    continue
            return issue_id
    return None


def find_area_work_logs(phase_dir, fid):
    """area別工程: phase_dir（スタックリポ側 specs/{案件キー}/{工程dir}/）配下の
    *_{機能ID}/ を走査し work-log.md のパス一覧を返す（スタックは呼び出し側で確定済み）。
    """
    results = []
    if not fid or not phase_dir.is_dir():
        return results
    for issue_dir in phase_dir.iterdir():
        if issue_dir.is_dir() and issue_dir.name.endswith(f"_{fid}"):
            results.append(issue_dir / "work-log.md")
    return results


def read_issue_scope_marker(repo_root):
    """issue-init が書き込んだスコープ確定情報（ISSUE_SCOPE_MARKER_REL_PATH）を読む。

    存在しない・壊れている・必須キー（work_log）を欠く場合は None を返す
    （呼び出し側はブランチ名解析へフォールバックする。旧ブランチ・別作業者による
    フレッシュチェックアウト時〔マーカーはローカル・未追跡のため引き継がれない〕にも
    この経路を通る）。
    """
    try:
        marker_path = Path(repo_root) / ISSUE_SCOPE_MARKER_REL_PATH
        if not marker_path.is_file():
            return None
        data = json.loads(marker_path.read_text(encoding="utf-8"))
        if not isinstance(data, dict) or not data.get("work_log"):
            return None
        return data
    except Exception:
        return None


def resolve_issue_work_logs(product_root, branch, owning_repo_root=None):
    """記録先 work-log.md のパス一覧を解決する。

    owning_repo_root: 編集対象ファイルが属するリポジトリのルート（`find_owning_repo_root` の戻り値）。
      省略時は product_root（案件リポ）と同じ扱いにする。
      SA/UI/SS-Plan は常に案件リポ側、SS/PG-Plan/PG/PT-Plan/PT はスタックリポ側になる。

    まず owning_repo_root 配下のスコープ確定マーカー（`read_issue_scope_marker`）を確認し、
    存在する場合はそこに記録された work_log（owning_repo_root からの相対パス）を最優先で使用する。
    マーカーが無い場合のみ、以下の既存ロジック（ブランチ名の正規表現解析・案件 meta.md の
    対応表探索）にフォールバックする。
    案件メタ（`specs/{案件キー}/meta.md`）は常に案件リポ側が正本のため、
    issue-id の解決（対応表の参照）は product_root 側で行い、
    work-log.md の実配置先（phase_dir 以下）は owning_repo_root 側で組み立てる。

    解決不能なら当該リポの案件レベル（specs/{案件キー}/work-log.md）にフォールバック。
    新ワークフロー形式でないブランチには空リストを返す（issue別記録なし）。
    """
    owning_repo_root = owning_repo_root or product_root

    marker = read_issue_scope_marker(owning_repo_root)
    if marker:
        return [owning_repo_root / marker["work_log"]]

    m = BRANCH_PATTERN.match(branch)
    if not m:
        return []
    case_key = m.group("case")
    phase = m.group("phase")
    fid = m.group("fid")

    central_case_dir = product_root / "specs" / case_key  # meta.md の正本（常に案件リポ側）
    if not central_case_dir.is_dir():
        return []

    local_case_dir = owning_repo_root / "specs" / case_key  # work-log.md の実配置先
    phase_dir_name, is_area, phase_label = PHASE_MAP[phase]
    phase_dir = local_case_dir / phase_dir_name

    # 案件リポ側で進む工程（sa/ui/ssplan）以外はスタック単位（PG-Plan/PT-Plan は meta.md の絞り込みに使用）
    stack = None
    if phase not in CASE_REPO_PHASES and owning_repo_root != product_root:
        stack = owning_repo_root.name

    targets = []
    if is_area:
        targets = find_area_work_logs(phase_dir, fid)
    else:
        issue_id = resolve_phase_issue_id(central_case_dir / "meta.md", phase_label, stack=stack)
        if issue_id:
            issue_dir = phase_dir / issue_id
            if issue_dir.is_dir():
                targets.append(issue_dir / "work-log.md")

    if not targets:
        # フォールバック: 当該リポの案件レベル
        targets.append(local_case_dir / "work-log.md")
    return targets


def main():
    try:
        hook_data = read_hook_input()
        target_path = (
            hook_data.get("tool_input", {}).get("file_path")
            or hook_data.get("file_path", "")
        )
        if not target_path:
            return

        product_root = find_product_root()
        rel_path = to_relative_path(target_path, product_root)

        # ログ自身は記録対象外（無限ループ防止）
        if "docs/changelog/work-logs/" in rel_path:
            return
        if rel_path.endswith("/work-log.md"):
            return

        now = datetime.now()
        date_str = now.strftime("%Y-%m-%d")
        time_str = now.strftime("%H:%M:%S")
        year_month = now.strftime("%Y-%m")

        # 月次アーカイブ（冪等）
        archive_old_daily_logs(product_root, year_month)

        user = get_user_name()
        owning_repo_root = find_owning_repo_root(product_root, target_path)
        branch = get_git_branch(owning_repo_root)
        entry_line = f"- {time_str} [{user}@{branch}] `{rel_path}`\n"

        # 1. デイリーログ（常に記録）
        daily_log = product_root / "docs" / "changelog" / "work-logs" / f"{date_str}.md"
        daily_header = (
            f"# 作業ログ {date_str}\n\n"
            f"> このファイルは `.claude/hooks/log_work.py` により自動生成されています。\n"
            f"> 手動メモは `/work-log` スキルで追記してください。\n\n"
        )
        append_log_line(daily_log, daily_header, entry_line)

        # 2. issue スコープ別ログ（新ワークフローのブランチが解決できる場合）
        for issue_log in resolve_issue_work_logs(product_root, branch, owning_repo_root):
            issue_header = (
                f"# 作業ログ（工程issue スコープ）\n\n"
                f"> このファイルは `.claude/hooks/log_work.py` により自動生成されています。\n"
                f"> ブランチ {branch} 上の作業を記録します。\n\n"
            )
            issue_entry = f"- {date_str} {time_str} [{user}@{branch}] `{rel_path}`\n"
            append_log_line(issue_log, issue_header, issue_entry)

    except Exception:
        # silent failure: 本処理を妨げない
        pass


if __name__ == "__main__":
    main()
