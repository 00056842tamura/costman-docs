# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`KobanbetsuShushiController`/`KobanbetsuShushiServiceImpl`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(SEARCH_KOBANBETSU_SHUSHI).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## 工番別収支検索API

| 項目 | 内容 |
|---|---|
| 機能ID | bs-005 |
| API ID | SEARCH_KOBANBETSU_SHUSHI |
| API名 | 工番別収支検索API |
| API説明 | 検索した工番の収支データを返す |
| Method | GET |
| Path | `/api/kobanbetsu-shushi`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | 認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | 認証済みであればロール不問（`member`/`admin`） |
| Status Code | 200, 400, 401, 404, 409 |

### Path Variable

なし

### Query String

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 工番コードの親番 | `kobanCdOya` | string | 6 / 6 | ○ | 半角数字のみ |
| 2 | 工番コードの子番 | `kobanCdKo` | string | 2 / 2 | ○ | 半角数字のみ |
| 3 | 年度 | `nendo` | string | 4 / 4 | ○ | 半角数字のみ。選択肢は当年度を含む3年度分 |

### Request Body

なし

### Response Header

不明（旧設計書に記載なし）

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 工番情報 | kobanInfo | object | - | ○ | `kobanCdOya`/`kobanCdKo`/`kobanNm`/`priority`。実装は検索結果の先頭1件のみを設定する |
| 2 | 収支レコード一覧 | recordList | object[] | - | ○ | `bushoCd`/`shainNo`/`shainNm`/`keikakuShushi`/`jissanShushi`（後2つは収支管理データ構造） |
| 2-1 | 収支管理データ（計画/実算/合計/利用率で共通構造） | keikakuShushi 等 | object | - | - | `nendo`/`kobanCdOya`/`kobanCdKo`/`shainNo`/`yojitsuFlg`/`bushoCd`/`kobanBushoCd`/`shainNm`/`kobanNm`/`firstMonth`〜`twelfthMonth`（数値）/`total`/`percent`/`registDt`/`updateDt` |
| 3 | 速報値フラグ | sokuhoFlgMap | object | - | ○ | `firstMonth`（4月）〜`twelfthMonth`（3月）の12か月分の真偽値 |
| 4 | 計画の合計 | keikakuTotal | object | - | ○ | 収支管理データ構造 |
| 5 | 実算の合計 | jissanTotal | object | - | ○ | 収支管理データ構造 |
| 6 | 利用率 | usageRate | object | - | ○ | 収支管理データ構造 |
| 7 | 登録日時一覧 | registerDate | object[] | - | ○ | `no`/`table`/`registDate`（値なしは `null`）。フロントエンドで5件分の領域を確保 |

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
{
  "kobanInfo": { "kobanCdOya": "123456", "kobanCdKo": "01", "kobanNm": "サンプル工番", "priority": "1" },
  "recordList": [{ "bushoCd": "1001", "shainNo": "0000001", "shainNm": "山田太郎" }],
  "sokuhoFlgMap": { "firstMonth": false },
  "registerDate": [{ "no": "1", "table": "SHAKATSU", "registDate": null }]
}
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`KobanbetsuShushiController`/`KobanbetsuShushiServiceImpl`）と照合し、Path/認可/エラー構造の記載を更新（レスポンス構造は実装と一致確認済み） |
