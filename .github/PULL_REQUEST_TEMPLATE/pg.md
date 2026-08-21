<!-- PG PR テンプレート（実装・スタックリポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- プログラム仕様書_{機能ID} の要点・本 PR の実装範囲 -->

## 成果物
- コード: {スタック}/src/main/（Java / Mapper XML / Flyway SQL / TypeScript 等）
- ※ docs 生成なし（プログラム仕様書・メッセージ一覧・コンポーネント仕様書はすべて SS 工程の `{スタック}/docs/detail-design/` に生成済み）

## テスト結果
<!-- PG 工程は N/A（単体テストは PT 工程で実施） -->
- N/A

## 影響スタック
{{STACK_BLOCK}}

## 検討の記録（specs・本 PR に含めて push）
- 実装作業メモ・ADR-PG-{n}（あれば）・レビューレポート（backend/batch: springer-review-report.md／frontend: reacter-code-review-report.md）: {スタック}/specs/{{CASE}}/implementation/{issue_id}_{機能ID}/

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] レビュー実施記録（`springer-review-report.md` または `reacter-code-review-report.md`）が存在する（無いと `gh pr create` 自体がブロックされる）
- [ ] `/springer-review-fix`（backend/batch）で未対応指摘が 0 件、または `/reacter-code-review`（frontend）の指摘を手動解消済み
- [ ] 詳細設計（{スタック}/docs/detail-design/）と実装が整合しているか
- [ ] 単体テストは工程8 PT で実施（本 PR は実装＋実装docs）
- [ ] discussion-log/ADRの未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（次工程への持ち越しがある場合は次工程issueの上流成果物節に転記済み）
