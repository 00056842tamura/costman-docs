# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`KobanbetsuShushiController`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(SEARCH_KOBAN).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## 工番検索API

| 項目 | 内容 |
|---|---|
| 機能ID | bs-004 |
| API ID | SEARCH_KOBAN |
| API名 | 工番検索API |
| API説明 | 検索条件に一致した工番情報の一覧を返す（工番別収支データ参照画面の工番検索ダイアログから利用） |
| Method | GET |
| Path | `/api/search-koban`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | 認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | 認証済みであればロール不問（`member`/`admin`） |
| Status Code | 200, 400, 401, 404, 409 |

### Path Variable

なし

### Query String

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 工番コードの親番 | `kobanCdOya` | string | - / 6 | △ | 半角数字のみ。未入力を許容する（前方一致） |
| 2 | 工番コードの子番 | `kobanCdKo` | string | 2 / 2 | △ | 半角数字のみ。入力する場合は2桁（完全一致） |
| 3 | 工番名 | `kobanNm` | string | - / 15 | △ | 未入力を許容する（部分一致） |

3項目すべてが未入力の場合、フロントエンドの関連項目チェックにより通信自体を行わない。

### Request Body

なし

### Response Header

不明（旧設計書に記載なし）

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| （最上位） | 工番情報一覧 | - | 配列 | - | ○ | 0件の場合は空配列と推定（サーバ側仕様のため不明） |
| 1 | 工番コードの親番 | kobanCdOya | string | - | ○ | |
| 2 | 工番コードの子番 | kobanCdKo | string | - | ○ | |
| 3 | 工番名 | kobanNm | string | - | ○ | |
| 4 | 優先度 | priority | string | - | ○ | フロントエンドでは使用していない |

#### 400 Bad Request

> `errorCode`/`message`/`detail`の3項目構成は実装（`RestErrorInfo`）と一致を確認済み。`detail`内部のバリデーションエラー明細（`Field`/`ValidationMessage`）はSpringerフレームワークの規約と整合するが、実機起動でのレスポンス確認は未実施。

| No. | 論理名 | 物理名 | 型 | 説明 |
|---|---|---|---|---|
| 1 | エラーコード | errorCode | string | |
| 2 | メッセージ | message | string | |
| 3 | 明細 | detail | object[]／null | |
| 3-1 | フィールド名 | Field | string | |
| 3-2 | バリデーションメッセージ | ValidationMessage | string | |

`401`・`404`・`409` も同一のレスポンス形式。

### 電文サンプル

#### Status Code: 200

```json
[
  { "kobanCdOya": "123456", "kobanCdKo": "01", "kobanNm": "サンプル工番", "priority": "1" }
]
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`KobanbetsuShushiController#getKobanInfo`）と照合し、Path/認可/エラー構造の記載を更新（レスポンス構造は`KobanInfo`モデルと一致確認済み） |
