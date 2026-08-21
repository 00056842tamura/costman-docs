#!/usr/bin/env bash
# log_work.py 呼び出しラッパー（PostToolUse・matcher: Write|Edit）。
#
# 旧来の `bash -c "python3 .claude/hooks/log_work.py ... || python ... || true"` は、
# コマンド文字列内の相対パス `.claude/hooks/log_work.py` をシェルの起動時カレント
# ディレクトリ基準で解決するため、cwd が worktree（`.claude/` を持たない別リポジトリの
# チェックアウト）配下にあると python3/python のどちらもスクリプトを見つけられず、
# `|| true` により失敗が一切表示されないまま作業ログの記録がサイレントに欠落する。
# check_java.sh 等と同じ `${BASH_SOURCE[0]}` 基準の絶対パス解決に統一し、
# cwd に依存しないようにする。
#
# 本フックは記録のみが目的で、失敗してもツール呼び出しをブロックしてはならない
# ため、Python 側の終了コードに関わらず常に exit 0 する（旧来の `|| true` と同じ意味）。
set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INPUT="$(cat)"

is_python3() {
  command -v "$1" >/dev/null 2>&1 && "$1" -c "import sys; sys.exit(0 if sys.version_info[0] >= 3 else 1)" >/dev/null 2>&1
}

if is_python3 python3; then
  PYBIN="python3"
elif is_python3 python; then
  PYBIN="python"
else
  exit 0
fi

# PYTHONUTF8=1: Windows・日本語ロケール環境では sys.stdin の既定エンコーディングが
# cp932 になり、UTF-8 のフック入力 JSON（日本語パスを含む場合）が UnicodeDecodeError
# になって握り潰される（issue #71）。Python の UTF-8 モードを強制して回避する。
printf '%s' "$INPUT" | PYTHONUTF8=1 "$PYBIN" "$SCRIPT_DIR/log_work.py" || true
exit 0
