# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`CommonController`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(GET_BUSHO_CD_LIST).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## 部署コード一覧取得API

| 項目 | 内容 |
|---|---|
| 機能ID | bs-001 |
| API ID | GET_BUSHO_CD_LIST |
| API名 | 部署コード一覧取得API |
| API説明 | コスト利用率参照画面の初期表示時に呼び出し、部署コードの選択肢を取得する |
| Method | GET |
| Path | `/api/bushocd-list`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | 認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | 認証済みであればロール不問（`member`/`admin`） |
| Status Code | 200, 400, 401, 404, 409 |

### Path Variable

なし

### Query String

なし（リクエストパラメータはない）

### Request Body

なし

### Response Header

| ステータス | ヘッダー |
|---|---|
| 200 | 不明（旧設計書に記載なし） |

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| （最上位） | 部署コード一覧 | - | 配列 | - | ○ | 応答の最上位が文字列の配列（オブジェクトの配列ではない） |
| 1 | 部署コード | （配列の要素） | string | 4桁（画面側の入力チェックで4桁として扱う。サーバ側桁数は不明） | ○ | フロントエンドでは各要素を選択肢の表示ラベルと値の両方に使用する |

#### 400 Bad Request

> `errorCode`/`message`/`detail`の3項目構成は実装（`jp.co.nekonet.springer.exceptionhandler.RestErrorInfo`）と一致を確認済み。`detail`内部のバリデーションエラー明細（`Field`/`ValidationMessage`）はSpringerフレームワークの規約と整合するが、実機起動でのレスポンス確認は未実施。

| No. | 論理名 | 物理名 | 型 | 説明 |
|---|---|---|---|---|
| 1 | エラーコード | errorCode | string | エラーコード（メッセージID） |
| 2 | メッセージ | message | string | エラーメッセージ |
| 3 | 明細 | detail | object[]／null | バリデーションエラー時の明細 |
| 3-1 | フィールド名 | Field | string | エラーフィールド名 |
| 3-2 | バリデーションメッセージ | ValidationMessage | string | エラー詳細メッセージ |

`401`・`404`・`409` も同一のレスポンス形式（`detail`／`errorCode`／`message`）。

### 電文サンプル

#### Status Code: 200

```json
["1001", "1002", "2001"]
```

#### Status Code: 400

```json
{
  "detail": [{ "Field": "example", "ValidationMessage": "サンプルメッセージ" }],
  "errorCode": "error.unexpect.method-argument-not-valid",
  "message": "Validation is failed."
}
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`CommonController`）と照合し、Path/認可/エラー構造の記載を更新 |
