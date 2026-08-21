#!/usr/bin/env bash
# pr-template.sh — .github/PULL_REQUEST_TEMPLATE/{name}.md を読み込み、
#   {{CASE}}・{{PHASE_LINE}}・{{ISSUE_LINE}}・{{STACK_BLOCK}} を動的値に置換して返す。
#
# 設計原則:
#   - PR 本文は issue 本文と異なり「概要・成果物・意思決定」等の自由記述部分の比率が高いため、
#     issue-template.sh の render_issue_template()（「## メタ」ブロックのみ差し替え）とは異なり、
#     機械的プレースホルダーのみを置換し、それ以外の本文（見出し構造・自由記述欄・チェック
#     リスト）はテンプレートファイルをそのまま読み込む。
#   - 見出し構造（特に「## チェック」）は .github/PULL_REQUEST_TEMPLATE/{phase}.md を
#     唯一の真実の源とする。AI が --body-file で自由記述部分を埋める場合も、この見出しを
#     削除・改変してはならない（運用ルール。スクリプト側では強制できない）。
#
# 使い方（source して関数を呼ぶ）:
#   . "$SCRIPT_DIR/lib/pr-template.sh"
#   render_pr_template "$REPO_ROOT/.github/PULL_REQUEST_TEMPLATE/ss.md" "$CASE" "$PHASE_LINE" "$ISSUE_LINE" "$STACK_CONTENT"
#   戻り値: 0=成功（stdout に本文）／ 1=テンプレート未検出（呼び出し側でフォールバックすること）

render_pr_template() {
  local tmpl_file="$1" case_key="$2" phase_line="$3" issue_line="$4" stack_content="$5"
  [ -f "$tmpl_file" ] || return 1
  local body
  body=$(sed \
    -e "s/{{CASE}}/$case_key/g" \
    -e "s/{{PHASE_LINE}}/$phase_line/g" \
    -e "s|{{ISSUE_LINE}}|${issue_line:-（issue 番号なし）}|g" \
    "$tmpl_file")
  printf '%s' "$body" | awk -v sb="$stack_content" '
    /\{\{STACK_BLOCK\}\}/ { print sb; next }
    { print }
  '
}

# phase → テンプレートファイル名（.github/PULL_REQUEST_TEMPLATE/ 配下・拡張子なし）
# issue_template_name()（issue-template.sh）と同一マッピングだが、意味的に別レイヤー
# （issue用/PR用）のテンプレート解決であり、check_backlog_gate.py・check_review_gate.py が
# BRANCH_PATTERN を意図的に複製している前例（個別実装で十分というユーザー判断）に倣い、
# 共有化はせず複製する。
pr_template_name() {
  case "$1" in
    sa) echo "sa" ;; ui) echo "ui" ;;
    ssplan) echo "ss-plan" ;; pgplan) echo "pg-plan" ;; ptplan) echo "pt-plan" ;;
    ss) echo "ss" ;; pg) echo "pg" ;; pt) echo "pt" ;;
    *) return 1 ;;
  esac
}
