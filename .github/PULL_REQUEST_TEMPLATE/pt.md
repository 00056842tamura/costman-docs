<!-- PT PR テンプレート（単体テスト・スタックリポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- テスト仕様書_{機能ID} の要点・本 PR のテスト範囲 -->

## 成果物
- テストコード: {スタック}/src/test/（ホワイトボックス/ブラックボックス/E2E・初期データSQL）
- テストdocs: {スタック}/docs/unit-test/テスト仕様書_{機能ID}.md・テスト計画書.md・機能要件対比表.md

## テスト結果（人間確認済み）
- mvn test / npm test: {N}件 全件パス（実行日・実行者）

## 影響スタック
{{STACK_BLOCK}}

## 検討の記録（specs・本 PR に含めて push）
- テスト作業メモ・consistency-check レポート: {スタック}/specs/{{CASE}}/unit-test/{issue_id}_{機能ID}/

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] `consistency-report.md` が存在する（無いと `gh pr create` 自体がブロックされる）
- [ ] テストシナリオの受け入れ条件を全件カバー（機能要件対比表で確認）
- [ ] 三者整合（/consistency-check）済み・未整合なし
- [ ] テスト全件パス（人間確認済み）
- [ ] discussion-log/ADRの未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（次工程への持ち越しがある場合は次工程issueの上流成果物節に転記済み）
