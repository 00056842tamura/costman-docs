#!/usr/bin/env bash
# template-sync.sh — LLM機構テンプレートの「機構レイヤ」を 案件リポへ取り込み・同期する
#   ※ スタックリポは機構（.claude/）を持たないため、このスクリプトの適用対象外。
#
# 使い方:
#   template-sync.sh --template {git-url|local-path} [--target {dir:default .}] \
#       [--branch chore/template-sync] [--repo {owner/repo}] [--create-pr] [--mirror]
#
# 同期（機構・上書き）: .claude/{rules,skills,agents,hooks,orchestrators,scripts}, .claude/settings.json,
#   .claude/repositories.local.md.example（個人ローカルファイルのひな形）,
#   docs/templates/（設計テンプレ・全案件共通）, specs/templates/（工程メモテンプレ）,
#   .github/, _templates/（新規スタック初期化用ひな形・全案件共通）, README.md, MIGRATION_GUIDE.md
# 非上書き（プロダクト依存・差分のみ報告）: CLAUDE.md, .gitignore
# 不可侵（プロダクト成果物・機構リストに無い＝触らない）: docs/requirements,base-design,architecture,glossary,
#   operation,changelog / {スタック}/(src・docs) / specs/{案件キー}/ / .claude/logs/ / .claude/settings.local.json /
#   .claude/repositories.local.md（個人ローカル・.claude/repositories.local.md.example は同期対象）
# 安全: DRY_RUN=1 で変更系を表示のみ。--mirror 未指定時はテンプレ削除分を orphan 報告（自動削除しない）。
# 規約: .claude/rules/github-ops.md
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$SCRIPT_DIR/lib/proxy-detect.sh"

TEMPLATE=""; TARGET="."; BRANCH="chore/template-sync"; REPO=""; CREATE_PR=0; MIRROR=0
DRY_RUN="${DRY_RUN:-0}"
usage(){ sed -n '2,17p' "$0"; exit 1; }
while [ $# -gt 0 ]; do case "$1" in
  --template) TEMPLATE="$2"; shift 2;; --target) TARGET="$2"; shift 2;;
  --branch) BRANCH="$2"; shift 2;; --repo) REPO="$2"; shift 2;;
  --create-pr) CREATE_PR=1; shift;; --mirror) MIRROR=1; shift;;
  -h|--help) usage;; *) echo "不明な引数: $1" >&2; usage;; esac; done
[ -n "$TEMPLATE" ] || { echo "ERROR: --template 必須（git URL or ローカルパス）" >&2; usage; }
[ -d "$TARGET" ] || { echo "ERROR: --target ディレクトリ無し: $TARGET" >&2; exit 1; }

run(){ if [ "$DRY_RUN" = "1" ]; then echo "  [dry-run] $*"; else "$@"; fi; }

# テンプレート取得（git URL なら浅くクローン）
CLEANUP_TPL=""
case "$TEMPLATE" in
  *://*|*@*:*|*.git)
    TMP=$(mktemp -d); echo "▶ テンプレートを取得: $TEMPLATE"
    git clone --depth 1 "$TEMPLATE" "$TMP" >/dev/null 2>&1 || { echo "ERROR: clone 失敗: $TEMPLATE" >&2; exit 1; }
    TPL="$TMP"; CLEANUP_TPL="$TMP" ;;
  *) TPL="$TEMPLATE" ;;
esac
trap '[ -n "$CLEANUP_TPL" ] && rm -rf "$CLEANUP_TPL"' EXIT
[ -d "$TPL/.claude" ] || { echo "ERROR: テンプレートに .claude/ が無い: $TPL" >&2; exit 1; }

MACH_DIRS=(.claude/rules .claude/skills .claude/agents .claude/hooks .claude/orchestrators .claude/scripts docs/templates specs/templates .github _templates)
MACH_FILES=(.claude/settings.json .claude/repositories.local.md.example README.md MIGRATION_GUIDE.md)
REPORT_FILES=(CLAUDE.md .gitignore)

echo "=== テンプレート同期: $TPL → $TARGET（DRY_RUN=$DRY_RUN・mirror=$MIRROR）==="

for d in "${MACH_DIRS[@]}"; do
  [ -d "$TPL/$d" ] || continue
  echo "▶ [機構dir] $d"
  if [ -d "$TARGET/$d" ]; then
    while IFS= read -r line; do
      echo "   ⚠ orphan（テンプレに無い・自動削除しない）: ${line#Only in }"
    done < <(diff -rq "$TPL/$d" "$TARGET/$d" 2>/dev/null | grep "^Only in $TARGET/" || true)
  fi
  if [ "$MIRROR" = "1" ]; then run rm -rf "$TARGET/$d"; fi
  run mkdir -p "$TARGET/$d"
  if [ "$DRY_RUN" = "1" ]; then echo "  [dry-run] cp -R $TPL/$d/. $TARGET/$d/"; else cp -R "$TPL/$d/." "$TARGET/$d/"; fi
done

for f in "${MACH_FILES[@]}"; do
  [ -f "$TPL/$f" ] || continue
  echo "▶ [機構file] $f"
  run cp "$TPL/$f" "$TARGET/$f"
done

for f in "${REPORT_FILES[@]}"; do
  [ -f "$TPL/$f" ] || continue
  if [ ! -f "$TARGET/$f" ]; then
    echo "▶ [初回] $f 未存在 → コピー（以後はプロダクト編集・非上書き）"; run cp "$TPL/$f" "$TARGET/$f"; continue
  fi
  if diff -q "$TPL/$f" "$TARGET/$f" >/dev/null 2>&1; then
    echo "= [非上書き] $f は一致"
  else
    echo "⚠ [非上書き] $f はテンプレと差分あり（プロダクト依存＝手動マージ）。diff（target → template・先頭40行）:"
    diff "$TARGET/$f" "$TPL/$f" | head -40 || true
  fi
done

echo "✅ 機構レイヤを処理（プロダクト成果物 docs/requirements・docs/base-design・{スタック}/src・specs/{案件キー} は不可侵）。"

if [ "$CREATE_PR" = "1" ]; then
  echo "▶ 同期ブランチ $BRANCH に commit → PR（人間レビュー）"
  run git -C "$TARGET" switch -c "$BRANCH"
  run git -C "$TARGET" add -A
  run git -C "$TARGET" commit -m "chore: sync template machinery from ① (template-sync.sh)"
  run git -C "$TARGET" push -u origin "$BRANCH"
  if [ -n "$REPO" ]; then
    run gh pr create --repo "$REPO" --base develop --head "$BRANCH" \
      --title "chore: テンプレート機構同期" \
      --body "LLM機構テンプレートの機構レイヤを同期（template-sync.sh）。CLAUDE.md/.gitignore の差分は手動マージ。プロダクト成果物は不可侵。"
  fi
fi
echo "ℹ 初回の新規リポは gh repo create {repo} --template {①テンプレリポ} で丸ごと生成可（MIGRATION_GUIDE）。"
