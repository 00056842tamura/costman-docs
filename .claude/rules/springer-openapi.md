---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 21 OpenAPI / Swagger 仕様書

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: Controller は [springer-controller.md](springer-controller.md)、依存・バージョンは [springer-tech-stack-dependency.md](springer-tech-stack-dependency.md)、入力チェック（`@Schema(hidden)`）は [springer-validation-input.md](springer-validation-input.md)。

## 🚫 禁止 (NEVER)

### R-21-N1 生成用 API エンドポイントを本番・開発環境で公開したままにしない
`/v3/api-docs`（Springfox は `/v2/api-docs`）等の swagger.json 生成用 API を本番・開発環境で公開しない（R-21-02 で無効化する）。
- 理由 / 背景: 生成用 API は公開対象でないため（springdoc の仕様）。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-21-01 BS（内部 API）とオープン型 US は OpenAPI 対応を必須とする
内部 API に位置づけられる BS 層アプリケーション、および不特定多数のサービスから利用されるオープン型 API を提供する US 層アプリケーションは、OpenAPI(Swagger) 対応を必須とする。
- 理由 / 背景: API Gateway Management Tool への API 登録時に Swagger ファイルが必要なため。

### R-21-02 デプロイ時は api-docs を無効化する
本番環境・開発環境へデプロイする場合は `springdoc.api-docs.enabled: false` を設定する。
- 補足: ローカルでの swagger.json 取得時のみ有効化する運用。

### R-21-03 swagger.json はプロジェクト直下に配置し Git 管理する
生成した `swagger.json` をプロジェクト直下（`pom.xml` と同じ階層）に配置し Git（原典は GitLab）で管理する。作成後は Swagger Editor（editor.swagger.io）で仕様の不備をチェックする。

### R-21-04 ハンドラー・モデルに OpenAPI アノテーションを付与する
- Controller クラス: `@Tag(name=...)`（複数は `@Tags`）。
- ハンドラーメソッド: `@Operation(summary, description)` ＋ `@ApiResponses`/`@ApiResponse(responseCode, description, content, headers)`。
- 引数: `@PathVariable`/`@RequestParam` に `@Parameter(description=...)`。GET のクエリストリングを POJO で受ける場合は `@ParameterObject`（POST の JSON ボディには不要）。
- モデル/プロパティ: `@Schema(description=...)`。関連項目チェック用メソッド（`@AssertTrue` 等）は `@Schema(hidden = true)` でモデル定義から除外する。
- 補足: 上記は springdoc（v1/v2）系。Springfox 採用時は別アノテーション体系（R-21-C1）。

## ✨ 推奨 (PREFER)

### R-21-05 override-with-generic-response は無効にする
`springdoc.override-with-generic-response` は無効（`false`）にする。
- 理由 / 背景: 有効だと誤った内容が出力される可能性があるため。

## 条件付き事項

### R-21-C1 ライブラリはバージョンで選定する
- Springer 2.0 以降（Spring Boot 3.0 以降）: **springdoc-openapi v2**（`org.springdoc:springdoc-openapi-starter-webmvc-api`）。`@ParameterObject` の import は `org.springdoc.core.annotations`。
- Springer 1.5〜1.7（Spring Boot 2.6〜2.7）: **springdoc-openapi v1**（`org.springdoc:springdoc-openapi-webmvc-core`）。`@ParameterObject` の import は `org.springdoc.api.annotations`。
- Springer 1.4 以前（Spring Boot 2.5 以前）: **Springfox**（`io.springfox:springfox-swagger2:2.9.2`）。アノテーションは `@EnableSwagger2`＋`Docket` Bean・`@Api(tags)`・`@ApiOperation`・`@ApiResponse(code, message)`・`@ApiModelProperty`。api-docs URL は `/v2/api-docs`。
- 制約: Springfox は Springer 1.5（Spring Boot 2.6）以降は使用不可。springdoc v1 は Springer 1.5〜1.7 の間のみ使用可。
- 注記: 原典は Springer 2.0 以降＝springdoc v2 までを記載（Springer 3 / Spring Boot 4 への明示記述はなし）。② サンプル（Spring Boot 4 系）も springdoc v2 を使用しており、Springer 3 でも springdoc v2 を採用する。

### R-21-C2 springdoc のスキャン・出力設定を行う
`springdoc.packages-to-scan`（対象リクエストハンドラーのパッケージ。複数はカンマ区切り）、`springdoc.paths-to-match`（対象 URL）、`springdoc.default-produces-media-type`（既定 Accept、例 `application/json`）を設定する。

### R-21-C3 既知不具合に注意する
- `MultipartFile` を含むモデルの `@Schema` 内容が反映されない不具合あり（springdoc v1 1.6.15／v2 2.0.2、いずれも 2023-04-06 時点）。
- Springfox 2.9.2 はマルチパートリクエスト出力に Swagger 文法エラーを含むため、出力後に `swagger.json` を手動修正する。

## 参考・推奨実装パターン

- 設定クラスで `OpenAPI` Bean を定義し、`Info`（`title`/`description`/`version`）を設定する（springdoc）。Springfox は `Docket`（`DocumentationType.SWAGGER_2`）＋`ApiInfo` を Bean 定義する。
- swagger.json は SoapUI/Postman 等で api-docs URL に GET し、レスポンス JSON を保存して取得する。
