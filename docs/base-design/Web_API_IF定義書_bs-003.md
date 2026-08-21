# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`CostRiyoritsuController`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(SEARCH_COST_RIYORITSU).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## コスト利用率検索API

| 項目 | 内容 |
|---|---|
| 機能ID | bs-003 |
| API ID | SEARCH_COST_RIYORITSU |
| API名 | コスト利用率検索API |
| API説明 | 検索条件に一致したコスト利用率データを返す |
| Method | GET |
| Path | `/api/cost-riyoritsu`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | 認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | 認証済みであればロール不問（`member`/`admin`） |
| Status Code | 200, 400, 401, 404, 409 |

### Path Variable

なし

### Query String

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 工番コードの親番 | `kobanCdOya` | string | - / 6 | △（項目は送信・値未入力可） | 半角数字のみ。未入力を許容する |
| 2 | 工番コードの子番 | `kobanCdKo` | string | 2 / 2 | △ | 半角数字のみ。入力する場合は2桁。未入力を許容する |
| 3 | 工番名 | `kobanNm` | string | - / 15 | △ | 未入力を許容する |
| 4 | 年度 | `nendo` | string | 4 / 4 | ○ | 半角数字のみ。選択肢は当年度を含む3年度分 |
| 5 | 部署コード | `bushoCd` | string | 4 / 4 | △ | 半角数字のみ。部署コード一覧取得API（bs-001）で取得。未選択を許容する |

### Request Body

なし

### Response Header

不明（旧設計書に記載なし）

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 登録日時一覧 | registerDate | object[] | - | ○ | フロントエンドで5件分の領域を確保して表示する |
| 1-1 | 連番 | registerDate.no | string | - | ○ | |
| 1-2 | テーブル名 | registerDate.table | string | - | ○ | |
| 1-3 | 登録日時 | registerDate.registDate | string | - | ○ | 値がない場合 `null` |
| 2 | コスト利用率データ一覧 | usageRate | object[] | - | ○ | 収支管理データと同一構造（`nendo`/`kobanCdOya`/`kobanCdKo`/`shainNo`/`yojitsuFlg`/`bushoCd`/`kobanBushoCd`/`shainNm`/`kobanNm`/`firstMonth`〜`twelfthMonth`/`total`/`percent`/`registDt`/`updateDt`） |

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
  "registerDate": [{ "no": "1", "table": "SHAKATSU", "registDate": "2026/08/01 09:00:00" }],
  "usageRate": [{ "kobanCdOya": "123456", "kobanCdKo": "01", "kobanNm": "サンプル工番", "firstMonth": 105.0 }]
}
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`CostRiyoritsuController`）と照合し、Path/認可/エラー構造の記載を更新（レスポンス構造は実装と一致確認済み） |
