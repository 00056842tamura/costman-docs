#!/usr/bin/env bash
# issue-open.sh — 工程 issue を起票し（任意で）作業ブランチを作成する
#
# 使い方:
#   issue-open.sh --repo {owner}/{repo} --case {案件キー} --phase {sa|ui|ssplan|ss|pgplan|pg|ptplan|pt} \
#       --title "{要約}" [--feature bs-001] [--area {スタック}] [--blocked "#12 #15"] \
#       [--body-file {file} | --body "{text}"] [--create-branch] [--reopen-suffix sp-{m}]
#
# phase と type/ラベル/ブランチ/リポ区分の対応:
#   sa/ui/ssplan = type:phase・repo:case（案件リポ）
#   pgplan/ptplan = type:phase・repo:stack（スタックリポ・スタック単位）
#   ss/pg/pt = type:task・repo:stack（スタックリポ・機能ID単位／--feature 必須）
#
# 本文（--body/--body-file 未指定時のデフォルト）:
#   .github/ISSUE_TEMPLATE/{phase}.md を読み込み、「## メタ」ブロックのみ動的値に差し替えて使う
#   （説明文・完了ゲート等はテンプレートを唯一の真実の源として維持・再現性を保証）。
#   テンプレート未検出時は簡易 handoff スキーマにフォールバックする。
#
# 安全: DRY_RUN=1 で変更系を表示のみ。出力に作成 issue 番号/URL を表示。
# 規約: .claude/rules/github-ops.md
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$SCRIPT_DIR/lib/proxy-detect.sh"
. "$SCRIPT_DIR/lib/issue-template.sh"

REPO=""; CASE=""; PHASE=""; TITLE=""; FEATURE=""; AREA=""; BLOCKED=""
BODY=""; BODY_FILE=""; CREATE_BRANCH=0; SP_SUFFIX=""
DRY_RUN="${DRY_RUN:-0}"
usage() { sed -n '2,20p' "$0"; exit 1; }

while [ $# -gt 0 ]; do
  case "$1" in
    --repo) REPO="$2"; shift 2 ;;
    --case) CASE="$2"; shift 2 ;;
    --phase) PHASE="$2"; shift 2 ;;
    --title) TITLE="$2"; shift 2 ;;
    --feature) FEATURE="$2"; shift 2 ;;
    --area) AREA="$2"; shift 2 ;;
    --blocked) BLOCKED="$2"; shift 2 ;;
    --body) BODY="$2"; shift 2 ;;
    --body-file) BODY_FILE="$2"; shift 2 ;;
    --create-branch) CREATE_BRANCH=1; shift ;;
    --reopen-suffix) SP_SUFFIX="$2"; shift 2 ;;  # 手戻り: -sp-{m}
    -h|--help) usage ;;
    *) echo "不明な引数: $1" >&2; usage ;;
  esac
done
for v in REPO CASE PHASE TITLE; do [ -n "${!v}" ] || { echo "ERROR: --${v,,} は必須" >&2; usage; }; done

run() { if [ "$DRY_RUN" = "1" ]; then echo "  [dry-run] $*"; else "$@"; fi; }

# phase → 工程ラベル / type / repo区分 / ブランチ接尾辞
case "$PHASE" in
  sa)     KLBL="工程:sa";      TYPE="phase"; RK="case";  BSEG="sa" ;;
  ui)     KLBL="工程:ui";      TYPE="phase"; RK="case";  BSEG="ui" ;;
  ssplan) KLBL="工程:ss-plan"; TYPE="phase"; RK="case";  BSEG="ssplan" ;;
  pgplan) KLBL="工程:pg-plan"; TYPE="phase"; RK="stack"; BSEG="pgplan" ;;
  ptplan) KLBL="工程:pt-plan"; TYPE="phase"; RK="stack"; BSEG="ptplan" ;;
  ss)     KLBL="工程:ss";      TYPE="task";  RK="stack"; BSEG="ss" ;;
  pg)     KLBL="工程:pg";      TYPE="task";  RK="stack"; BSEG="pg" ;;
  pt)     KLBL="工程:pt";      TYPE="task";  RK="stack"; BSEG="pt" ;;
  *) echo "ERROR: 未知の --phase '$PHASE'" >&2; usage ;;
esac
if [ "$TYPE" = "task" ] && [ -z "$FEATURE" ]; then
  echo "ERROR: 実施工程（ss/pg/pt）は --feature bs-001 必須" >&2; usage
fi

# スタックリポ起票の確認ログ（SS/PG/PT/PG-Plan/PT-Plan はスタックリポへ起票する必要がある）
if [ "$RK" = "stack" ]; then
  echo "ℹ スタックリポ（③）への起票: --repo $REPO（$PHASE 工程）"
  echo "  ⚠ 案件リポへの誤起票でないことを確認してください（product-rules.md・github-ops.md §4）"
fi

# ラベル組み立て
LABELS="$KLBL,type:$TYPE,repo:$RK"
[ -n "$AREA" ] && LABELS="$LABELS,area:$AREA"
[ -n "$SP_SUFFIX" ] && LABELS="$LABELS,rework"

# ブランチ名
if [ "$TYPE" = "task" ]; then BRANCH="feature/$CASE-$BSEG-$FEATURE"; else BRANCH="feature/$CASE-$BSEG"; fi
[ -n "$SP_SUFFIX" ] && BRANCH="$BRANCH-$SP_SUFFIX"

# 本文（未指定なら .github/ISSUE_TEMPLATE/{phase}.md をレンダリング。テンプレ未検出時のみ簡易 handoff スキーマにフォールバック）
if [ -z "$BODY_FILE" ] && [ -z "$BODY" ]; then
  [ "$RK" = "case" ] && REPO_LABEL="案件リポ" || REPO_LABEL="スタックリポ"
  PHASE_LABEL=$(issue_phase_label "$PHASE")
  TMPL_NAME=$(issue_template_name "$PHASE")
  REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo ".")
  TMPL_FILE="$REPO_ROOT/.github/ISSUE_TEMPLATE/$TMPL_NAME.md"

  # メタブロック構築（可変部のみ。説明文・完了ゲート等はテンプレート本体をそのまま使う）
  META_BLOCK="## メタ
- 案件キー: $CASE（Milestone）
- 工程: $PHASE_LABEL"
  if [ "$TYPE" = "task" ]; then
    META_BLOCK="$META_BLOCK
- 機能ID: $FEATURE ／ 対象スタック: ${AREA:-未指定}"
  elif [ "$RK" = "stack" ]; then
    META_BLOCK="$META_BLOCK
- 対象スタック: ${AREA:-未指定}"
  fi
  META_BLOCK="$META_BLOCK
- 作業ブランチ: \`$BRANCH\`
- リポ: $REPO_LABEL
- 担当:"
  if [ "$TYPE" = "task" ]; then
    META_BLOCK="$META_BLOCK

## 依存（blocked-by）
- Blocked by: ${BLOCKED:-なし}"
  fi

  if BODY=$(render_issue_template "$TMPL_FILE" "$META_BLOCK"); then
    echo "ℹ 本文テンプレート: $TMPL_FILE"
  else
    echo "⚠ テンプレート未検出（$TMPL_FILE）。簡易 handoff スキーマにフォールバックします" >&2
    BODY=$(cat <<EOF
## メタ
- 案件キー: $CASE（Milestone）
- 工程: $PHASE（type:$TYPE）
- 機能ID: ${FEATURE:-—}／対象スタック: ${AREA:-—}
- 作業ブランチ: $BRANCH
- リポ: $REPO（repo:$RK）

## 上流成果物（着手前に読む）
- リポの docs/・specs/ に push 済みの正本を参照（計画 plan.md・discussion-log も specs/ として push 済み・github-ops §3-B）

## 依存（blocked-by）
- Blocked by: ${BLOCKED:-なし}

## 着手手順
- /issue-init 工程: $PHASE 案件キー: $CASE $([ -n "$FEATURE" ] && echo "機能ID: $FEATURE 対象スタック: $AREA")
- 以降は CLAUDE.md §4 の各工程スキル

## 完了ゲート
- [ ] 成果物（docs/・specs/ とも push 済み）
- [ ] ゲート（PR レビュー・マージ）
EOF
)
  fi
fi

echo "▶ issue 起票: [$PHASE] $TITLE（$REPO・Milestone=$CASE）"
ISSUE_URL=""
if [ "$DRY_RUN" = "1" ]; then
  echo "  [dry-run] gh issue create --repo $REPO --title \"$TITLE\" --label \"$LABELS\" --milestone \"$CASE\" --body <…>"
else
  if [ -n "$BODY_FILE" ]; then
    ISSUE_URL=$(gh issue create --repo "$REPO" --title "$TITLE" --label "$LABELS" --milestone "$CASE" --body-file "$BODY_FILE")
  else
    ISSUE_URL=$(gh issue create --repo "$REPO" --title "$TITLE" --label "$LABELS" --milestone "$CASE" --body "$BODY")
  fi
  echo "  作成: $ISSUE_URL"
fi

# ブランチ作成（--create-branch 指定時。base = feature/{案件キー}）
if [ "$CREATE_BRANCH" = "1" ]; then
  BASE_BRANCH="feature/$CASE"
  if gh api "repos/$REPO/git/ref/heads/$BRANCH" >/dev/null 2>&1; then
    echo "  ブランチ '$BRANCH' は既存"
  else
    SHA=$(gh api "repos/$REPO/git/ref/heads/$BASE_BRANCH" --jq '.object.sha' 2>/dev/null) \
      || { echo "  WARN: 統合ブランチ '$BASE_BRANCH' が無い。先に case-bootstrap.sh を実行" >&2; SHA=""; }
    [ -n "$SHA" ] && run gh api -X POST "repos/$REPO/git/refs" -f ref="refs/heads/$BRANCH" -f sha="$SHA" >/dev/null \
      && echo "  ブランチ '$BRANCH' を作成（base=$BASE_BRANCH）"
  fi
fi
