---
name: "[SS-Plan] 詳細設計計画"
about: SS-Plan 詳細設計計画工程（type:phase・案件リポ）。起票時に「工程:ss-plan」ラベルと Milestone を設定してください。
title: "[SS-Plan] "
labels: ["type:phase", "repo:case", "工程:ss-plan"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: SS-Plan 詳細設計計画（type:phase・案件リポ）
- 作業ブランチ: `feature/{案件キー}-ssplan`
- リポ: 案件リポ
- 担当:

---

## この工程で担保すること
- UI 工程で確定済みの全機能ID（`{カテゴリ}-{3桁連番}`）が sub-issue として漏れなく棚卸しされている（スタックへの分解は UI 工程で完了済みのため本工程では再分解しない）
- スタック間の実装依存（blocked-by）が明示され、SS 着手順序が決まっている
- SS sub-issue が全量先行起票されており、担当者が単独で着手できる状態になっている

## 対応概要
UI 成果物（`docs/base-design/機能一覧.md` で確定済みの機能ID）を基に SS sub-issue の棚卸しを行い、plan.md に記録する。
クロススタック依存（us-api blocked-by bs 等）を確定し、SS sub-issue を全量先行起票する。
成果物（plan.md）は本工程のブランチに push し、PR レビューで承認を得る。

## インプット
- `docs/base-design/`（UI 成果物・push 済み正本。機能一覧・テーブル定義書・実装対象クラス一覧を含む）

## 対応フロー
1. `/issue-init 工程: SS-Plan 案件キー: {案件キー}` を実行
2. `docs/base-design/実装対象クラス一覧.md` を読み込む（push 済み正本を直接参照）
3. `/issue-plan 工程: SS-Plan` を実行して plan.md を作成する（機能ID単位の棚卸し・依存記録）。**Step 3.5 で `.claude/repositories.local.md`「## スタックリポ」の記入・clone を確認する（空欄・未cloneなら本ステップで停止・plan.md 生成不可）。`/stack-init` 未実行スタックがあれば完了報告で案内する（こちらは plan.md 生成をブロックしない）**
4. plan.md を含む specs 配下の変更を本工程のブランチに push し、PR を作成する（`docs-to-pr.md`）
5. レビュアーによる PR 承認・マージを得る
6. マージ後、SS sub-issue を全量先行起票する（スタックリポに `[SS-{機能ID}] 詳細設計-{機能名}` で起票・blocked-by 付き）

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| plan.md（機能ID単位の棚卸し・依存一覧） | `specs/{案件キー}/detail-design-plan/{issue_id}/` |
| SS sub-issue（全機能ID） | スタックリポ（GitHub issues） |

## レビュー対象
- plan.md：機能ID棚卸しの漏れ・blocked-by 関係の正確性
- SS sub-issue 一覧：全機能IDが揃っているか・blocked-by の記載
- Step 3.5 のスタック構築状況確認結果（`.claude/repositories.local.md` 記入・clone・`/stack-init` 実行状況）

## 完了ゲート
- [ ] plan.md が本PRに含まれている（push 済み・マージ済み）
- [ ] 対象スタック全てで `.claude/repositories.local.md`「## スタックリポ」の記入・clone が完了している（Step 3.5）
- [ ] SS sub-issue が全機能ID分起票されている
- [ ] us-api 系 sub-issue に `Blocked by: #{bs issue}` が記載されている
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] PR レビュー承認・マージ済み
