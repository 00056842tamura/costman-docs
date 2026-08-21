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

## 調査結果（2026-08-21実施）
`old_docs/00.共通部品.md`・`old_docs/工番別収支データ参照画面(KOBANBETSU_SHUSHI)/04.画面・帳票編集仕様.md` を確認した結果、**両件ともテストシナリオ.mdの記載が正しく、PG実装側が不足していた**と判明した。

- `old_docs/00.共通部品.md` §17「通信エラーハンドリング」item5: 「応答のエラーコードがバリデーションエラー（`error.unexpect.method-argument-not-valid`）で、かつ検証結果の明細がある場合、明細のメッセージを改行（`\r\n`）で連結し、エラー区分のトースト通知で表示する」と明記されている。
- `old_docs/工番別収支データ参照画面(KOBANBETSU_SHUSHI)/04.画面・帳票編集仕様.md`（部署・社員番号・氏名の項目）: 「予実区分フラグが`0`（計画）の行は表示する。`1`（実算）の行は空欄とする」と明記されている。

## 対応内容（2026-08-21実施・実装追加）
- `frontend/src/utils/commonUtil.ts` に `convertHiddenRow` を追加し、`frontend/src/features/kobanbetsuShushi/components/KobanbetsuShushi.tsx` の収支一覧行データ生成部（`buildShushiListRows` の後段）に適用した。
- `frontend/src/const/const.ts` に `SERVER_ERROR_CODE.VALIDATION_ERROR`（`'error.unexpect.method-argument-not-valid'`）を追加し、`frontend/src/hooks/useErrorHandling.ts` の `handleError` にバリデーションエラー時の `detail[].ValidationMessage` 改行結合表示ロジックを追加した。
- `docs/base-design/テストシナリオ.md` の修正は不要（記載どおりの実装になったため）。
- `frontend/specs/costman-2026-001/unit-test-plan/1/plan.md` のUT-3・UT-9〜11除外の注記を、実装追加後の内容に更新する。

## ステータス
対応完了（2026-08-21・実装追加により解消。frontend/はローカルのみ・costman-frontendへのpushはIssue「frontend/ ソース統合」で別途実施）
