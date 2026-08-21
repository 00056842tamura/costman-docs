---
name: "[PT-bs-001] 単体テスト-{機能名}"
about: PT 単体テスト工程（type:task・スタックリポ・bs-001 単位）。起票時に「工程:pt」「area:{スタック}」ラベルと Milestone を設定してください。
title: "[PT-bs-001] 単体テスト-{機能名}"
labels: ["type:task", "repo:stack", "工程:pt"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: PT 単体テスト（type:task・スタックリポ）
- 機能ID: <!-- bs-001 --> ／ 対象スタック: <!-- bs / us-api 等 -->
- 作業ブランチ: `feature/{案件キー}-pt-bs-001`
- リポ: スタックリポ
- 担当:

## 依存（blocked-by）
- Blocked by: <!-- #N -->

---

## この工程で担保すること
- bs-001 の全テスト観点（正常系・異常系・バリデーション）に対応するテストコードが存在する
- テスト仕様書・機能要件対比表により、要件・設計・テストの三者整合が確認されている
- 全テストが CI 上でパスしている

## 対応概要
WB・プログラム仕様書・テストシナリオを基に bs-001 のテストコード・テスト仕様書・機能要件対比表を生成する。
`/consistency-check` で三者整合を確認し、`mvn test`（または `npm test`）を実行して全件パスを確認する。

## インプット
- `{スタック}/src/main/`（PG 実装コード）
- `{スタック}/docs/detail-design/`（SS 工程が生成した形式設計書。プログラム仕様書_{機能ID}・メッセージ一覧・該当時は外部IF定義書。境界値・エラーコード・メッセージCDの正本）
- WB（Controller/Service/Repository）（SS の push 済み正本を直接参照。同一スタックリポの統合ブランチ上に既に push されている）
- `docs/base-design/テストシナリオ.md`（push 済み正本を直接参照。テストケースの「観点」の網羅性基準。具体的な期待値は上記のSS詳細設計を正本とする）

## 対応フロー
1. `/issue-init 工程: PT 案件キー: {案件キー} 機能ID: bs-001 対象スタック: {スタック}` を実行
2. `/springer-unit-test-gen`（bs/us-api/us-mpa/batch）または `/reacter-unit-test-gen`（frontend）を実行してテストコード・テスト仕様書・テスト計画書・機能要件対比表を生成する（**妥当性確認実施票は本工程の責務ではない**。テストではなくリリース判定ゲート向けの承認書類であり、PT完了後に人間がリリース前ゲートまでの任意のタイミングで作成する〔生成スキルなし〕）
3. `/blackbox-test-gen` で Controller API テストを生成する（Springer の場合。frontend は `/reacter-unit-test-gen` に含まれるためこのStepは不要）
4. `/consistency-check` で設計書・コード・テストの三者整合を確認する（レポートは `consistency-report.md`。存在しないと `gh pr create`/`docs-to-pr` 自体がブロックされる）
5. `mvn test`（または `npm test`）を実行し、全件パスを確認する（人間が実行）
6. `/docs-to-pr` で工程ゲート PR を作成する

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| テストコード（全クラス） | `{スタック}/src/test/` |
| テスト仕様書 | `{スタック}/docs/unit-test/` |
| テスト計画書 | `{スタック}/docs/unit-test/` |
| 機能要件対比表 | `{スタック}/docs/unit-test/` |
| consistency-report.md（三者整合レビュー結果） | `{スタック}/specs/{案件キー}/unit-test/{issue_id}_{機能ID}/` |

## レビュー対象
- テスト計画書：テスト方針・スコープ・リスク評価の妥当性
- テスト仕様書：テスト観点の網羅性・期待値の正確性
- 機能要件対比表：bs-001 の要件がすべてテストで検証されているか
- テスト実行結果：全件パスの確認

## 完了ゲート
- [ ] テストコードが `{スタック}/src/test/` に生成されている
- [ ] テスト仕様書・テスト計画書・機能要件対比表が `{スタック}/docs/unit-test/` に生成されている
- [ ] `/consistency-check` で三者整合の確認が完了し `consistency-report.md` が `{issue_id}_{機能ID}/` に存在する（無いと `gh pr create` 自体がブロックされる）
- [ ] `mvn test`（または `npm test`）が全件パスしている
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] 工程ゲート PR が作成されている
- [ ] レビュアーの承認を得ている
