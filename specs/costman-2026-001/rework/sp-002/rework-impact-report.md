# 手戻り影響レポート（sp-002）

## 発覚経緯
- 発覚工程: 工程外（ユーザーとの対話調査。「現在生成されたコードと設計書では、セッションタイムアウト周りの挙動はどうなっていますか」という質問を起点に、frontend実装の出自を遡って調査した結果判明）
- 発覚日: 2026-08-24

## 不整合の内容

`frontend/src/features/` の front-intra-001〜007（7画面）の実装は、`costman-frontend` リポの PR #2「frontend実装の同期（costman-2026-001・旧世代実装を置き換え）」によって投入されたものであり、以下が確認された。

1. **SS工程が正規スキルを経由していない**: `/detailed-design-gen`（コンポーネント仕様書等の生成）・`/detailed-design-review-frontend`（レビュー）のいずれも実行された記録がない。
2. **SS成果物のうちWB・ADRが未整備**: `frontend/specs/costman-2026-001/detail-design/{issue_id}_{機能ID}/frontend.md`（WB）・`ADR-SS-*.md`・`review-report.md` が front-intra-001〜007 のいずれにも存在しない。
3. **PG工程が正規スキルを経由していない**: `/reacter-code-gen`・`/reacter-code-review` の実行記録がなく、`reacter-code-review-report.md` も存在しない。
4. **統合ブランチを経由していない**: PR #2 のベースブランチが `develop` であり、`feature/costman-2026-001` 統合ブランチを経由していない。
5. **軽微な命名不整合**: front-intra-001 のコンポーネント名が仕様書上は `TopPage`、実装は `Top.tsx`。

生成物自体の出自は複数の生成イベントが交錯しており（外部プロジェクト`ctm-costman-frontend-main`のコピー→`old_docs`/`_src`からの`new_docs`生成→`new_docs`ベースのコード生成→最終的に別途完了していたPR#2への置き換え）、`docs/base-design/`のみを直接・一貫して踏襲したことを示す記録は存在しない（詳細は `生成レポート/frontend設計書駆動再生成_作業状況レポート_20260821.md` 参照）。

## 影響範囲
- **SS**: front-intra-001〜007 全7機能ID。既存のSS sub-issue（#4〜#10）は「costman-frontend PR #2で完了済みと判明したため」としてクローズ済みだが、実際にはSS工程の正規記録（WB・ADR・review-report）が存在しないため、Reopenして正規に再実施する必要がある。
- **PG**: front-intra-001〜007 全7機能ID。PG issueは未起票のまま実装が投入されているため、新規にPG issueを起票し正規に再実施する必要がある。
- **PT**: 未着手のため直接の影響はないが、PT工程の入力となるSS/PG成果物が本手戻りで置き換わるため、PT-Planのsub-issue棚卸し内容（`plan.md`）が再生成後の実装と整合するか、PT着手前に再確認が必要。

## 対応方針
`生成レポート/frontend正規フロー再生成計画_20260824.md` に定義した Step 0〜3 に従い、`docs/base-design/` を正本として SS→PG を機能ID単位で正規フローにより再実施する。既存実装は置き換え対象とし、既知の実装教訓（sp-001の`convertHiddenRow`/`useErrorHandling`、コスト利用率参照画面の3点差異）がリグレッションしないことを再生成後に確認する。

## ステータス
起票済み・着手前（2026-08-24）。GitHub操作（issue Reopen・新規issue起票・ブランチ作成・PR作成）を伴うため、着手前にユーザーへ確認を得る。
