"""
Springer プログラミング規約チェック — PreToolUse フック
Write/Edit ツール実行前に MyBatis Mapper XML ファイルを検査し、
SQL インジェクションリスクのある ${} 使用を警告します（ブロックしません）。

入力は標準入力（公式仕様）を優先し、環境変数 CLAUDE_TOOL_INPUT を後方互換の
フォールバックとして使う（read_hook_input 参照）。
"""
import os
import sys
import json
from datetime import datetime
from pathlib import Path


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
    return Path(__file__).resolve().parent.parent.parent


def append_violation_log(product_root, filename, issues):
    now = datetime.now()
    date_str = now.strftime("%Y-%m-%d")
    time_str = now.strftime("%H:%M:%S")
    log_dir = product_root / "docs" / "changelog" / "work-logs"
    log_file = log_dir / f"violations-{date_str}.md"
    log_dir.mkdir(parents=True, exist_ok=True)
    header = (
        f"# 規約違反ログ {date_str}\n\n"
        f"> このファイルは `.claude/hooks/` により自動生成されています。\n\n"
    )
    if not log_file.exists():
        log_file.write_text(header, encoding="utf-8")
    entry = f"## {time_str} `{filename}`\n"
    for issue in issues:
        entry += f"{issue}\n"
    entry += "\n"
    with open(log_file, "a", encoding="utf-8") as f:
        f.write(entry)


def get_content_lines(tool_input):
    """ツール入力から書き込み後のコンテンツ行リストを返す。"""
    file_path = tool_input.get("file_path", "")
    if "content" in tool_input:
        # Write ツール: content フィールドから直接取得
        return tool_input["content"].splitlines(keepends=True)
    elif "old_string" in tool_input and "new_string" in tool_input:
        # Edit ツール: 既存ファイルを読んで編集をシミュレート
        if not os.path.isfile(file_path):
            return None
        with open(file_path, encoding="utf-8", errors="ignore") as f:
            content = f.read()
        old_string = tool_input["old_string"]
        new_string = tool_input["new_string"]
        if tool_input.get("replace_all", False):
            content = content.replace(old_string, new_string)
        else:
            content = content.replace(old_string, new_string, 1)
        return content.splitlines(keepends=True)
    return None


def main():
    try:
        hook_data = read_hook_input()
        tool_input = hook_data.get("tool_input", {})
        file_path = tool_input.get("file_path", "")

        if not file_path.endswith(".xml"):
            return

        # Mapper XML のみ対象（ファイル名が Mapper.xml で終わる、または mapper/ パッケージ配下）
        normalized = file_path.replace("\\", "/")
        if not (file_path.endswith("Mapper.xml") or "/mapper/" in normalized):
            return

        lines = get_content_lines(tool_input)
        if lines is None:
            return

        issues = []

        for i, line in enumerate(lines, 1):
            stripped = line.strip()

            # XMLコメント行は除外
            if stripped.startswith("<!--"):
                continue

            # ${} の使用（SQL インジェクション注意）
            if "${" in line:
                issues.append(
                    f"  [注意 L{i}] ${{}} 使用（SQLインジェクション注意。ユーザー入力値に使用禁止）: "
                    f"{stripped[:80]}"
                )

        if issues:
            filename = os.path.basename(file_path)
            print(f"[Springer 規約チェック] {filename} に警告:", file=sys.stderr)
            for issue in issues:
                print(issue, file=sys.stderr)
            try:
                append_violation_log(find_product_root(), filename, issues)
            except Exception:
                pass
        # 警告のみ、ブロックしない（exit 0）

    except Exception:
        pass


if __name__ == "__main__":
    main()
