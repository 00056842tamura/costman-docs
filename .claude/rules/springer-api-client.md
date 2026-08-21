---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 09 API 呼び出し（RestClient・RestTemplate・SOAP・GraphQL）

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-09-N1 BS からのメッセージをそのまま画面に出さない
US-Repository の例外処理で、BS のレスポンスボディ（`RestErrorInfo.getErrorCode()`）はエラーコード判定にのみ使い、US 層のメッセージコードに置き換える（BS のメッセージを画面に直結しない）。[springer-exception.md](springer-exception.md) と整合。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-09-01 URI 組み立てはプレースホルダーを build で展開する
`queryParam`/`path` のプレースホルダー（`{}`）に対し `build` で値を展開する。プレースホルダーがない場合は `build(Collections.emptyMap())` を指定する。
- 理由 / 背景: この方法に従わないと一部記号が意図通りにエンコードされない。

### R-09-N2 RestClient の例外変換規約（retrieve 使用時）に従う
`retrieve()` 使用時、Springer は `404`→`RestHttpResourceNotFoundException`、`409`→`RestHttpConflictException`、その他 `4xx`→`RestHttpClientErrorException`、`5xx`→`RestHttpServerErrorException`、I/O エラー→`ResourceAccessException` を投げる。

### R-09-N3 US-Repository は受領した例外を US 層メッセージで業務例外にリスローする
RestClient/RestTemplate から例外が返ったら、`RestHttpConflictException`→`ConflictException`、`RestHttpResourceNotFoundException`→`ResourceNotFoundException`、その他はステータス/エラーコードを判断し適切な業務例外にリスロー（US 層のメッセージコードを設定）。

## ✨ 推奨 (PREFER)

### R-09-02 新規構築では RestClient を使用する
新規構築では HTTP クライアントとして RestClient を使用する。
- 理由 / 背景: RestTemplate は将来的に削除予定（Spring 公式アナウンス）。

## 条件付き事項

### R-09-C1 RestClient・RestTemplate をカスタマイズ／複数登録する場合は自身で Bean 登録する
通常は Springer が自動 Bean 登録するため不要。カスタマイズ時・複数登録時は `@Configuration`+`@Bean`（複数時は `@Bean("名")`）で登録する。**自身で 1 つでも登録すると Springer は登録しなくなる**点に注意。複数登録時はインジェクションを `@Qualifier("Bean名")` で指定する。

### R-09-C2 タイムアウト・KeepAlive・コネクションプールは application yml で設定する
共通設定は識別子 `springer.rest`（SOAP は `springer.wstemplate`）、Bean 別個別設定は `springer.rest.clients[]` 配下に記述する（指定名の Bean が無いと起動時エラー）。

### R-09-C3 通信結果取得は retrieve / exchange を選択する
`retrieve()` はボディ取得・自動エラー処理向け（4xx/5xx で自動例外）。`exchange()` はステータス/ヘッダー分岐など低レイヤー制御向けで手動チェック（I/O エラー以外は例外を投げない）。**ファイルダウンロードは exchange を使う**。

### R-09-C4 SOAP は WebServiceTemplate を使用する
SOAP は `WebServiceTemplate` を使用し、有効化→WSDL からインターフェイス生成→`Jaxb2Marshaller`+`WebServiceTemplate` の Bean 登録→yml 設定→`marshalSendAndReceive` で呼び出す。専用 HTTP 例外クラスがないため、`WebServiceTransportException`/`SoapFaultClientException`/`WebServiceIOException` を判定し業務例外でラップする。

### R-09-C5 GraphQL は HttpSyncGraphQlClient を使用する
GraphQL は `HttpSyncGraphQlClient`（RestClient 上に構築）を使用する。`SyncGraphQlClientInterceptor` で errors 部があれば `FieldAccessException` を投げる設定を推奨。GraphQL ドキュメントは `src/main/resources/graphql-documents`（`.graphql`/`.gql`）に配置。例外は GraphQL 固有が `FieldAccessException`、I/O が `ResourceAccessException`。

## 参考・推奨実装パターン

- レスポンスボディ取得: 単一 `body(クラス)`、List `body(new ParameterizedTypeReference<>() {})`、ボディ不要 `toBodilessEntity()`。ヘッダーは `accept()`/`header()`。
- ファイルアップロード/ダウンロードの multipart・ストリーム保存・一時ファイル登録は [springer-file-prevention.md](springer-file-prevention.md) を参照。
- データアクセス例外体系は [springer-exception.md](springer-exception.md)。
