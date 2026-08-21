#!/usr/bin/env bash
# check_mapper_xml.py 呼び出しラッパー（PreToolUse・matcher: Write|Edit）。
#
# 旧来の `python3 ... 2>/dev/null || python ... 2>/dev/null || true` という
# フォールバック連結は、python3 が実行できた場合に標準入力（フック入力 JSON）を
# 読み切ってしまい、`||` で起動する python 側が空の標準入力しか受け取れず、
# 正しく検知できなくなる問題がある（stdin は1回しか読めないため）。
# 本スクリプトは警告専用（常に exit 0・ブロックしない）だが、入力を正しく検知できないと
# 警告ログ（docs/changelog/work-logs/violations-*.md）が残らないため、実効性のために
# 標準入力を一度だけ読み取り、実行可能なインタプリタ（python3 優先・無ければ python）を
# 実行前に判定してから、選んだインタプリタ 1 つだけに渡す。
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
  # 実行可能な Python 3 インタプリタが見つからない場合は fail-safe（素通り。本スクリプトは常にブロックしない設計）
  exit 0
fi

# PYTHONUTF8=1: Windows・日本語ロケール環境では sys.stdin の既定エンコーディングが
# cp932 になり、UTF-8 のフック入力 JSON（日本語パスを含む場合）が UnicodeDecodeError
# になって握り潰される（issue #71）。Python の UTF-8 モードを強制して回避する。
printf '%s' "$INPUT" | PYTHONUTF8=1 "$PYBIN" "$SCRIPT_DIR/check_mapper_xml.py"
exit 0
