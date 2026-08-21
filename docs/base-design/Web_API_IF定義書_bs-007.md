# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`AdminTorikomiController`）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(UPLOAD_SHAKATSU_DATA).md` からの移行を基に、実装調査で判明した相違点を反映）。

---

## 社活データアップロードAPI

| 項目 | 内容 |
|---|---|
| 機能ID | bs-007 |
| API ID | UPLOAD_SHAKATSU_DATA |
| API名 | 社活データアップロードAPI |
| API説明 | 管理者用データ取り込み画面の「社員活動情報データ CSV取り込み」から呼び出す |
| Method | PUT |
| Path | `/api/upload-shakatsudata`（ベースURL: `http://localhost:8080`。`/api`プレフィックス必須） |
| Request Header | `Content-Type: multipart/form-data`（実装は`MultipartFile`のためこの形式で確定）、認証情報（Cookie `JSESSIONID`）を送信する（axios `withCredentials: true`） |
| 認可 | **`admin`ロール限定**（`hasAuthority("admin")`。`member`ロールでアクセスした場合はHTTP 403） |
| Status Code | 200, 400, 401, 403, 404, 409 |

### Path Variable

なし

### Query String

なし

### Request Body

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 年月 | `yearMonth` | string | 6 / 6 | ○ | 半角数字のみ。`yyyyMM`形式。選択肢は当月から遡って12か月分 |
| 2 | 社員活動情報データファイル | `shakatsuData` | file | - | ○ | 選択された1件目のファイルのみを送信。フォームデータ形式に変換 |

### Response Header

不明（旧設計書に記載なし）

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | 登録件数 | insertCount | number | - | ○ | メッセージ `INFOMATION_SUCCESS_REGIST` で通知 |
| 2 | 削除件数 | deleteCount | number | - | ○ | フロントエンドでは使用していない |

#### 400 Bad Request

> `errorCode`/`message`/`detail`の3項目構成は実装（`RestErrorInfo`）と一致を確認済み。`detail`内部のバリデーションエラー明細（`Field`/`ValidationMessage`）はSpringerフレームワークの規約と整合するが、実機起動でのレスポンス確認は未実施。想定される業務エラーコード: `bs.error.business.ShakatsuDataYearMonthDifference`（ファイル内の年月と送信した年月の不一致）。`AdminTorikomiController`には`ConflictException`用の`@ExceptionHandler`（409）と`ApplicationException`用の`@ExceptionHandler`（業務エラーは400、それ以外は500）が実装されていることを確認済み。

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
{ "insertCount": 45, "deleteCount": 0 }
```

#### Status Code: 400

```json
{ "detail": null, "errorCode": "bs.error.business.ShakatsuDataYearMonthDifference", "message": "ファイル内の年月と選択した年月が一致しません。" }
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`AdminTorikomiController`）と照合し、Path/`admin`ロール限定（403追加）/エラー構造の記載を更新（レスポンス構造は実装と一致確認済み） |
