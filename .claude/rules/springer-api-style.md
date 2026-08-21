---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 22 API 方式（REST / GraphQL）

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: トランザクション境界は [springer-service-transaction.md](springer-service-transaction.md)、Controller/レスポンスは [springer-controller.md](springer-controller.md)、例外・データアクセス例外は [springer-exception.md](springer-exception.md)/[springer-api-client.md](springer-api-client.md)。
> 記法: ルールが多いため Style B（REST/GraphQL でグルーピングし `**R-22-xx**` で列挙）。

## 🚫 禁止 (NEVER)

### GraphQL
- **R-22-08** GraphQL で mutation（更新処理）を実装・提供しない。更新処理は従来どおり REST で実装する。
- **R-22-09** 取得データ間で厳密なトランザクションを要求する参照処理を GraphQL で実装しない（REST で実装する）。Spring GraphQL は複数回に分けてデータ取得し、それぞれ別トランザクションになるため。
- **R-22-12** 型システムの `interface` / `union` / `ID` は利用しない（`ID` 型は GraphQL レイヤーで型チェックできず、`union` は Java 型と自然にマッピングできないため）。
- **R-22-13** `@SchemaMapping` を使わず、関連オブジェクト取得は常に `@BatchMapping` を使う（`@SchemaMapping` は N+1 問題で性能劣化するため）。
- **R-22-15** レスポンス（`errors` 部）に例外情報・スタックトレースを含めない（`ExceptionHandlerUtil.getErrorMessageSet` 等でメッセージ化する）。

## ⚠ 非推奨 (AVOID)

### GraphQL
- **R-22-A1** 深い階層・循環構造の型定義を避ける（型階層は 3 階層までを目安とし、必要な場合は深さを制限する）。

## ✅ 必須 (ALWAYS)

### REST
- **R-22-01** 基本的に全 API を RESTful で実装し、成熟度レベル 2（CRUD を GET/POST/PUT(PATCH)/DELETE に分割した状態）を目指す。
- **R-22-02** HTTP メソッドはリソースの CRUD に応じて選択し、HTTP ステータスコードは処理結果に応じて選択する（201 CREATED は POST で、かつ、登録の機能で採用し Location に新規 URI を設定／N 件検索時の 0 件は 404 でなく 200／409 CONFLICT は作成済み・ロック中等の競合）。
- **R-22-03** 単一の API は単一のデータベース（リソース）にのみアクセスする（複数ドメインに跨るファットな API にしない）。
- **R-22-04** トランザクションは API 単位で完結させる（複数 API 呼び出しで 1 トランザクションを構成する場合はリカバリ方式を検討する）。詳細は [springer-service-transaction.md](springer-service-transaction.md)。
- **R-22-06** API ファースト設計を適用する（先に API 仕様を策定・合意し、フロント/バック同時進行と外部公開を可能にする）。
- **R-22-07** エラー発生時は所定項目を設定・返却する（HTTP ステータスコード＝必須、エラーコード＝4xx 時必須〔5xx はレスポンスボディを返さないため対象外〕、関連項目名・関連項目データ・関連処理名＝条件付き）。HTTP 通信の例外は定義済みクラスを使用する（[springer-exception.md](springer-exception.md)/[springer-api-client.md](springer-api-client.md)）。

### GraphQL
- **R-22-10** クエリの同時実行数を制限する（`Instrumentation` を `@Bean` 登録。例: 最大 1 件 `SINGLE_QUERY = 1`、超過時 `AbortExecutionException`）。
- **R-22-11** クエリ深度を制限する（`MaxQueryDepthInstrumentation` を `@Bean` 登録）。
- **R-22-14** バリデーションは 2 段階で実装する（基本ルール＝GraphQL スキーマの `!`（NonNull）・データ型、詳細＝Java 側でリクエストオブジェクトに `@Size`/`@NotEmpty` 等＋Controller 引数に `@Validated`）。
- **R-22-16** 検索性能を担保するうえで必要な項目は必須項目とする（任意項目にしない）。

## ✨ 推奨 (PREFER)

（なし）

## 条件付き事項

### R-22-C1 API バージョン変更の要否（REST）
API バージョンは API I/F が変更になる場合に変更する。ただし「新たな項目追加のみ」かつ「その項目が非必須」かつ「追加項目を使わない呼び出しでレスポンスが従前と変わらない」場合は変更しない。API I/F 変更を伴わずアプリ実装のみ変更する場合はアプリケーションバージョンのみ変更する。バージョン変更時はアーカイブ(jar)を複数バージョン並走で管理し、廃止バージョンへのアクセスは API-GW でエラー（404）を返す。

### R-22-C2 GraphQL を採用してよい条件
GraphQL は「参照処理」「ビジネスロジック提供でなくデータアクセス手段提供が目的」「厳密なトランザクションが不要」「BS での利用」の全条件を満たす場合のみ採用する。いずれかを満たさない場合は REST で実装する。更新と参照の両方が必要で参照がメインの場合は「更新＝REST／参照＝GraphQL」で実装できる。

### R-22-C3 API バージョンはパスへの組み込みを必須としない
API バージョンをパスに組み込むことは必須ではなく、必要な場合（上記 R-22-C1 に該当する変更が生じる場合等）に組み込む。組み込む場合は HTTP ヘッダ/クエリではなくパスを使用する。

### R-22-C4 R-22-07「関連項目名・関連項目データ・関連処理名」と RestErrorInfo の対応関係
出典（`001.REST_API方式.md` 2.2.4「エラー方針」の「エラー時返却内容一覧」）に基づき、R-22-07 の3用語と [springer-exception.md](springer-exception.md) の `RestErrorInfo` 構造の対応は以下のとおり（3者は一様に対応するわけではない）。
- **関連項目名** → `detail[].Field` に対応する（バリデーションエラーの該当箇所を特定する必要がある場合に設定）。
- **関連項目データ**（項目名だけではエラー内容を特定できない場合に必要な、実際の入力値）→ **現行の `RestErrorInfo.detail[]`（`Type`/`Field`/`ValidationMessage`）に対応するフィールドは存在しない**（構造上の既知のギャップ）。必要な場合は `ValidationMessage` のメッセージ文言に値を含める以外の手段がない。
- **関連処理名**（外部システム機能単位のエラーが発生した場合に必要）→ **バリデーションエラーの `detail[]` とは異なる場面**（外部システム連携時のエラー）を指す。単一 API の 400 バリデーションエラー電文（`detail[]`）の項目としては対象外であり、データアクセス例外（HTTP通信。[springer-api-client.md](springer-api-client.md)）側の扱いに属するが、対応フィールドの詳細は未確認（原典の参照先〔`101.例外ハンドリング_Detail.md`〕は本リポジトリで未入手）。

## 参考・推奨実装パターン

- REST の URL 設計: スキーム＝HTTP(S)、第 1 階層＝/アプリケーション名、第 2 階層＝API バージョン（例 `/v1`）、第 3 階層以下＝操作対象リソース名・子リソース名を順次、検索条件はクエリパラメータ。
- GraphQL の構成: Controller は `@Controller`＋`@QueryMapping`（type Query 対応・メソッド名＝クエリ名・引数に `@Argument`）／`@BatchMapping`（メソッド名＝主オブジェクトのフィールド名・引数＝主オブジェクトの List・戻り値＝主⇔関連の Map）。例外は `@ControllerAdvice`＋`@GraphQlExceptionHandler` で処理し、`errors` を HTTP 200 で応答する（通信経路・認証等の GraphQL 外エラーは 4xx/5xx）。
- GraphQL のスキーマは `*.graphqls`（`src/main/resources/graphql/schema.graphqls`）。非デフォルトスカラー型は拡張スカラー（`graphql-java-extended-scalars`）またはカスタムスカラー（`Coercing` 実装＋`GraphQLScalarType`＋登録）で導入する。`[Long!]` 等の配列型引数は非配列の単一値も許容される点に注意。
