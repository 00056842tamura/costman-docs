---
name: "[UI] 基本設計"
about: UI 基本設計工程（type:phase・案件リポ）。起票時に「工程:ui」ラベルと Milestone を設定してください。
title: "[UI] "
labels: ["type:phase", "repo:case", "工程:ui"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: UI 基本設計（type:phase・案件リポ）
- 作業ブランチ: `feature/{案件キー}-ui`
- リポ: 案件リポ
- 担当:

---

## この工程で担保すること
- 要件ID（R###）がスタック×API/画面/ジョブ単位に分解され、機能ID（`{カテゴリ}-{3桁連番}`）が採番されている（後続工程すべての起点となる識別子）
- 画面・API・シーケンス・DBスキーマが設計され、実装・テストのインプットが揃っている
- テストシナリオ（受け入れ条件）が確定し、PT・UAT の検証基準が定義されている
- 実装対象クラス一覧が確定し、SS-Plan での棚卸しが可能な粒度になっている

## 対応概要
SA で確定した要件（要件ID）をスタック×API/画面/ジョブ単位に分解し、機能ID（`{カテゴリ}-{3桁連番}`）を採番したうえで画面・API・シーケンス・DBスキーマ・ロバストネス分析を設計する。
壁打ちを通じてテーブル一覧・テーブル定義書・実装対象クラス一覧・テストシナリオ・ロバストネス図・クラス図・画面状態遷移図を確定させ、`docs/base-design/` へ正式成果物として直接生成する。

## インプット
- `docs/requirements/`（SA 成果物・push 済み正本）
- `docs/requirements/要件一覧.md`（R### 一覧）
- `docs/base-design/機能一覧.md`（既存の機能ID（`{カテゴリ}-{3桁連番}`）一覧・重複確認のため参照）

## 対応フロー
1. `/issue-init 工程: UI 案件キー: {案件キー}` を実行
2. `issue-to-design` に従い Claude と壁打ちして設計を深掘りする（Step 7 で要件ID→機能ID分解方針を確認）
3. discussion-log にテーブル一覧・テーブル定義書・実装対象クラス一覧・テストシナリオ・ロバストネス図・クラス図・画面状態遷移図の内容を確定する
4. 判断事項を `ADR-UI-{n}`（連番）として記録する
5. `/basic-design-gen` を実行し、要件IDをスタック×API/画面/ジョブ単位に分解して機能ID（`{カテゴリ}-{3桁連番}`）を採番し、基本設計書群（上記文書すべて含む）を `docs/base-design/` に直接生成する
6. `/basic-design-review` を実行してレビューし、`ADR-UI-{n}` に判断事項を記録する（finding 0件でも1件は必ず起票）
7. `/docs-to-pr` で工程ゲート PR を作成する

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| 機能一覧.md（機能ID〔`{カテゴリ}-{3桁連番}`〕採番台帳・要件IDとの対応表） | `docs/base-design/` |
| Web_API_IF一覧表.md | `docs/base-design/` |
| Web_API_IF定義書（機能ごと・1機能ID=1API） | `docs/base-design/` |
| シーケンス図 | `docs/base-design/` |
| 機能概要.md | `docs/base-design/` |
| テーブル一覧.md（テーブルID採番台帳・bs/batchのみ） | `docs/base-design/` |
| テーブル定義書（テーブルごと・bs/batchのみ・**正本**） | `docs/base-design/` |
| 実装対象クラス一覧.md（UI issue単位で累積） | `docs/base-design/` |
| テストシナリオ.md（UI issue単位で累積） | `docs/base-design/` |
| ロバストネス図_{要件ID}.md（要件IDごと・ユースケース単位） | `docs/base-design/` |
| クラス図.md（案件共通・累積） | `docs/base-design/` |
| 画面状態遷移図_{機能ID}.md（機能IDごと・frontendのみ） | `docs/base-design/` |
| review-report.md（レビュー結果） | `specs/{案件キー}/base-design/{issue_id}/` |
| ADR-UI-{n} | `specs/{案件キー}/base-design/{issue_id}/` |

## レビュー対象
- `docs/base-design/機能一覧.md`：機能ID（`{カテゴリ}-{3桁連番}`）の網羅性・要件IDとの対応・既存 API ID/ジョブID との対応
- `docs/base-design/Web_API_IF一覧表.md`：API の網羅性・命名・ステータスコード
- `docs/base-design/Web_API_IF定義書（機能ごと）`：リクエスト/レスポンスの型・バリデーション・エラーコード詳細
- `docs/base-design/シーケンス図`：正常系・異常系のフロー
- `docs/base-design/テーブル一覧.md`：テーブルID（T###）の網羅性・重複がないか
- `docs/base-design/テーブル定義書`：カラム定義・制約・インデックスの妥当性（SS工程では変更されないため念入りに確認）
- `docs/base-design/テストシナリオ.md`：受け入れ条件の網羅性・曖昧さの排除
- `docs/base-design/実装対象クラス一覧.md`：SS-Plan での棚卸しに直結するため命名・粒度・網羅性
- `docs/base-design/ロバストネス図_{要件ID}.md`：Boundary/Control/Entity分析の妥当性
- `docs/base-design/クラス図.md`：ドメインモデルの整合性
- `docs/base-design/画面状態遷移図_{機能ID}.md`（frontendのみ）：state・遷移契機の網羅性

## 完了ゲート
- [ ] 機能一覧.md に要件IDから分解した機能ID（`{カテゴリ}-{3桁連番}`）が採番されている
- [ ] Web_API_IF一覧表.md が生成されている
- [ ] テーブル一覧・テーブル定義書・実装対象クラス一覧・テストシナリオが `docs/base-design/` に生成されている
- [ ] `/basic-design-review` のレビュー結果（review-report.md）が反映されている
- [ ] 工程ゲート PR が作成されている
- [ ] ADR-UI-{n} が本PRに含まれている（push 済み）
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] レビュアーの承認を得ている
