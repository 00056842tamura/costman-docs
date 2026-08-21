#!/usr/bin/env bash
# pr-open.sh — 工程ゲート PR を作成（全工程共通: sa/ui/ssplan/ss/pgplan/pg/ptplan/pt）。
# 使い方:
#   pr-open.sh --repo {owner/repo} --case {案件キー} --phase {sa|ui|ssplan|ss|pgplan|pg|ptplan|pt} [--feature bs-001] \
#       --title "{要約}" [--body-file {file}] [--stacks "bs,us-api"] [--issue {N}] [--base {ブランチ}] [--draft]
# テンプレ: 工程ごとに .github/PULL_REQUEST_TEMPLATE/{sa|ui|ss-plan|ss|pg-plan|pg|pt-plan|pt}.md
#           （pr_template_name() でマッピング）。--body-file 指定時はそちらを優先。規約: .claude/rules/github-ops.md
# **マージはしない**（人間レビュー）。DRY_RUN=1 で gh pr create を表示のみ。
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$SCRIPT_DIR/lib/proxy-detect.sh"
. "$SCRIPT_DIR/lib/pr-template.sh"
REPO=""; CASE=""; PHASE=""; FEATURE=""; TITLE=""; BODY_FILE=""; STACKS=""; ISSUE=""; DRAFT=0; BASE=""
DRY_RUN="${DRY_RUN:-0}"
usage(){ sed -n '2,9p' "$0"; exit 1; }
while [ $# -gt 0 ]; do case "$1" in
  --repo) REPO="$2"; shift 2;; --case) CASE="$2"; shift 2;; --phase) PHASE="$2"; shift 2;;
  --feature) FEATURE="$2"; shift 2;; --title) TITLE="$2"; shift 2;; --body-file) BODY_FILE="$2"; shift 2;;
  --stacks) STACKS="$2"; shift 2;; --issue) ISSUE="$2"; shift 2;; --base) BASE="$2"; shift 2;;
  --draft) DRAFT=1; shift;; -h|--help) usage;; *) echo "不明な引数: $1" >&2; usage;; esac; done
for v in REPO CASE PHASE TITLE; do [ -n "${!v}" ] || { echo "ERROR: --${v,,} 必須" >&2; usage; }; done

case "$PHASE" in
  sa)     KLBL="工程:sa";      BSEG="sa";     PHASE_UC="SA";;
  ui)     KLBL="工程:ui";      BSEG="ui";     PHASE_UC="UI";;
  ssplan) KLBL="工程:ss-plan"; BSEG="ssplan"; PHASE_UC="SS-Plan";;
  ss)     KLBL="工程:ss";      BSEG="ss";     PHASE_UC="SS";;
  pgplan) KLBL="工程:pg-plan"; BSEG="pgplan"; PHASE_UC="PG-Plan";;
  pg)     KLBL="工程:pg";      BSEG="pg";     PHASE_UC="PG";;
  ptplan) KLBL="工程:pt-plan"; BSEG="ptplan"; PHASE_UC="PT-Plan";;
  pt)     KLBL="工程:pt";      BSEG="pt";     PHASE_UC="PT";;
  *) echo "ERROR: 未知の --phase '$PHASE'" >&2; usage;;
esac
case "$PHASE" in ss|pg|pt) [ -n "$FEATURE" ] || { echo "ERROR: $PHASE は --feature bs-001 必須" >&2; usage; };; esac

if [ -n "$FEATURE" ]; then HEAD="feature/$CASE-$BSEG-$FEATURE"; else HEAD="feature/$CASE-$BSEG"; fi
BASE="${BASE:-feature/$CASE}"

# PR タイトルのブラケット: task 工程は機能ID を含める
if [ -n "$FEATURE" ]; then PR_PREFIX="[$PHASE_UC-$FEATURE]"; else PR_PREFIX="[$PHASE_UC]"; fi

stack_block(){
  local all="frontend us-api us-mpa bs batch" s
  for s in $all; do
    if printf ',%s,' ",${STACKS}," | grep -q ",$s,"; then echo "- [x] $s/"; else echo "- [ ] $s/（変更なし）"; fi
  done
}

if [ -n "$BODY_FILE" ]; then
  BODY=$(cat "$BODY_FILE")
else
  REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo ".")
  TMPL_NAME=$(pr_template_name "$PHASE") || TMPL_NAME=""
  TMPL_FILE="$REPO_ROOT/.github/PULL_REQUEST_TEMPLATE/${TMPL_NAME}.md"
  PHASE_LINE="${PHASE_UC}${FEATURE:+（機能ID: $FEATURE）}"
  ISSUE_LINE="${ISSUE:+Closes #$ISSUE}"
  STACK_CONTENT=$(stack_block)
  if BODY=$(render_pr_template "$TMPL_FILE" "$CASE" "$PHASE_LINE" "$ISSUE_LINE" "$STACK_CONTENT"); then
    :
  else
    # テンプレートが見つからない場合のフォールバック
    BODY=$(cat <<EOF
## 工程 / 案件
- 案件キー（Milestone）: $CASE
- 工程: ${PHASE_UC}${FEATURE:+（機能ID: $FEATURE）}
$([ -n "${ISSUE:-}" ] && echo "- Closes #$ISSUE")
- ベースブランチ: $BASE

## 影響スタック
$(stack_block)

## チェック
- [ ] 影響スタックを明記（product-rules）
- [ ] フック（規約チェック）の警告を解消
- [ ] 工程の完了ゲート条件を満たす
EOF
)
  fi
fi

echo "▶ PR 作成: $PR_PREFIX $TITLE  ($REPO  $HEAD → $BASE)"
TMP=$(mktemp); printf '%s\n' "$BODY" > "$TMP"; trap 'rm -f "$TMP"' EXIT
ARGS=(--repo "$REPO" --base "$BASE" --head "$HEAD" --title "$PR_PREFIX $TITLE" --label "$KLBL" --body-file "$TMP")
[ "$DRAFT" = "1" ] && ARGS+=(--draft)
if [ "$DRY_RUN" = "1" ]; then
  echo "  [dry-run] gh pr create ${ARGS[*]}"
else
  gh pr create "${ARGS[@]}"
fi
echo "✅ PR 作成（**マージは人間レビュー後**・develop→main は別途・product-rules）。"
