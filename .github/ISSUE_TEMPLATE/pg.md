---
name: "[PG-bs-001] 実装-{機能名}"
about: PG 実装工程（type:task・スタックリポ・bs-001 単位）。起票時に「工程:pg」「area:{スタック}」ラベルと Milestone を設定してください。
title: "[PG-bs-001] 実装-{機能名}"
labels: ["type:task", "repo:stack", "工程:pg"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: PG 実装（type:task・スタックリポ）
- 機能ID: <!-- bs-001 --> ／ 対象スタック: <!-- bs / us-api 等 -->
- 作業ブランチ: `feature/{案件キー}-pg-bs-001`
- リポ: スタックリポ
- 担当:

## 依存（blocked-by）
- Blocked by: <!-- #N -->

---

## この工程で担保すること
- Springer/Reacter 規約に準拠した実装コードが生成されている
- レビューで規約違反・ロジック誤りがゼロになっている（backend/batch: `/springer-review-fix` で消し込み・frontend: 手動修正）
- SS 工程で生成済みのプログラム仕様書・メッセージ一覧（backend/batch）またはコンポーネント仕様書・画面アクション遷移図（frontend）と実装が整合している

## 対応概要
WB・形式設計書（SS 成果物）を基に機能ID単位で実装コードのみを生成する（**docs 生成なし**。プログラム仕様書・メッセージ一覧・コンポーネント仕様書・画面アクション遷移図は SS 工程で `{スタック}/docs/detail-design/` に生成済みであり、本工程では読み込み専用）。
backend/batch は `/springer-scaffold` → `/springer-review` → `/springer-review-fix`、frontend は `/reacter-code-gen` → `/reacter-code-review`（修正は手動）で進める。

## インプット
- WB（backend: Controller/Service/Repository・batch: batch.md・frontend: frontend.md）（SS の push 済み正本を直接参照。同一スタックリポの統合ブランチ上に既に push されている）
- `{スタック}/docs/detail-design/`（SS 成果物・プログラム仕様書・メッセージ一覧・コンポーネント仕様書・画面アクション遷移図等・push 済み正本）
- Blocked by 解消確認

## 対応フロー
1. Blocked by が解消されていることを確認してから着手する
2. `/issue-init 工程: PG 案件キー: {案件キー} 機能ID: bs-001 対象スタック: {スタック}` を実行
3. WB・形式設計書を SS の push 済み正本から直接読み込む
4. **backend/batch**: `/springer-scaffold` を実行して実装コードを生成する → `/springer-review` でレビューレポートを生成する → `/springer-review-fix` でレポートを消し込み、未対応 0 件を確認する
   **frontend**: `/reacter-code-gen` を実行して実装コードを生成する → `/reacter-code-review` でレビューレポートを生成する → 指摘事項を手動で修正する（`-fix` に相当する自動消し込みスキルは無い）
5. `/docs-to-pr` で工程ゲート PR を作成する

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| 実装コード（全クラス／全コンポーネント） | `{スタック}/src/main/` |
| レビューレポート（backend/batch: springer-review-report.md） | `{スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/` |
| レビューレポート（frontend: reacter-code-review-report.md） | `frontend/specs/{案件キー}/implementation/{issue_id}_{機能ID}/` |

## レビュー対象
- 全実装クラス／全コンポーネント（`/springer-review`または`/reacter-code-review` の出力）：規約準拠・ロジック・例外処理
- レビューレポート：未対応指摘がゼロになっているか
- 実装コード：SS工程のプログラム仕様書・メッセージ一覧（backend/batch）またはコンポーネント仕様書・画面アクション遷移図（frontend）との整合性

## 完了ゲート
- [ ] 全クラス／全コンポーネントが `{スタック}/src/main/` に生成されている（**docs 生成はPG工程の対象外**）
- [ ] `springer-review-report.md`（backend/batch）または `reacter-code-review-report.md`（frontend）が `{issue_id}_{機能ID}/` に存在する（無いと `gh pr create` 自体がブロックされる）
- [ ] backend/batch: `/springer-review-fix` で未対応指摘が 0 件になっている／frontend: レビュー指摘を手動で解消済み
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] 工程ゲート PR が作成されている
- [ ] レビュアーの承認を得ている
