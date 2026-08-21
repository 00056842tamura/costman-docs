#!/usr/bin/env bash
# check_review_gate.py 呼び出しラッパー（PreToolUse・matcher: Bash）。
#
# check_backlog_gate.sh と同じ理由（stdin の二重読み取り事故を避けるため、
# 標準入力を一度だけ読み取り、実行可能なインタプリタ〔python3 優先・無ければ python〕を
# 実行前に判定してから、選んだインタプリタ1つだけに渡す）で同じ実装パターンに揃える。
# 本フックは「実際にブロックする」ことが目的そのものなので、この問題を避ける。
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
  # 実行可能な Python 3 インタプリタが見つからない場合は fail-safe（ブロックせず素通り）
  exit 0
fi

# PYTHONUTF8=1: Windows・日本語ロケール環境では sys.stdin の既定エンコーディングが
# cp932 になり、UTF-8 のフック入力 JSON（日本語パスを含む場合）が UnicodeDecodeError
# になって握り潰される（issue #71）。Python の UTF-8 モードを強制して回避する。
printf '%s' "$INPUT" | PYTHONUTF8=1 "$PYBIN" "$SCRIPT_DIR/check_review_gate.py"
exit $?
