# 手戻り影響レポート（sp-001）

## 発覚経緯
- 発覚工程: PT-Plan（`/issue-plan 工程: PT-Plan 案件キー: costman-2026-001 issue-id: 1`）
- 発覚日: 2026-08-21
- Step 3.6相当の確認（PT実装対象コードの横断確認）中に、PT工程の入力となる `docs/base-design/テストシナリオ.md`（UI工程の正本）の記載内容と、実際のPG実装（`frontend/src/`）を突き合わせて発見。

> 注記: `docs/base-design/テストシナリオ.md`・`docs/base-design/機能一覧.md`・`frontend/src/features/`（PG実装）はいずれも `/case-init` 実施前（旧設計書からのAI移行作業）に生成されたものであり、UI issue・PG issueが正規に起票・レビューされていない。そのため本不整合は「工程が並走して生じた食い違い」ではなく、正規工程を経ずに生成された成果物間の食い違いである。

## 不整合の内容

### 1. `convertHiddenRow`（UT-3）
- `docs/base-design/テストシナリオ.md` ユニットテスト観点 UT-3:
  「`utils/commonUtil.convertHiddenRow` — 実算行（`yojitsuFlg`が実算）で値を渡すと、計画行と重複する項目（部署/社員番号/氏名）を空文字に変換して返す（非表示行判定）」
- 実態: `frontend/src/utils/commonUtil.ts` に `convertHiddenRow` は存在しない。`frontend/src/features/kobanbetsuShushi/utils/shushiListUtil.ts` にも同等ロジックは見つからない（`grep` で該当箇所なし）。
- 対応機能ID: front-intra-004（工番別収支データ参照画面）

### 2. `axiosErrorHandling`（UT-9〜11）
- テストシナリオ記載: `hooks/useErrorHandling.axiosErrorHandling` が、(a) Axiosエラーでない例外でシステムエラートースト、(b) 401時に3秒後自動ログアウト、(c) バリデーションエラー時に `detail[].ValidationMessage` を改行結合してトースト表示、という3つの挙動を持つ。
- 実態: `frontend/src/hooks/useErrorHandling.ts` は `useErrorHandling()` フックが `{ handleError }` を返す構成（関数名が異なる）。(a)(b)相当の挙動は実装されているが、(c)の `detail[].ValidationMessage` 結合表示ロジックは実装されておらず、`error.response?.data.message` を単純表示するのみ。
- 対応機能ID: front-intra-002, 004, 005, 006, 007（`useErrorHandling` 利用箇所全て）

## 影響範囲
- **UI**: `docs/base-design/テストシナリオ.md` のUT-3・UT-9〜11の記載が、現行実装を正しく反映していない可能性（実装ミスではなくテストシナリオの記載が旧設計書ベースで古い可能性を含む）
- **PG**: 上記の関数・挙動が本来必要な仕様であれば、`frontend/src/utils/commonUtil.ts`（`convertHiddenRow`）・`frontend/src/hooks/useErrorHandling.ts`（バリデーションエラー時の`detail[]`結合表示）の実装追加が必要
- **PT**: 本不整合が解消されるまで、PT-Plan（issue-1）のsub-issue棚卸しでは、UT-3・UT-9〜11に相当するテスト対象は「実装に存在するものに限定」して棚卸しし、存在しない関数・挙動はテスト対象から除外する（`plan.md`に注記）

## 推奨対応（次アクション・未実施）
1. どちらが正か（テストシナリオが古い／実装が未完了）を判断する調査を行う（`old_docs/`の旧実装コードを確認するのが近道）
2. 判断結果に応じて:
   - テストシナリオが誤り・古い → UI issueを新規起票（`/issue-init 工程: UI`）し `docs/base-design/テストシナリオ.md` を修正
   - 実装が不足 → PG issueを新規起票（`/issue-init 工程: PG`）し `frontend/src/` に実装を追加
3. 対応後、本レポートのステータスを更新し、`specs/costman-2026-001/meta.md` の手戻り管理表を「完了」に更新する

## ステータス
対応中（次アクション未着手）
