---
name: "[PG-Plan] 実装計画"
about: PG-Plan 実装計画工程（type:phase・スタックリポ・スタック単位）。起票時に「工程:pg-plan」「area:{スタック}」ラベルと Milestone を設定してください。
title: "[PG-Plan] "
labels: ["type:phase", "repo:stack", "工程:pg-plan"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: PG-Plan 実装計画（type:phase・スタックリポ・スタック単位）
- 対象スタック: <!-- bs / us-api 等 -->
- 作業ブランチ: `feature/{案件キー}-pgplan`
- リポ: スタックリポ
- 担当:

---

## この工程で担保すること
- スタック内の全機能ID（`{カテゴリ}-{3桁連番}`）について実装 sub-issue が棚卸しされており、着手順序と依存が確定している
- クロススタック順序（SS-Plan の blocked-by）が PG sub-issue に継承されている
- PG 担当者が単独で着手できるチケットが揃っている

## 対応概要
SS 成果物（設計書群）を基に PG sub-issue を棚卸しし、実装順序・クラス間依存を plan.md に記録する。
plan.md は本工程のブランチに push し、PR レビューで承認を得た上で PG sub-issue を先行起票する。

## インプット
- `{スタック}/docs/detail-design/`（SS 成果物・push 済み正本）
- SS-Plan の plan.md（クロススタック依存の参照元）

## 対応フロー
1. `/issue-init 工程: PG-Plan 案件キー: {案件キー} 対象スタック: {スタック}` を実行
2. `/issue-plan 工程: PG-Plan` を実行して plan.md を作成する（実装 sub-issue 棚卸し・実装順序・依存）。**Step 3.6 で対象スタックの全機能ID分の SS成果物（外部IF定義書・プログラム仕様書・メッセージ一覧・WB）を横断してCPC-1/CPC-2の観点で確認する（本チェックは plan.md 生成をブロックする。不整合発見時は手戻り連携手順を適用）**
3. SS-Plan の blocked-by をクロススタック実装順序として plan.md に継承する
4. plan.md を含む specs 配下の変更を本工程のブランチに push し、PR を作成する（`docs-to-pr.md`）
5. レビュアーによる PR 承認・マージを得る
6. マージ後、PG sub-issue を先行起票する（`[PG-{機能ID}] 実装-{機能名}`・blocked-by 付き）

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| plan.md（実装順序・依存一覧） | `{スタック}/specs/{案件キー}/implementation-plan/{issue_id}/` |
| PG sub-issue（全機能ID） | スタックリポ（GitHub issues） |

## レビュー対象
- plan.md：実装順序の妥当性・依存関係の正確性・漏れ
- PG sub-issue 一覧：全機能IDが揃っているか
- Step 3.6 の工程内整合性チェック結果（全機能ID横断のCPC-1/CPC-2確認・discussion-log記録）

## 完了ゲート
- [ ] plan.md が本PRに含まれている（push 済み・マージ済み）
- [ ] Step 3.6 の工程内整合性チェック（全機能ID横断・CPC-1/CPC-2）が完了し discussion-log に記録されている
- [ ] PG sub-issue が全機能ID分起票されている
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] PR レビュー承認・マージ済み
