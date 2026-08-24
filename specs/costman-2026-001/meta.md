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
| SS-Plan | phase | 1 | | N/A | feature/costman-2026-001-ssplan | クローズ（PR #2 マージ済み。SS sub-issue 7件は下記の通り別PRで完了判明のため全件クローズ） |
| SS | task | #4(001) #5(002) #6(003) #7(004) #8(005) #9(006) #10(007) | front-intra-001〜007 | frontend | feature/costman-2026-001-ss-front-intra-###（手戻り再開分は `-sp-002` 付与） | 手戻りsp-002によりReopen（2026-08-24）。front-intra-001〜007全件、SS正規再生成PR作成・レビュー（全件「PR提出可」）・**マージ完了**（PR #12〜#18、2026-08-24 10:02〜10:11 UTC・feature/costman-2026-001へマージ済み）。front-intra-002〜007では実装との不整合（存在しないコンポーネント分割・状態管理方式の記述誤り・画面状態遷移図の簡略化）を検出・修正済み。front-intra-007では追加でAPI定義書（bs-007/008/009のStatus Code 403）と実装（HANDLING_STATUS_CODE定数）の不整合を検出しPG工程へのバックログとして記録（ADR-SS-1・PG工程で対応予定） |
| PG-Plan | phase | | | frontend | feature/costman-2026-001-pgplan | 実施せず（SS/PGともcostman-frontend PR #2で完了済みのため） |
| PG | task | #19(007完了)・#21(001完了)・#23(002完了) | front-intra-001〜007 | frontend | feature/costman-2026-001-pg-front-intra-###-sp-002 | 手戻りsp-002対応として着手（2026-08-24）。front-intra-007（issue #19・PR #20）・front-intra-001（issue #21・PR #22）・front-intra-002（issue #23・PR #24）完了・レビュー待ち。いずれも`/reacter-code-gen`→`/reacter-code-review`（観点台帳クラスタ別2パス検証）を実施。front-intra-007: `HANDLING_STATUS_CODE`への403追加。front-intra-001: 型定義切り出し・try-catch追加・as const付与。front-intra-002: レイアウトCSS修正（CSS-04）。全機能ID横断の既存課題（COM-01・QLT-22）はADR-PG-1.mdにバックログ化。front-intra-002ではUI工程正本（docs/base-design/）のSS修正未追従も検出しmeta.mdバックログ持ち越し管理表No.1〜4に記録。残り4機能ID（003〜006）へ展開中。事後修正: PR #11（`.env.development`/`.env.prod`のセッションタイムアウト設定値を仕様〔1時間・タイムアウト30秒前警告〕に修正、マージ済み2026-08-24） |
| PT-Plan | phase | 1 | | frontend | feature/costman-2026-001-ptplan | クローズ（PR #3 マージ済み・issue #1 クローズ済み2026-08-24）。plan.md作成済み・sub-issue棚卸し完了 |
| PT | task | | front-intra-001〜007 | frontend | feature/costman-2026-001-pt-front-intra-### | 未着手 |

## 手戻り管理表

> 手戻り発生時に採番し記録する。手戻りID（`sp-{3桁連番}`）は案件内でユニーク。

| 手戻りID | 原因概要 | 発覚工程 | 影響工程 | 関連 issue | ステータス |
|---|---|---|---|---|---|
| sp-001 | `docs/base-design/テストシナリオ.md`（UI正本）が参照する `convertHiddenRow`（UT-3）・`axiosErrorHandling`（UT-9〜11の詳細挙動）が、現行PG実装（`frontend/src/`）に存在しなかった | PT-Plan（issue-1・front-intra-004,002/004/005/006/007横断） | PG（frontend実装。old_docs調査により実装不足と判明・テストシナリオ.mdの修正は不要） | 未起票（issue化せず直接実装追加で対応） | 完了（2026-08-21・`convertHiddenRow`/`useErrorHandling`のバリデーションエラー分岐を追加。frontendのpushは別途「ソース統合」で実施） |
| sp-002 | costman-frontend PR #2（frontend実装の同期）が正規のSS工程スキル（`/detailed-design-gen`→`/detailed-design-review-frontend`）・PG工程スキル（`/reacter-code-gen`→`/reacter-code-review`）を経由せずに生成され、`frontend.md`（WB）・`ADR-SS-*`・`review-report.md`・`reacter-code-review-report.md` が front-intra-001〜007 いずれも未整備。本体PR（develop向け）はマージ済みのため `cross-process-consistency.md`「本体PRマージ後に発覚した場合の手戻り判定」に従い手戻り相当として扱う | 本セッションでのユーザーとの対話調査（2026-08-24） | SS・PG（frontend実装。7機能ID全て・スタック内で影響が閉じる） | SS: #4〜#10（Reopen済み・全件PR作成済み）／PG: 未起票（新規起票予定） | SS対応完了（front-intra-001〜007全件、PR #12〜#18マージ完了・2026-08-24）。PG対応着手中（issue-init→reacter-code-gen→reacter-code-reviewを機能ID単位で実施）。対応計画: `生成レポート/frontend正規フロー再生成計画_20260824.md`。詳細: `specs/costman-2026-001/rework/sp-002/rework-impact-report.md` |

## バックログ持ち越し管理表

> Step 0（バックログ解消ゲート）でユーザーが承認した次工程への持ち越し事項を記録する。

| No | 発生工程 | 発生issue | 項目概要 | 承認先工程 | 承認者 | 承認日 | ステータス |
|---|---|---|---|---|---|---|---|
| 1 | PG（front-intra-002・issue #23） | costman-frontend #23 | `docs/base-design/画面状態遷移図_front-intra-002.md`(L20)のコンポーネント名「LoginPage」→「Login」に修正（SS工程の正本更新に未追従） | UI | Mai Tamura | 2026-08-24 | 未対応 |
| 2 | PG（front-intra-002・issue #23） | costman-frontend #23 | `docs/base-design/画面状態遷移図_front-intra-002.md`(L39)のエラー応答ステータス「400/401/404/409」→「401のみ」に修正（SS工程の正本更新に未追従） | UI | Mai Tamura | 2026-08-24 | 未対応 |
| 3 | PG（front-intra-002・issue #23） | costman-frontend #23 | `docs/base-design/機能概要_front-intra-002.md`(L31)・`テストシナリオ.md`(L149,L151)の遷移元パス項目名「`from`」→「`pathname`」に修正（SS工程の正本更新に未追従） | UI | Mai Tamura | 2026-08-24 | 未対応 |
| 4 | PG（front-intra-002・issue #23） | costman-frontend #23 | `docs/base-design/テストシナリオ.md`(L98)の自動ログアウト記述精度の修正（401時は無条件でタイマー起動する実装と厳密には不一致） | UI | Mai Tamura | 2026-08-24 | 未対応 |

## 関連
- 要件ID採番の正本: `docs/requirements/要件一覧.md`
- 機能ID採番の正本: `docs/base-design/機能一覧.md`
- 工程issue 固有の状態: `specs/{案件キー}/{工程}/{issue_id}/meta.md`
- 手戻りルール: `.claude/rules/product-rules.md §手戻り統一ワークフロー`
- 経緯調査レポート: `生成レポート/テストコード生成テスト観点抽出_実行可否調査レポート_20260821.md`
