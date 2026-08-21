# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`TorikomiController`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(UPLOAD_PROKAN_DATA).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## プロ管データアップロードAPI

| 項目 | 内容 |
|---|---|
| 機能ID | bs-006 |
| API ID | UPLOAD_PROKAN_DATA |
| API名 | プロ管データアップロードAPI |
| API説明 | データ取り込み画面の「プロ管データ CSV取り込み」から呼び出し、CSVを取り込む |
| Method | PUT |
| Path | `/api/upload-prokandata`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | `Content-Type: multipart/form-data`（実装は`MultipartFile[]`のためこの形式で確定）、認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | 認証済みであればロール不問（`member`/`admin`） |
| Status Code | 200, 400, 401, 404, 409 |

### Path Variable

なし

### Query String

なし

### Request Body

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | プロ管データファイル | `prokanDatas` | file | - | ○ | 利用者が選択したフォルダ配下のファイル全件。同一物理名で繰り返し設定してフォームデータ形式で送信する |

### Response Header

不明（旧設計書に記載なし）

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 登録件数 | insertCount | number | - | ○ | フロントエンドはこの値のみ使用しメッセージ `INFOMATION_SUCCESS_REGIST` で通知する |
| 2 | 削除件数 | deleteCount | number | - | ○ | フロントエンドでは使用していない |

#### 400 Bad Request

> `errorCode`/`message`/`detail`の3項目構成は実装（`RestErrorInfo`）と一致を確認済み。`detail`内部のバリデーションエラー明細（`Field`/`ValidationMessage`）はSpringerフレームワークの規約と整合するが、実機起動でのレスポンス確認は未実施。想定される業務エラーコード: `bs.error.business.NippoMeisaiAlradyExists`（日報明細が既に存在）・`bs.error.business.FileLoadingInvalidFormat`（ファイル形式不正）・`bs.error.business.DuplicateKobanCd`（工番コード重複）・`bs.error.system.FileReadingFailure`（ファイル読込失敗）。**`TorikomiController`には`ConflictException`用の`@ExceptionHandler`が実装されておらず、業務コンフリクトが409として返るかは未確認。**

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
{ "insertCount": 120, "deleteCount": 0 }
```

#### Status Code: 400

```json
{ "detail": null, "errorCode": "bs.error.business.FileLoadingInvalidFormat", "message": "ファイルの内容が取り込み可能な形式ではありません。" }
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`TorikomiController`/`TorikomiServiceImpl`）と照合し、Path/認可/エラー構造の記載を更新（レスポンス構造`insertCount`/`deleteCount`は実装と一致確認済み） |
