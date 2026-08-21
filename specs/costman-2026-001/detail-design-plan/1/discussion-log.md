# 壁打ち履歴・意思決定ログ

> 🤖 このファイルは Claude Code との壁打ち中に自動更新されます。
> 重要な決定事項は手動で追記しても構いません。

---

## 元チケット内容

> **タイトル**: 詳細設計計画
>
> **本文**:
> UI 工程で確定済みの機能ID（front-intra-001〜007）を基に SS sub-issue の棚卸しを行い、plan.md に記録する。クロススタック依存を確定し、SS sub-issue を全量先行起票する。
> インプット: `docs/base-design/`（機能一覧・実装対象クラス一覧・テーブル定義書等、push済み正本）
> アウトプット: plan.md（`specs/costman-2026-001/detail-design-plan/1/`）、SS sub-issue（frontendスタックリポ）
>
> **ラベル**: 工程:ss-plan, type:phase, repo:case
> **担当者**: Mai Tamura

---

## Phase 1: SS-Plan 棚卸しサマリー（2026-08-21）

### Step 3.5: スタック構築状況の確認

- `.claude/repositories.local.md`「## スタックリポ」: frontend 行は記入済み（`00056842tamura/costman-frontend`）
- `frontend/` は clone済み（`.git` 存在確認済み）、`frontend/package.json` 存在（プレースホルダーでない）→ `/stack-init` 実施済みと判断
- bs は `docs/base-design/機能一覧.md` 記載の通り未着手（`bs-xxx` は参考情報のみ）。本plan.mdの棚卸し対象外
- → 未構築スタックなし。完了報告に警告事項なし

### Step 3.6: 工程内整合性チェック

SS-Plan では対象外（`docs/base-design/` は UI issue 単位で `/basic-design-review` 済みのため）。

### 確認した不明点と決定事項

| 質問 | 決定内容 |
|-----|---------|
| front-intra-001〜007 の着手順序をどう決めるか | `front-intra-002`（ログイン）を認証基盤設計の起点として最優先。`front-intra-001`（トップページ）は認証基盤に依存しないためユーザー承認済みパイロットとして最優先で並走。残り5画面は `front-intra-002` を blocked-by とする |
| bs-001〜009 を棚卸し対象に含めるか | 含めない（bs スタック未着手・参考情報のため） |

---

## Phase 2: PR#2発見によるクローズ判断（2026-08-21）

SS sub-issue（#4〜#10）の着手準備中、`frontend/`のローカルgitリポジトリに`feature/costman-2026-001-frontend-sync`ブランチが存在し、既にcostman-frontend PR #2（develop向け・「frontend実装の同期（costman-2026-001・旧世代実装を置き換え）」）としてfront-intra-001〜007全画面のSS詳細設計成果物（`docs/detail-design/コンポーネント仕様書_*.md`・画面アクション遷移図・共通設計書・メッセージ一覧）とPG実装（`src/features/`配下）が完了・push済みであることが判明した。

ユーザー確認の上、PR #2をそのままマージし、本SS-Plan issue・SS sub-issue（#4〜#10）は完了済みとしてクローズする方針とした。

**既知のgovernanceギャップ**（今後の課題として記録）:
- `frontend/specs/costman-2026-001/detail-design/`相当のWB（`frontend.md`）・`ADR-SS-*`・`review-report.md`が作成されていない（PR #2は正規のSS工程スキル〔`/detailed-design-gen`・`/detailed-design-review-frontend`〕を経由せず生成されたため）
- PR #2のベースブランチが`develop`であり、`feature/costman-2026-001`（frontendリポの統合ブランチ）を経由していない（同ブランチ自体がfrontendリポに存在しない）
- front-intra-001の仕様書で「コンポーネント名: TopPage」と記載されているが実装は`Top.tsx`（軽微な命名不整合）

## 未解決事項・TODO

<!-- 記法（docs-to-pr Step 0 が機械判定する書式。.claude/rules/github-ops.md §3-B「バックログ記録形式」参照）:
     未解決:           - [ ] {項目}: {内容}
     解消時:           - [x] {項目}: {内容}（解消: {決定内容・日付}）
     承認済み持ち越し: - [ ] {項目}: {内容}（承認済み持ち越し→{次工程}・承認者: {ユーザー名}・{日付}） -->
- [ ] us-api の要否（`実装対象クラス一覧.md`未確定事項）: 現行フロントは`bs`を直接呼び出す構成で移行されているが、`product-rules.md`の許可される依存関係（`frontend→us-api→bs`）との整合は本SS-Planでは判断せず、SS工程（bs着手後）に持ち越す（承認済み持ち越し→SS・承認者: Mai Tamura・2026-08-21）

