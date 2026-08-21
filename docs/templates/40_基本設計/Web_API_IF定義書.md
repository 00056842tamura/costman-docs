# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | <!-- プロダクト名 --> |
| 作成日 | <!-- YYYY/MM/DD --> |
| 作成者 | <!-- 氏名 --> |
| 最終更新日 | <!-- YYYY/MM/DD --> |
| 最終更新者 | <!-- 氏名 --> |

> 本ファイルは `Web_API_IF定義書_{機能ID}.md` として **1機能ID = 1API** で生成する。
> 機能ID・API IDの対応は `docs/base-design/機能一覧.md`・`Web_API_IF一覧表.md` を正とする。

---

## <!-- API名（例: リソース一覧取得） -->

| 項目 | 内容 |
|---|---|
| 機能ID | <!-- bs-001 --> |
| API ID | <!-- API0001 --> |
| API名 | <!-- リソース一覧取得 --> |
| API説明 | <!-- 検索条件に応じたリソース情報をリストで返却します --> |
| Method | <!-- GET --> |
| Path | <!-- /resources --> |
| Request Header | `Accept: application/json` |
| Status Code | <!-- 200, 400 --> |

### Path Variable

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| <!-- 1 --> | <!-- リソースID --> | <!-- resourceId --> | <!-- string --> | <!-- - / 10 --> | <!-- ○ --> | <!-- 説明 --> |

### Query String

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | <!-- 名称 --> | <!-- name --> | <!-- string --> | <!-- - / 20 --> | <!-- - --> | <!-- 部分一致検索 --> |
| 2 | <!-- ソート順 --> | <!-- order --> | <!-- string --> | <!-- - / - --> | <!-- - --> | <!-- asc / desc --> |

### Request Body

なし

### Response Header

| ステータス | ヘッダー |
|---|---|
| 200 | `Content-Type: application/json` |
| 400 | `Content-Type: application/json` |

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | <!-- ID --> | <!-- id --> | <!-- string --> | <!-- - / 10 --> | <!-- ○ --> | <!-- 説明 --> |
| 2 | <!-- 名称 --> | <!-- name --> | <!-- string --> | <!-- - / 20 --> | <!-- ○ --> | <!-- 説明 --> |
| 3 | <!-- 価格 --> | <!-- price --> | <!-- number --> | <!-- 1 / 999999 --> | <!-- ○ --> | <!-- 最小値1、最大値999999 --> |

#### 400 Bad Request

> ⚠️ 本セクションは `RestErrorInfo` の固定構造（`.claude/rules/springer-exception.md`「API エラーレスポンス構造（RestErrorInfo）」が1次情報）であり、フィールド名・大文字小文字を変更しない。トップレベル（`errorCode`/`message`/`detail`）は camelCase、`detail[]` 内部要素（`Type`/`Field`/`ValidationMessage`）は **PascalCase** という表記の非対称性に注意。

| No. | 論理名 | 物理名 | 型 | 説明 |
|---|---|---|---|---|
| 1 | エラーコード | errorCode | string | エラーコード（メッセージID） |
| 2 | メッセージ | message | string | エラーメッセージ |
| 3 | 明細 | detail | object[]／null | バリデーションエラー時は要素数1以上の配列。対象外の例外では`null` |
| 3-1 | 種別 | Type | string | `GLOBAL`（項目非依存）または `FIELD`（項目に紐づく）。単項目チェックエラー時は通常 `FIELD` |
| 3-2 | フィールド名 | Field | string／null | エラーフィールド名。グローバルエラー（項目に紐づかない相関チェック等）の場合は `null` |
| 3-3 | バリデーションメッセージ | ValidationMessage | string | エラー詳細（人間が読めるメッセージ文言） |

### 電文サンプル

#### Status Code: 200

```json
[
  {
    "id": "R0001",
    "name": "リソース名",
    "price": 1000
  }
]
```

#### Status Code: 400

```json
// 例1: FIELD（単項目チェックエラー）
{
  "errorCode": "error.validation.failed",
  "message": "Validation is failed. Error count:1",
  "detail": [
    {
      "Type": "FIELD",
      "Field": "name",
      "ValidationMessage": "名前は1～20文字で入力してください"
    }
  ]
}
```

```json
// 例2: GLOBAL（相関チェックエラー・項目に紐づかない）
{
  "errorCode": "error.validation.failed",
  "message": "Validation is failed. Error count:1",
  "detail": [
    {
      "Type": "GLOBAL",
      "Field": null,
      "ValidationMessage": "開始日は終了日より前である必要があります"
    }
  ]
}
```

---

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | <!-- YYYY/MM/DD --> | <!-- 氏名 --> | 初版 |
