<!-- SS-Plan PR テンプレート（詳細設計計画・案件リポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- 当該計画工程で棚卸しした sub-issue（機能ID単位）の範囲・方針 -->

## 成果物
- specs/{{CASE}}/detail-design-plan/{issue_id}/plan.md
- discussion-log.md（同ディレクトリ）

## sub-issue 棚卸し（機能ID単位・レビュー対象）
<!-- plan.md の「## sub-issue 棚卸し」テーブルの要約 -->

## 影響スタック
{{STACK_BLOCK}}

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] 棚卸しが機能ID単位で漏れなく列挙されているか（`docs/base-design/機能一覧.md` と整合）
- [ ] 依存（blocked-by）・並走可否が妥当か（スタック間 I/F の確定順）
- [ ] 優先順位・着手順が妥当か
- [ ] discussion-log の未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている
