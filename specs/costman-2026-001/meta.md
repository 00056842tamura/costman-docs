# 案件メタ情報（案件レベル）

> 案件開始（`/case-init`）で `specs/{案件キー}/meta.md` として生成する。
> 案件(Milestone)横断の不変情報・要件ID/機能ID採番の索引・工程issue対応表を管理する。

## 案件基本情報
- 案件キー: costman-2026-001
- プロダクト: コスト管理システム
- パッケージ名: jp.co.costman
- 案件統合ブランチ: feature/costman-2026-001
- Milestone URL: https://github.com/00056842tamura/costman-docs/milestone/1

## リポジトリ（3 リポ・`gh` の `--repo` に渡す／`.claude/rules/github-ops.md`）
> ※記載する owner/repo は、あらかじめ人間がGitHub上に作成済みのリポジトリであること。存在しないリポジトリ名を記載した場合、`/case-init` 実行時にエラーで停止する（AIがリポジトリを新規作成することはない）。
- 案件リポ:     00056842tamura/costman-docs（上流 docs・SS-Plan）
- スタックリポ: 00056842tamura/costman-frontend（frontend。bs/us-api/us-mpa/batch は現時点で未着手のため未確定）

> 補足: 既存の `frontend/develop` ブランチには、ローカル `frontend/` と構成が異なる旧世代の実装が既に存在する（コンポーネント分割・型定義ファイル構成が異なる）。ローカル成果物のpush・統合は本 `/case-init` の範囲外であり、別タスクでの整理・判断が必要（2026-08-21 時点、未解決）。

## 要件ID 採番表
> 要件ID（R###）の**正本は `docs/requirements/要件一覧.md`**。SA工程で採番する業務要求単位の識別子。本表は工程横断トレース用の索引。
> 以下は `/case-init` 実行前に既に旧設計書から移行済みの内容（`docs/requirements/要件一覧.md` より転記）。

| 要件ID | 要件名 | 概要 | 採番日 |
|---|---|---|---|
| R001 | トップページの表示 | 未ログインでも表示可能な入口ページ | 2026-08-20 |
| R002 | ログイン認証 | 独自ID・パスワード方式。セッション（Cookie）＋ローカルストレージでログイン状態を維持 | 2026-08-20 |
| R003 | メニューからの機能選択 | ログイン必須。管理者権限の場合のみ「管理者用」ボタンを表示 | 2026-08-20 |
| R004 | 工番別収支データの参照 | ログイン必須。工番検索ダイアログを含む | 2026-08-20 |
| R005 | コスト利用率の参照 | ログイン必須。工番別収支データ参照画面への遷移を含む | 2026-08-20 |
| R006 | プロ管データの取り込み（一般ユーザー向け） | ログイン必須。権限による制限なし | 2026-08-20 |
| R007 | 社員活動情報・単価・ソフ仮データの取り込み（管理者向け） | ログイン必須。管理者権限（`admin`）が必要 | 2026-08-20 |

## 機能ID 採番表
> 機能ID（{カテゴリ}-{3桁連番}）の**正本は `docs/base-design/機能一覧.md`**。UI工程で要件ID（R###）をスタック×API/画面/ジョブ単位に分解して採番する識別子。本表は工程横断トレース用の索引。
> 以下は `/case-init` 実行前に既に生成済みの内容（`docs/base-design/機能一覧.md` より転記。詳細な対応関係は正本ファイルを参照）。

| 機能ID | 要件ID | スタック | 種別（API/画面/ジョブ） | 機能名 | 採番日 |
|---|---|---|---|---|---|
| front-intra-001〜007 | R001〜R007 | frontend | 画面 | （詳細は docs/base-design/機能一覧.md 参照） | 2026-08-20 |
| bs-001〜009 | - | bs（参考情報・未着手） | API | （詳細は docs/base-design/Web_API_IF一覧表.md 参照） | 2026-08-20 |

## issue-id 対応表（工程 × 要件ID/機能ID）
> 各工程で起票した GitHub issue を要件ID・機能ID・工程に紐付ける。`log_work.py` の issue-id 解決に使用するため必ず更新する。
> **SA/UI = 要件ID（R###）単位、SS-Plan/PG-Plan/PT-Plan = 案件・スタック単位、SS/PG/PT = 機能ID（{カテゴリ}-{3桁連番}）単位。**

| 工程 | issue種別 | issue-id | 対象ID | 対象スタック | ブランチ | ステータス |
|---|---|---|---|---|---|---|
| SA | phase | | R001〜R007（旧設計書からの移行・issue未起票） | N/A | feature/costman-2026-001-sa | 事後記録 |
| UI | phase | | R001〜R007（旧設計書からの移行・issue未起票） | N/A | feature/costman-2026-001-ui | 事後記録 |
| SS-Plan | phase | 1 | | N/A | feature/costman-2026-001-ssplan | 着手（issue-1・plan.md作成中） |
| SS | task | | front-intra-001〜007（issue未起票） | frontend | feature/costman-2026-001-ss-front-intra-### | 事後記録 |
| PG-Plan | phase | | | frontend | feature/costman-2026-001-pgplan | |
| PG | task | | front-intra-001〜007（issue未起票） | frontend | feature/costman-2026-001-pg-front-intra-### | 事後記録 |
| PT-Plan | phase | 1 | | frontend | feature/costman-2026-001-ptplan | plan.md作成済み（sub-issue棚卸し完了・PRレビュー待ち） |
| PT | task | | front-intra-001〜007 | frontend | feature/costman-2026-001-pt-front-intra-### | 未着手 |

## 手戻り管理表

> 手戻り発生時に採番し記録する。手戻りID（`sp-{3桁連番}`）は案件内でユニーク。

| 手戻りID | 原因概要 | 発覚工程 | 影響工程 | 関連 issue | ステータス |
|---|---|---|---|---|---|
| sp-001 | `docs/base-design/テストシナリオ.md`（UI正本）が参照する `convertHiddenRow`（UT-3）・`axiosErrorHandling`（UT-9〜11の詳細挙動）が、現行PG実装（`frontend/src/`）に存在しなかった | PT-Plan（issue-1・front-intra-004,002/004/005/006/007横断） | PG（frontend実装。old_docs調査により実装不足と判明・テストシナリオ.mdの修正は不要） | 未起票（issue化せず直接実装追加で対応） | 完了（2026-08-21・`convertHiddenRow`/`useErrorHandling`のバリデーションエラー分岐を追加。frontendのpushは別途「ソース統合」で実施） |

## バックログ持ち越し管理表

> Step 0（バックログ解消ゲート）でユーザーが承認した次工程への持ち越し事項を記録する。

| No | 発生工程 | 発生issue | 項目概要 | 承認先工程 | 承認者 | 承認日 | ステータス |
|---|---|---|---|---|---|---|---|

## 関連
- 要件ID採番の正本: `docs/requirements/要件一覧.md`
- 機能ID採番の正本: `docs/base-design/機能一覧.md`
- 工程issue 固有の状態: `specs/{案件キー}/{工程}/{issue_id}/meta.md`
- 手戻りルール: `.claude/rules/product-rules.md §手戻り統一ワークフロー`
- 経緯調査レポート: `生成レポート/テストコード生成テスト観点抽出_実行可否調査レポート_20260821.md`
