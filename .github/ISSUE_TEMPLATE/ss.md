---
name: "[SS-bs-001] 詳細設計-{機能名}"
about: SS 詳細設計工程（type:task・スタックリポ・bs-001 単位）。起票時に「工程:ss」「area:{スタック}」ラベルと Milestone を設定してください。
title: "[SS-bs-001] 詳細設計-{機能名}"
labels: ["type:task", "repo:stack", "工程:ss"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: SS 詳細設計（type:task・スタックリポ）
- 機能ID: <!-- bs-001 --> ／ 対象スタック: <!-- bs / us-api 等 -->
- 作業ブランチ: `feature/{案件キー}-ss-bs-001`
- リポ: スタックリポ
- 担当:

## 依存（blocked-by）
- Blocked by: <!-- #N（us-api 等の場合、依存する bs SS が完了していること） -->

---

## この工程で担保すること
- PG 担当者が設計書を見るだけで迷わず実装に着手できる粒度の設計情報が揃っている
- 外部 IF の変更点が明確になっており、他スタックへの影響が把握できる
- レビューで設計の論理的正確性・規約適合性が確認されている

## 対応概要
機能ID単位で WB（処理ウォークスルー）と PSK50 形式設計書を作成する（backend/batch: 外部IF定義書・プログラム仕様書／frontend: コンポーネント仕様書・画面アクション遷移図）。
テーブル一覧・テーブル定義書は UI 工程 `docs/base-design/` が正本のため本工程では生成・変更しない（読み込み専用）。
ADR-SS-{n} に設計上の判断事項を記録し、WB・形式設計書は本工程のブランチに push して PG へ引き継ぐ。

## インプット
- `docs/base-design/`（UI 成果物・push 済み正本。テーブル一覧.md・テーブル定義書_{テーブルID}_{テーブル名（論理）}.md を含む）
- plan.md（案件リポ `specs/{案件キー}/detail-design-plan/{issue_id}/` の push 済み正本を直接参照）
- Blocked by 解消確認（us-api 等の場合、依存する bs SS が完了していること）

## 対応フロー
1. Blocked by が解消されていることを確認してから着手する
2. `/issue-init 工程: SS 案件キー: {案件キー} 機能ID: bs-001 対象スタック: {スタック}` を実行
3. SS-Plan の plan.md（push 済み正本）を直接参照する
4. `/detailed-design-gen` を実行して WB・形式設計書を生成する（backend/batch: 外部IF定義書・プログラム仕様書／frontend: コンポーネント仕様書・画面アクション遷移図。テーブル一覧・テーブル定義書は `docs/base-design/` から読み込む）
5. 対象スタックに応じ `/detailed-design-review-backend`（bs/us-api/us-mpa）・`/detailed-design-review-batch`（batch）・`/detailed-design-review-frontend`（frontend）のいずれかを実行してレビューし、`ADR-SS-{n}` に判断事項を記録する（finding 0件でも1件は必ず起票）
6. WB（controller/service/repository.md、batchはbatch.md、frontendはfrontend.md）を本工程のブランチへ push する
7. `/docs-to-pr` で工程ゲート PR を作成する

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| Controller / Service / Repository WB（backend） | `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` |
| batch.md WB（batch） | `batch/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` |
| frontend.md WB（frontend） | `frontend/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` |
| 外部 IF 定義書（backend/batch・外部連携時） | `{スタック}/docs/detail-design/` |
| プログラム仕様書（backend/batch） | `{スタック}/docs/detail-design/` |
| コンポーネント仕様書（frontend） | `frontend/docs/detail-design/` |
| 画面アクション遷移図（frontend） | `frontend/docs/detail-design/` |
| review-report.md（レビュー結果） | `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` |
| ADR-SS-{n} | `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/adr/` |

## レビュー対象
- WB 全件：処理ロジックの正確性・例外処理・トランザクション境界（backend/batch）／コンポーネント仕様・状態管理（frontend）
- 外部 IF 定義書：リクエスト/レスポンスの型・エラーコード（backend/batch）
- テーブル定義書（`docs/base-design/テーブル定義書_{テーブルID}_{テーブル名}.md`）との整合：repository.md のカラム定義が最新のテーブル定義書と一致しているか
- 画面アクション遷移図とコンポーネント仕様書「画面遷移」「アクション定義」表との横軸整合・`docs/base-design/画面状態遷移図_{機能ID}.md` との縦軸整合（frontendのみ・観点D）

## 完了ゲート
- [ ] 形式設計書（外部IF定義書・プログラム仕様書 または コンポーネント仕様書・画面アクション遷移図）が `{スタック}/docs/detail-design/` に生成されている
- [ ] WB が `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` として本PRに含まれている（push 済み）
- [ ] `review-report.md` と `adr/ADR-SS-*.md`（最低1件）が `{issue_id}_{機能ID}/` に存在する（無いと `gh pr create` 自体がブロックされる）
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] 工程ゲート PR が作成されている
- [ ] レビュアーの承認を得ている
