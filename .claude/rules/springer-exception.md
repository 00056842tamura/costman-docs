---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 10 例外ハンドリング

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-10-02 Service でチェック例外を throw しない
Service での「チェック例外」の `throw` は禁止。例外時は非チェックの「業務例外」または「システム例外」をスローする。
- 理由 / 背景: チェック例外をスローするとトランザクションがロールバックされずコミットされてしまう。

### R-10-03 Service でデータアクセス例外・システム例外を catch しない
Service で「データアクセス例外」「システム例外」を catch しない。データアクセス例外を扱う必要がある場合も、必ず上位概念の「業務例外」型としてハンドリングする。
- 理由 / 背景: Service が具体的なデータアクセス手段を意識すると Repository の仕様変更が波及するため。

### R-10-04 Controller でデータアクセス例外・システム例外をハンドリングしない
Controller で「業務例外」以外（データアクセス例外・システム例外）を catch または `@ExceptionHandler` でハンドリングせず、グローバル例外ハンドラーに委譲する（ステータス 500 が返る）。
- 補足: 特に「システム例外」は `@ExceptionHandler` ではハンドリングできない。

### R-10-05 BS のエラーメッセージを US の呼び出し元クライアントに直接出力しない
US は BS から返るエラーコードを判別し、US 独自のエラーコード/メッセージに詰め替えて再スローする。該当しないエラーコードはシステム例外に変換する。

## ⚠ 非推奨 (AVOID)

### R-10-N1 try-catch による例外ハンドリングは @ExceptionHandler で実現できない場合のみ
Controller の例外ハンドリングは原則 `@ExceptionHandler` で行う。レスポンスにリクエスト情報を設定する場合など `@ExceptionHandler` で実現できない場合に限り `try-catch` を使う。
- 代替案: まず `@ExceptionHandler`（複数 Controller 共通なら `@ControllerAdvice`）を検討する。

## ✅ 必須 (ALWAYS)

### R-10-01 明示的に throw する例外は業務例外・システム例外に限定し原因例外をラップする
Service・Repository で明示的に `throw` する例外は「業務例外」と「システム例外」に限定し、原因例外をネスト（ラップ）する（コンストラクタ末尾引数で原因例外を渡す）。
- 補足: Repository で catch する例外は、RestClient/RestTemplate や MyBatis の「データアクセス例外」が主。

### R-10-06 独自例外は ApplicationException を継承する
独自の例外クラスを作成する場合は `ApplicationException` を継承する。

### R-10-07 API エラーレスポンスの detail[] 要素名は大文字小文字を含めそのまま使用する
API のバリデーションエラー（400）レスポンスの `detail[]` 要素は `Type`／`Field`／`ValidationMessage`（**先頭大文字＝PascalCase**）を持つ。設計書生成・実装のいずれでも、このフィールド名を省略・言い換え・大文字小文字変更をせずそのまま使用する（推論で `type`／`field`／`validationMessage` 等の camelCase に書き換えない）。
- 理由 / 背景: トップレベル（`errorCode`/`message`/`detail`）は camelCase だが、ネストした `detail[]` 内部要素は PascalCase という表記の非対称性が拡張部品の実際の仕様であり、一般的な JSON 命名慣習（camelCase 統一）からの類推で書き換えると誤りになる（詳細は「参考・推奨実装パターン」の構造表を参照）。

### R-10-N2 継続可能/不可能で例外の責務を分ける
例外は「継続可能（＝業務例外）」と「継続不可能（＝システム例外）」に分類する。継続可能な例外の捕捉・レスポンス責務はアプリケーションにあり、継続不可能な例外の責務は拡張部品（グローバル例外ハンドラー等）にある。継続可能/不可能の判断は、例外をスローした拡張部品/ライブラリを呼び出したレイヤ内（例: Repository）で行う。

### R-10-N3 継続不可能と判断した例外はアプリで catch しない
DB 接続エラー・SQL 構文エラー・HTTP 500 系など「継続不可能」とする例外はアプリで catch せず拡張部品にハンドリングさせる。

### R-10-N4 Repository でデータアクセス例外を業務例外でラップする
MyBatis/RestClient 等がスローするデータアクセス例外を Repository で catch し、想定される業務例外でラップしてスローする（例: `DBDuplicateKeyException`→`ConflictException`、`RestHttpConflictException`→`ConflictException`）。

## ✨ 推奨 (PREFER)

（なし。Controller は原則 `@ExceptionHandler`＝R-10-N1 参照）

## 条件付き事項

### R-10-C1 エラーコードを設定しない場合はラップせずエスカレーション可
`DBDuplicateKeyException`・`RestHttpConflictException` は `ConflictException` のサブクラス、`RestResourceNotFoundException`は`ResourceNotFoundException`のサブクラスのため、エラーコードを設定しない場合はラップせずそのままエスカレーションすることもできる。

### R-10-C2 Repository は処理結果がエラー内容なら自発的に例外をスローする
ライブラリから例外が出なくても、更新件数が想定と異なる／ステータス 200 のレスポンスボディがエラーを表す場合などは、Repository が自発的に業務例外またはシステム例外をスローする。

### R-10-C3 API リクエスト時の業務例外のステータスコード
API では `ApplicationException`→400、`ResourceNotFoundException`→404、`ConflictException`→409 を返す。

### R-10-C4 システム例外を横断ハンドリングする場合は SystemExceptionHandler を実装し Bean 登録する
システム例外を横断的に扱う（レスポンスボディを返す等）必要がある場合は `SystemExceptionHandler` を実装して Bean 登録する。実装しない場合はグローバル例外ハンドラーに委譲される。

### R-10-C5 グローバル例外ハンドラーのレスポンス種別を設定で制御する
未ハンドリング例外はグローバル例外ハンドラーが処理し、`Accept`/`X-Requested-With` でレスポンス種別（画面/API）を判別する。判別不能時は `springer.exceptionhandler.response-type`（`ANY`/`VIEW_ONLY`/`REST_ONLY`）で制御する（BS は `REST_ONLY`、Thymeleaf+Ajax の US は `ANY`）。

## 参考・推奨実装パターン

### 例外クラス体系

**業務例外**: `ApplicationException`（汎用業務）/ `ResourceNotFoundException`（404）/ `ConflictException`（重複・排他競合, 409）

**データアクセス例外（DB: MyBatis）**: `DBDuplicateKeyException`（一意制約）/ `DBPessimisticLockingFailureException`（悲観排他）/ `DBOptimisticLockingFailureException`（楽観排他）/ `DBCannotGetJdbcConnectionException`（JDBC 接続）/ `DBDataAccessException`（その他 SQL）

**データアクセス例外（HTTP: RestClient/RestTemplate）**: `RestHttpResourceNotFoundException`（404）/ `RestHttpConflictException`（409）/ `RestHttpClientErrorException`（その他 400 系）/ `RestHttpServerErrorException`（500 系）/ `RestResourceAccessException`（I/O・RestTemplate）/ `ResourceAccessException`（I/O・RestClient）

**システム例外**: `SystemException`（汎用システム）

### API エラーレスポンス構造（RestErrorInfo）

API エラーレスポンスは拡張部品の `RestErrorInfo` で返す（`new RestErrorInfo(e)`）。**この構造は設計書・実装で一字一句（大文字小文字を含め）転記する対象であり、推論で書き換えない（R-10-07）。**

**トップレベル構造**（キー名は camelCase）:

| フィールド | 型 | 説明 |
|---|---|---|
| `errorCode` | string | エラーコード（メッセージID）。`springer.exceptionhandler.error-codes-...` の設定でカスタマイズ可 |
| `message` | string | エラーメッセージ。同上でカスタマイズ可 |
| `detail` | object／object[]／null | バリデーションエラー時は配列で設定。対象外の例外では**キー自体は省略されず値が `null`** になる |

**`detail[]`（バリデーションエラー時のみ配列で設定）の内部要素**（キー名は **PascalCase**。トップレベルとは表記が異なる）:

| フィールド | 型 | 説明 |
|---|---|---|
| `Type` | string（`GLOBAL`／`FIELD`） | 相関チェック等で項目に紐づかない場合は `GLOBAL`、単項目チェックは `FIELD`。**既定では非出力**（出力するには `springer.exceptionhandler.validation-error.hide-type` を設定） |
| `Field` | string／null | エラーフィールド名。`Type=GLOBAL`（グローバルエラー）の場合は `null` |
| `ValidationMessage` | string | エラー詳細メッセージ |

### Spring MVC 例外のステータス対応（拡張部品が自動処理）

| 例外 | ステータス | レスポンスボディ | `errorCode`/`message` | `detail` |
|---|:--:|:--:|:--:|:--:|
| `MethodArgumentNotValidException`／`TypeMismatchException`／`ConstraintViolationException`（入力チェック・型変換） | 400 | あり | あり | あり（配列） |
| `MissingServletRequestParameterException`／`ServletRequestBindingException`／`HttpMessageNotReadableException`／`MissingServletRequestPartException` | 400 | あり | あり | **`null`**（キーは省略されない） |
| `HttpRequestMethodNotSupportedException` | 405 | なし | なし | なし |
| `HttpMediaTypeNotSupportedException` | 415 | なし | なし | なし |
| `HttpMediaTypeNotAcceptableException` | 406 | なし | なし | なし |
| `NoHandlerFoundException` | 404 | なし | なし | なし |
| `MissingPathVariableException`／`ConversionNotSupportedException`／`HttpMessageNotWritableException` | 500 | なし | なし | なし |
| `AsyncRequestTimeoutException` | 503 | なし | なし | なし |

> いずれも拡張部品（グローバル例外ハンドラー）が処理し、アプリではハンドリングしない（R-10-04）。
