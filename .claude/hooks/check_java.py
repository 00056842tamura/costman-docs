"""
Springer プログラミング規約チェック — PreToolUse フック
Write/Edit ツール実行前に Java ファイルを検査し、禁止パターンを検出してブロックします。
テストファイル (src/test/) は対象外。

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

        if not file_path.endswith(".java"):
            return
        # テストファイルは除外
        if "/src/test/" in file_path.replace("\\", "/"):
            return

        lines = get_content_lines(tool_input)
        if lines is None:
            return

        issues = []

        for i, line in enumerate(lines, 1):
            stripped = line.strip()
            is_comment = (
                stripped.startswith("//")
                or stripped.startswith("*")
                or stripped.startswith("/*")
            )

            # Lombok インポート
            if "import lombok" in stripped:
                issues.append(
                    f"  [規約違反 L{i}] Lombok インポート禁止: {stripped}"
                    f" → 削除すること"
                )

            # スターインポート（静的インポートは除外）
            if (
                stripped.startswith("import ")
                and stripped.endswith(".*;")
                and not stripped.startswith("import static")
            ):
                issues.append(
                    f"  [規約違反 L{i}] スターインポート禁止: {stripped}"
                    f" → 使用クラスを個別に import すること"
                )

            # フィールドインジェクション (@Autowired の次行が private フィールド)
            if stripped == "@Autowired" and i < len(lines):
                next_stripped = lines[i].strip()
                if next_stripped.startswith("private ") and "(" not in next_stripped:
                    issues.append(
                        f"  [規約違反 L{i + 1}] フィールドインジェクション禁止: "
                        f"{next_stripped[:70]} → コンストラクターインジェクションを使用"
                    )

            # protected 修飾子
            if not is_comment and re.search(r"\bprotected\b", stripped):
                issues.append(
                    f"  [規約違反 L{i}] protected 修飾子禁止: {stripped[:70]}"
                    f" → private / public に変更すること"
                )

            # SQL アノテーション
            if stripped.startswith("@") and any(
                stripped.startswith(a)
                for a in ("@Select", "@Insert", "@Update", "@Delete")
            ):
                issues.append(
                    f"  [規約違反 L{i}] SQL アノテーション禁止: {stripped[:70]}"
                    f" → SQL は Mapper XML に記述すること"
                )

            # TODO コメント
            if "// TODO" in line or "//TODO" in line:
                issues.append(
                    f"  [規約違反 L{i}] TODO コメント禁止: {stripped[:70]}"
                    f" → 削除すること"
                )

            # 広スコープ catch
            if re.search(
                r"catch\s*\(\s*(Exception|Throwable|RuntimeException)\s*[\w)]",
                stripped,
            ):
                issues.append(
                    f"  [規約違反 L{i}] 広スコープ catch 禁止: {stripped[:70]}"
                    f" → 具体的な例外型を指定すること"
                )

            # 旧一時ファイル API（springer-file-io.md）
            if not is_comment and re.search(r"\bFile\.createTempFile\s*\(", stripped):
                issues.append(
                    f"  [規約違反 L{i}] 旧一時ファイル API 禁止: {stripped[:70]}"
                    f" → java.nio.file.Files.createTempFile を使用すること"
                )

            # SLF4J LoggerFactory 直接利用（springer-logging.md）
            if "import org.slf4j.LoggerFactory" in stripped:
                issues.append(
                    f"  [規約違反 L{i}] SLF4J LoggerFactory 直接利用禁止: {stripped[:70]}"
                    f" → jp.co.nekonet.springer.logging.LoggerFactory を使用すること"
                )

            # System.out/err・printStackTrace（springer-logging.md）
            if not is_comment and (
                re.search(r"System\.(out|err)\.print", stripped)
                or ".printStackTrace(" in stripped
            ):
                issues.append(
                    f"  [規約違反 L{i}] System.out/err・printStackTrace 禁止: {stripped[:70]}"
                    f" → ロガー（LoggerFactory）で出力すること"
                )

            # ログメッセージの文字列連結（springer-logging.md / masking.md）
            if not is_comment and re.search(
                r"\b(LOGGER|logger)\.\w+\s*\([^)]*\"\s*\+", stripped
            ):
                issues.append(
                    f"  [規約違反 L{i}] ログの文字列連結禁止: {stripped[:70]}"
                    f" → SLF4J プレースホルダ {{}} を使用すること"
                )

            # ラッパークラスのコンストラクタ生成（springer-checkstyle.md）
            if not is_comment and re.search(
                r"\bnew\s+(Integer|Long|Short|Byte|Boolean|Character|Double|Float)\s*\(",
                stripped,
            ):
                issues.append(
                    f"  [規約違反 L{i}] ラッパークラスの new 禁止: {stripped[:70]}"
                    f" → valueOf / オートボクシングを使用すること"
                )

            # WebSecurityConfigurerAdapter 継承（springer-security-config.md）
            if "extends WebSecurityConfigurerAdapter" in stripped:
                issues.append(
                    f"  [規約違反 L{i}] WebSecurityConfigurerAdapter 継承禁止: {stripped[:70]}"
                    f" → SecurityFilterChain を Bean 登録すること"
                )

            # BCryptPasswordEncoder 強度未指定（springer-security-config.md）
            if not is_comment and re.search(
                r"new\s+BCryptPasswordEncoder\s*\(\s*\)", stripped
            ):
                issues.append(
                    f"  [規約違反 L{i}] BCryptPasswordEncoder 強度未指定: {stripped[:70]}"
                    f" → new BCryptPasswordEncoder(12) を使用すること"
                )

            # @SchemaMapping 禁止（springer-graphql.md）
            if re.match(r"@SchemaMapping\b", stripped):
                issues.append(
                    f"  [規約違反 L{i}] @SchemaMapping 禁止: {stripped[:70]}"
                    f" → N+1 回避のため @BatchMapping を使用すること"
                )

        if issues:
            filename = os.path.basename(file_path)
            print(f"[Springer 規約チェック] {filename} に違反を検出（書き込みをブロックしました）:", file=sys.stderr)
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
