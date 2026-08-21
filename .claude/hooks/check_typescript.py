"""
Reacter プログラミング規約チェック — PreToolUse フック
Write/Edit ツール実行前に TypeScript/TSX ファイルおよび package.json を検査し、
禁止パターンを検出してブロックします。
テストファイル (.test.ts / .spec.ts / .test.tsx / .spec.tsx) は対象外。

重要: PreToolUse フックで実際にツール呼び出しをブロックできるのは exit code 2 のみ
（exit 1 は非ブロッキングエラーとして処理が継続される）。
違反を検出した場合は sys.exit(2) でツール呼び出しをブロックします。

入力は標準入力（公式仕様）を優先し、環境変数 CLAUDE_TOOL_INPUT を後方互換の
フォールバックとして使う（read_hook_input 参照）。
"""
import os
import re
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
        return tool_input["content"].splitlines(keepends=True)
    elif "old_string" in tool_input and "new_string" in tool_input:
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


def check_typescript(file_path, lines):
    """TypeScript / TSX ファイルの規約チェック。"""
    issues = []
    for i, line in enumerate(lines, 1):
        stripped = line.strip()

        # コメント行は一部チェックをスキップ
        is_comment = stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*")

        # React.FC 禁止
        if not is_comment and re.search(r"\bReact\.FC\b", stripped):
            issues.append(
                f"  [規約違反 L{i}] React.FC 禁止: {stripped[:80]}"
                f" → 戻り値型を省略するか JSX.Element を使用すること"
            )

        # any 型禁止（: any、as any、<any>、Array<any> などを検出）
        # 注: `:\s*any\b` に否定後読み (?<!\w) を付けない（識別子直後のコロンに
        # 常に単語文字が来るため、`変数名: any` という最も一般的な書き方が検知
        # できなくなる）。`as\s+any\b` のみ既存の否定後読みを維持する。
        if not is_comment and re.search(r"(:\s*any\b|(?<!\w)as\s+any\b|<any>|Array<any>)", stripped):
            issues.append(
                f"  [規約違反 L{i}] any 型禁止: {stripped[:80]}"
                f" → 適切な型定義または unknown を使用すること"
            )

        # dangerouslySetInnerHTML 禁止
        if not is_comment and "dangerouslySetInnerHTML" in stripped:
            issues.append(
                f"  [規約違反 L{i}] dangerouslySetInnerHTML 禁止（XSS リスク）: {stripped[:80]}"
                f" → JSX の通常のレンダリングを使用すること"
            )

        # innerHTML への代入禁止
        if not is_comment and re.search(r"\.innerHTML\s*=", stripped):
            issues.append(
                f"  [規約違反 L{i}] innerHTML への直接代入禁止（XSS リスク）: {stripped[:80]}"
                f" → JSX の通常のレンダリングを使用すること"
            )

        # window.alert 禁止
        if not is_comment and re.search(r"\bwindow\.alert\s*\(", stripped):
            issues.append(
                f"  [規約違反 L{i}] window.alert 禁止: {stripped[:80]}"
                f" → カスタムのダイアログコンポーネントを使用すること"
            )

        # console.log 残留禁止（console.error / console.warn は許容）
        if not is_comment and re.search(r"\bconsole\.log\s*\(", stripped):
            issues.append(
                f"  [規約違反 L{i}] console.log 禁止（本番コードへの混入）: {stripped[:80]}"
                f" → 削除すること"
            )

        # debugger 残留禁止
        if not is_comment and re.search(r"\bdebugger\b", stripped):
            issues.append(
                f"  [規約違反 L{i}] debugger 禁止: {stripped[:80]}"
                f" → 削除すること"
            )

        # TODO コメント禁止
        if re.search(r"//\s*TODO", line):
            issues.append(
                f"  [規約違反 L{i}] TODO コメント禁止: {stripped[:80]}"
                f" → issue 化して削除すること"
            )

    return issues


def check_package_json(lines):
    """package.json のバージョン固定チェック。"""
    issues = []
    try:
        content = "".join(lines)
        data = json.loads(content)
    except (json.JSONDecodeError, Exception):
        return issues

    def scan_versions(section_name, deps):
        if not isinstance(deps, dict):
            return
        for pkg, version in deps.items():
            if isinstance(version, str) and re.match(r"^[\^~*]", version):
                issues.append(
                    f"  [規約違反] {section_name}.{pkg}: バージョン '{version}' はワイルドカード指定禁止"
                    f" → package-lock.json で固定された正確なバージョン（例: '1.2.3'）を指定すること"
                )

    scan_versions("dependencies", data.get("dependencies", {}))
    scan_versions("devDependencies", data.get("devDependencies", {}))
    return issues


def main():
    try:
        hook_data = read_hook_input()
        tool_input = hook_data.get("tool_input", {})
        file_path = tool_input.get("file_path", "")
        normalized = file_path.replace("\\", "/")

        is_ts_file = file_path.endswith(".ts") or file_path.endswith(".tsx")
        is_pkg_json = normalized.endswith("package.json") and "node_modules" not in normalized

        if not is_ts_file and not is_pkg_json:
            return

        # テストファイルは除外
        if is_ts_file and re.search(r"\.(test|spec)\.(ts|tsx)$", file_path):
            return

        lines = get_content_lines(tool_input)
        if lines is None:
            return

        if is_ts_file:
            issues = check_typescript(file_path, lines)
            checker_name = "Reacter 規約チェック"
        else:
            issues = check_package_json(lines)
            checker_name = "package.json バージョンチェック"

        if issues:
            filename = os.path.basename(file_path)
            print(f"[{checker_name}] {filename} に違反を検出（書き込みをブロックしました）:", file=sys.stderr)
            for issue in issues:
                print(issue, file=sys.stderr)
            print("上記の違反を修正してから再度書き込んでください。", file=sys.stderr)
            try:
                append_violation_log(find_product_root(), filename, issues)
            except Exception:
                pass
            sys.exit(2)

    except Exception:
        pass


if __name__ == "__main__":
    main()
