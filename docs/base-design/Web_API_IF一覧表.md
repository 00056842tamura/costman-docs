# Web API I/F 一覧表

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本表は実装コード（Controllerクラス）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/` からの移行を基に、実装調査で判明した相違点を反映）。

## 実行環境

| 項目 | 内容 |
|---|---|
| ベースURL | `http://localhost:8080`（`server.servlet.context-path`は未設定のため、パスプレフィックスは付与されない） |
| APIパスプレフィックス | 全エンドポイント共通で `/api` が付与される（下表の Path 列は `/api` 込みの実パス） |
| 認証方式 | Cookieセッション認証（`JSESSIONID`）。フロントエンドの通信クライアントは `withCredentials: true` を設定する |
| CSRF | 無効化されているためトークン送信不要 |
| CORS | 未設定（無効）。フロントエンドを別オリジン（Vite開発サーバー等）で動かす場合は、bs側を変更せず開発用リバースプロキシ（Vite `server.proxy`）で吸収する |
| 認可 | `bs-007`（社活）・`bs-008`（ソフ仮）・`bs-009`（単価）の3APIのみ `admin` ロール限定。他は認証済みであればロール不問（`member`/`admin`） |

## API 一覧

| No. | 機能ID | API ID | API名 | Method | Path | API説明 | 認可 |
|---|---|---|---|---|---|---|---|
| 1 | bs-001 | GET_BUSHO_CD_LIST | 部署コード一覧取得API | GET | `/api/bushocd-list` | コスト利用率参照画面の初期表示時に部署コードの選択肢を取得する | 認証済みなら可 |
| 2 | bs-002 | LOGIN | ログインAPI | POST | `/api/login` | ユーザーID・パスワードによる認証を行う | 認証不要（ログイン自体） |
| 3 | bs-003 | SEARCH_COST_RIYORITSU | コスト利用率検索API | GET | `/api/cost-riyoritsu` | 検索条件に一致する工番のコスト利用率データを返す | 認証済みなら可 |
| 4 | bs-004 | SEARCH_KOBAN | 工番検索API | GET | `/api/search-koban` | 検索条件に一致した工番情報の一覧を返す | 認証済みなら可 |
| 5 | bs-005 | SEARCH_KOBANBETSU_SHUSHI | 工番別収支検索API | GET | `/api/kobanbetsu-shushi` | 検索した工番の収支データ（計画・実算・利用率・登録日時）を返す | 認証済みなら可 |
| 6 | bs-006 | UPLOAD_PROKAN_DATA | プロ管データアップロードAPI | PUT | `/api/upload-prokandata` | プロ管データ（CSV・複数件）を取り込み、登録件数・削除件数を返す | 認証済みなら可 |
| 7 | bs-007 | UPLOAD_SHAKATSU_DATA | 社活データアップロードAPI | PUT | `/api/upload-shakatsudata` | 年月と社員活動情報データ（CSV）を取り込み、登録件数・削除件数を返す | **adminロール限定** |
| 8 | bs-008 | UPLOAD_SOFUKARI_DATA | ソフ仮データアップロードAPI | PUT | `/api/upload-sofukaridata` | 年度とソフ仮データ（CSV）を取り込み、登録件数を返す | **adminロール限定** |
| 9 | bs-009 | UPLOAD_TANKA_DATA | 単価データアップロードAPI | PUT | `/api/upload-tankadata` | 単価データ（CSV）を取り込み、登録件数を返す | **adminロール限定** |

### Method 定義

| Method | 用途 |
|---|---|
| GET | リソースの取得 |
| POST | リソースの新規作成（本システムではログイン認証に使用） |
| PUT | リソースの更新（本システムではCSVデータの一括取り込みに使用） |

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版（`old_docs/03.Web_API_IF定義書/` からの移行） |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`ctm-costman-backend-main`）と照合し、Pathに`/api`プレフィックスを追加、実行環境（ベースURL・CORS・認可）の節を追加 |
