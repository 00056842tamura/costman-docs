#!/usr/bin/env bash
# issue-template.sh — .github/ISSUE_TEMPLATE/{name}.md からテンプレート本文を取得し、
#   「## メタ」ブロック（task issue は「## 依存（blocked-by）」含む）を動的値に差し替えて返す。
#
# 設計原則（冪等性・再現性）:
#   - 説明文（この工程で担保すること・対応概要・インプット・対応フロー・アウトプット・
#     cross-phase 成果物・レビュー対象・完了ゲート）は .github/ISSUE_TEMPLATE/ を唯一の真実の源とする。
#   - 可変なのは「## メタ」〜 最初の "---" 行までのみ（案件キー・機能ID・スタック・ブランチ・依存等）。
#   - テンプレート本体を変更すれば、次回起票からその内容が自動的に反映される（手書き本文の二重管理を排除）。
#
# 使い方（source して関数を呼ぶ）:
#   . "$SCRIPT_DIR/lib/issue-template.sh"
#   render_issue_template "$REPO_ROOT/.github/ISSUE_TEMPLATE/ss.md" "$META_BLOCK"
#   戻り値: 0=成功（stdout に本文）／ 1=テンプレート未検出（呼び出し側でフォールバックすること）

render_issue_template() {
  local tmpl_file="$1" meta_block="$2"
  [ -f "$tmpl_file" ] || return 1
  awk -v meta="$meta_block" '
    BEGIN { fm = 0; in_meta = 0; meta_done = 0 }
    NR == 1 && $0 == "---" { fm = 1; next }
    fm == 1 && $0 == "---" { fm = 0; next }
    fm == 1 { next }
    !meta_done && /^## メタ/ { in_meta = 1; print meta; next }
    in_meta && /^---$/ { in_meta = 0; meta_done = 1; next }
    in_meta { next }
    { print }
  ' "$tmpl_file"
}

# phase → テンプレートファイル名（.github/ISSUE_TEMPLATE/ 配下・拡張子なし）
issue_template_name() {
  case "$1" in
    sa) echo "sa" ;; ui) echo "ui" ;;
    ssplan) echo "ss-plan" ;; pgplan) echo "pg-plan" ;; ptplan) echo "pt-plan" ;;
    ss) echo "ss" ;; pg) echo "pg" ;; pt) echo "pt" ;;
    *) return 1 ;;
  esac
}

# phase → テンプレート内「工程: ...」表示文言（テンプレ本文の記述をそのまま踏襲）
issue_phase_label() {
  case "$1" in
    sa)     echo "SA 要件定義（type:phase・案件リポ）" ;;
    ui)     echo "UI 基本設計（type:phase・案件リポ）" ;;
    ssplan) echo "SS-Plan 詳細設計計画（type:phase・案件リポ）" ;;
    pgplan) echo "PG-Plan 実装計画（type:phase・スタックリポ・スタック単位）" ;;
    ptplan) echo "PT-Plan 単体テスト計画（type:phase・スタックリポ・スタック単位）" ;;
    ss)     echo "SS 詳細設計（type:task・スタックリポ）" ;;
    pg)     echo "PG 実装（type:task・スタックリポ）" ;;
    pt)     echo "PT 単体テスト（type:task・スタックリポ）" ;;
    *) return 1 ;;
  esac
}
