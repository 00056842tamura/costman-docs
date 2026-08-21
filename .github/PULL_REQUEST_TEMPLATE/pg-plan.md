<!-- PG-Plan PR テンプレート（実装計画・スタックリポ・スタック単位）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- 当該計画工程で棚卸しした sub-issue（機能ID単位）の範囲・実装順序・依存 -->

## 成果物
- {スタック}/specs/{{CASE}}/implementation-plan/{issue_id}/plan.md
- discussion-log.md（同ディレクトリ）

## sub-issue 棚卸し（機能ID単位・レビュー対象）
<!-- plan.md の「## sub-issue 棚卸し」テーブルの要約（実装順序・依存を含む） -->

## 影響スタック
{{STACK_BLOCK}}

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] 棚卸しが機能ID単位で漏れなく列挙されているか（SS 成果物と整合）
- [ ] 実装順序・依存（blocked-by）が妥当か（クロススタック順序は SS-Plan の依存を継承）
- [ ] 優先順位・着手順が妥当か
- [ ] discussion-log の未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている
