---
name: "[PT-Plan] 単体テスト計画"
about: PT-Plan 単体テスト計画工程（type:phase・スタックリポ・スタック単位）。起票時に「工程:pt-plan」「area:{スタック}」ラベルと Milestone を設定してください。
title: "[PT-Plan] "
labels: ["type:phase", "repo:stack", "工程:pt-plan"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: PT-Plan 単体テスト計画（type:phase・スタックリポ・スタック単位）
- 対象スタック: <!-- bs / us-api 等 -->
- 作業ブランチ: `feature/{案件キー}-ptplan`
- リポ: スタックリポ
- 担当:

---

## この工程で担保すること
- テスト対象クラスと観点（正常系・異常系・バリデーション）が網羅的に定義されている
- テストシナリオとの対応が明確になっており、PT で何を検証するかが合意されている
- PT 担当者が単独で着手できるチケットが揃っている

## 対応概要
PG 成果物（実装コード）と `docs/base-design/テストシナリオ.md` を基に PT sub-issue を棚卸しし、テスト対象・観点を plan.md に記録する。
plan.md は本工程のブランチに push し、PR レビューで承認を得る。

## インプット
- `{スタック}/src/main/`（PG 実装コード・push 済み正本）
- `{スタック}/docs/detail-design/`（プログラム仕様書・SS 工程生成済み）
- `docs/base-design/テストシナリオ.md`（push 済み正本を直接参照）

## 対応フロー
1. `/issue-init 工程: PT-Plan 案件キー: {案件キー} 対象スタック: {スタック}` を実行
2. `docs/base-design/テストシナリオ.md` を読み込む
3. `/issue-plan 工程: PT-Plan` を実行して plan.md を作成する（テスト対象クラス・観点一覧）。**Step 3.6 で対象スタックの全機能ID分の PG実装コード（`{スタック}/src/main/`）を横断してCPC-1/CPC-2の観点で確認する（本チェックは plan.md 生成をブロックする。不整合発見時は手戻り連携手順を適用）**
4. plan.md を含む specs 配下の変更を本工程のブランチに push し、PR を作成する（`docs-to-pr.md`）
5. レビュアーによる PR 承認・マージを得る
6. マージ後、PT sub-issue を先行起票する（`[PT-{機能ID}] 単体テスト-{機能名}`）

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| plan.md（テスト対象・観点一覧） | `{スタック}/specs/{案件キー}/unit-test-plan/{issue_id}/` |
| PT sub-issue（全機能ID） | スタックリポ（GitHub issues） |

## レビュー対象
- plan.md：テスト観点の網羅性（正常系・異常系・バリデーション）・テストシナリオとの対応
- Step 3.6 の工程内整合性チェック結果（全機能ID横断のCPC-1/CPC-2確認・discussion-log記録）

## 完了ゲート
- [ ] plan.md が本PRに含まれている（push 済み・マージ済み）
- [ ] Step 3.6 の工程内整合性チェック（全機能ID横断・CPC-1/CPC-2）が完了し discussion-log に記録されている
- [ ] PT sub-issue が全機能ID分起票されている
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] PR レビュー承認・マージ済み
