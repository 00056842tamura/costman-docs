#!/usr/bin/env bash
# subissue-bulk.sh — 計画工程の棚卸し（plan.md）から sub-issue を一括「先行起票」または「本文更新」する
#   先行起票はブランチを作らない（着手時に /issue-init が作る）。blocked-by を本文へ転記。
#
# 使い方:
#   subissue-bulk.sh --case {案件キー} --phase {ss|pg|pt} --tsv {file} [--repo-default {owner}/{repo}]
#                    [--plan {plan.md}] [--plan-issue {GitHub URL}] [--update]
#
# TSV 形式（タブ区切り・1 行 1 sub-issue。# 始まりとブランク行は無視）:
#   {機能ID}\t{スタック/area}\t{owner/repo}\t{タイトル}\t{blocked-by 例:"#12 #15"（任意）}
#   ※ --repo-default を指定すれば TSV の 3 列目（repo）は空欄でも可
#
# オプション:
#   --plan {file}      plan.md を読んで「計画情報（抜粋）」を issue body に追加する
#                      plan.md の ## sub-issue 棚卸し テーブルから 機能ID×スタック の行を抽出する
#   --plan-issue {URL} plan.md を含む計画工程 PR（マージ済み）または計画工程 issue の URL（参考リンクとして掲載。--plan 指定時のみ有効）
#                      plan.md 自体は push 済みでリポジトリの正本のため、この URL は文脈確認用の補助リンクにすぎない
#   --update           新規起票ではなく既存 open issue の本文を更新する（plan.md 更新後に使用）
#                      ⚠ open 状態の issue のみ更新する（close 済みは対象外）
#
# 本文: .github/ISSUE_TEMPLATE/{ss|pg|pt}.md を読み込み、「## メタ」ブロックのみ動的値に差し替える
#   （説明文・完了ゲート等はテンプレートを唯一の真実の源として維持）。
#   plan.md の計画情報（抜粋）は「## 完了ゲート」の直前に挿入する。
#   テンプレート未検出時は簡易 handoff スキーマにフォールバックする。
#
# 安全: DRY_RUN=1 で変更系を表示のみ。
# 規約: .claude/rules/github-ops.md / 計画工程: issue-plan / docs-to-pr（PR）
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$SCRIPT_DIR/lib/proxy-detect.sh"
. "$SCRIPT_DIR/lib/issue-template.sh"

CASE=""; PHASE=""; TSV=""; REPO_DEFAULT=""; PLAN_FILE=""; PLAN_ISSUE_URL=""; UPDATE_MODE=0
DRY_RUN="${DRY_RUN:-0}"
HERE="$(cd "$(dirname "$0")" && pwd)"
usage() { sed -n '2,20p' "$0"; exit 1; }

while [ $# -gt 0 ]; do
  case "$1" in
    --case)       CASE="$2";           shift 2 ;;
    --phase)      PHASE="$2";          shift 2 ;;
    --tsv)        TSV="$2";            shift 2 ;;
    --repo-default) REPO_DEFAULT="$2"; shift 2 ;;
    --plan)       PLAN_FILE="$2";      shift 2 ;;
    --plan-issue) PLAN_ISSUE_URL="$2"; shift 2 ;;
    --update)     UPDATE_MODE=1;       shift   ;;
    -h|--help) usage ;;
    *) echo "不明な引数: $1" >&2; usage ;;
  esac
done
for v in CASE PHASE TSV; do [ -n "${!v}" ] || { echo "ERROR: --${v,,} は必須" >&2; usage; }; done
[ -f "$TSV" ] || { echo "ERROR: TSV が見つからない: $TSV" >&2; exit 1; }
case "$PHASE" in ss|pg|pt) ;; *) echo "ERROR: --phase は ss|pg|pt（実施工程の先行起票）" >&2; usage ;; esac
if [ -n "$PLAN_FILE" ] && [ ! -f "$PLAN_FILE" ]; then
  echo "ERROR: plan.md が見つからない: $PLAN_FILE" >&2; exit 1
fi

# plan.md の ## sub-issue 棚卸し テーブルから 機能ID×スタック の行を抽出する
# 列順: | # | スタック | 機能ID | sub-issue | 成果物 | blocked-by | 優先・並走 | 備考 |
# ⚠ この列順・セクション名は変更禁止（specs/templates/plan.md に変更禁止コメントあり）
# 戻り値: "成果物|優先・並走|備考"（該当なしは空文字）
extract_plan_row() {
  local feature="$1" area="$2"
  [ -z "$PLAN_FILE" ] && return 0
  awk -v feat="$feature" -v area="$area" '
    /^## sub-issue 棚卸し/ { in_section=1; next }
    /^## / && in_section   { in_section=0 }
    !in_section            { next }
    /^\|[-: ]+\|/          { next }
    /\|/ {
      n = split($0, c, "|")
      if (n < 9) next
      stack = c[3]; gsub(/^[[:space:]]+|[[:space:]]+$/, "", stack)
      fid   = c[4]; gsub(/^[[:space:]]+|[[:space:]]+$/, "", fid)
      if (stack == area && fid == feat) {
        artifact = c[6]; gsub(/^[[:space:]]+|[[:space:]]+$/, "", artifact)
        priority = c[8]; gsub(/^[[:space:]]+|[[:space:]]+$/, "", priority)
        note     = c[9]; gsub(/^[[:space:]]+|[[:space:]]+$/, "", note)
        print artifact "|" priority "|" note
        exit
      }
    }
  ' "$PLAN_FILE"
}

PFX=$(echo "$PHASE" | tr '[:lower:]' '[:upper:]')
PHASE_LABEL=$(issue_phase_label "$PHASE")
TMPL_NAME=$(issue_template_name "$PHASE")
REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo ".")
TMPL_FILE="$REPO_ROOT/.github/ISSUE_TEMPLATE/$TMPL_NAME.md"
n=0
while IFS=$'\t' read -r FEATURE AREA REPO TITLE BLOCKED || [ -n "${FEATURE:-}" ]; do
  [ -z "${FEATURE// /}" ] && continue
  case "$FEATURE" in \#*) continue ;; esac
  REPO="${REPO:-$REPO_DEFAULT}"
  [ -n "$REPO" ] || { echo "  SKIP（repo 不明）: $FEATURE $TITLE" >&2; continue; }
  BLOCKED="${BLOCKED:-}"

  # plan.md から計画情報を取得
  PLAN_ARTIFACT=""; PLAN_PRIORITY=""; PLAN_NOTE=""
  if [ -n "$PLAN_FILE" ]; then
    PLAN_ROW=$(extract_plan_row "$FEATURE" "$AREA")
    if [ -n "$PLAN_ROW" ]; then
      IFS='|' read -r PLAN_ARTIFACT PLAN_PRIORITY PLAN_NOTE <<< "$PLAN_ROW"
    fi
  fi

  # 計画情報（抜粋）セクション（--plan 指定時のみ追加・「## 完了ゲート」の直前に挿入）
  if [ -n "$PLAN_FILE" ]; then
    PLAN_SECTION=$(printf '## 計画情報（plan.md より抜粋）\n<!-- plan.md はリポジトリに push 済みの正本です。詳細・最新版は plan.md を直接参照してください（参考リンク: %s）。\n     plan.md 更新後は subissue-bulk.sh --update で本文を同期できます。-->\n- 成果物: %s\n- 優先・並走: %s\n- 備考: %s\n' \
      "${PLAN_ISSUE_URL:-plan.md 参照}" \
      "${PLAN_ARTIFACT:-（plan.md 参照）}" \
      "${PLAN_PRIORITY:-（plan.md 参照）}" \
      "${PLAN_NOTE:--}")
  else
    PLAN_SECTION=""
  fi

  ISSUE_TITLE="[$PFX-$FEATURE] $AREA $TITLE"

  # メタブロック構築（可変部のみ。説明文・完了ゲート等はテンプレート本体をそのまま使う）
  META_BLOCK="## メタ
- 案件キー: $CASE（Milestone）
- 工程: $PHASE_LABEL
- 機能ID: $FEATURE ／ 対象スタック: $AREA
- 作業ブランチ: \`feature/$CASE-$PHASE-$FEATURE\`（着手時に /issue-init が作成）
- リポ: $REPO（repo:stack）
- 担当:

## 依存（blocked-by）
- Blocked by: ${BLOCKED:-なし}"

  if TMPL_BODY=$(render_issue_template "$TMPL_FILE" "$META_BLOCK"); then
    if [ -n "$PLAN_SECTION" ]; then
      # 「## 完了ゲート」の直前にスナップショットを挿入
      BODY=$(printf '%s\n' "$TMPL_BODY" | awk -v snap="$PLAN_SECTION" '
        /^## 完了ゲート/ { print snap; print; next }
        { print }
      ')
    else
      BODY="$TMPL_BODY"
    fi
  else
    echo "  ⚠ テンプレート未検出（$TMPL_FILE）。簡易 handoff スキーマにフォールバックします" >&2
    BODY=$(cat <<EOF
## メタ
- 案件キー: $CASE（Milestone）
- 工程: $PHASE（type:task・先行起票）
- 機能ID: $FEATURE／対象スタック: $AREA
- 作業ブランチ: feature/$CASE-$PHASE-$FEATURE（着手時に /issue-init が作成）
- リポ: $REPO（repo:stack）

## 上流成果物（着手前に読む）
- リポの docs/（${AREA}/docs/...）に push 済みの正本を参照

## 依存（blocked-by）
- Blocked by: ${BLOCKED:-なし}

## 着手手順
- /issue-init 工程: $PHASE 案件キー: $CASE 機能ID: $FEATURE 対象スタック: $AREA
- 以降は CLAUDE.md §4 の工程$([ "$PHASE" = ss ] && echo "4 SS" || { [ "$PHASE" = pg ] && echo "6 PG" || echo "8 PT"; }) スキル

## 完了ゲート
- [ ] 成果物（${AREA}/docs/・${AREA}/src/）
- [ ] スタックリポ PR${PLAN_SECTION}
EOF
)
  fi

  if [ "$UPDATE_MODE" = "1" ]; then
    echo "[$PFX-$FEATURE] $AREA $TITLE → $REPO（更新モード）"
    ISSUE_NUM=$(gh issue list --repo "$REPO" --milestone "$CASE" \
      --label "工程:${PHASE}" --state open \
      --search "$ISSUE_TITLE" --json number,title \
      --jq ".[] | select(.title == \"$ISSUE_TITLE\") | .number" 2>/dev/null | head -1 || true)
    if [ -z "$ISSUE_NUM" ]; then
      echo "  ⚠ open issue が見つからない: $ISSUE_TITLE（スキップ）" >&2
      continue
    fi
    if [ "$DRY_RUN" = "1" ]; then
      echo "  [dry-run] gh issue edit $ISSUE_NUM --repo $REPO --body ..."
    else
      gh issue edit "$ISSUE_NUM" --repo "$REPO" --body "$BODY"
      echo "  ✏ issue #$ISSUE_NUM を更新しました"
    fi
  else
    echo "$ISSUE_TITLE → $REPO"
    "$HERE/issue-open.sh" --repo "$REPO" --case "$CASE" --phase "$PHASE" \
      --feature "$FEATURE" --area "$AREA" \
      --title "$ISSUE_TITLE" --body "$BODY"
  fi
  n=$((n+1))
done < "$TSV"

if [ "$UPDATE_MODE" = "1" ]; then
  echo "✅ $n 件の sub-issue を更新しました。"
else
  echo "✅ $n 件の sub-issue を先行起票しました（ブランチは着手時に作成）。"
  echo "   blocked-by が解消した issue から担当者がアサインして着手できます（gh issue list --milestone $CASE --label 工程:$PHASE --state open）。"
fi
