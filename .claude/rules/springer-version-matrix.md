---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# Springer バージョン対応表（Springer / Spring Boot / Java）

> 本ファイルは `.claude/rules/springer-*.md` 各所・スタック `CLAUDE.md`・`_templates/*/CLAUDE.md`・`.claude/rules/product-rules.md` から参照される Springer/Spring Boot/Java の対応関係の正本。各ルールファイルにバージョン分岐の記述がある場合は当該ファイルの記述を優先し、本ファイルは横断的な参照用の一覧とする。

## バージョン対応表

| Springer | Spring Boot | Java | 主な特徴・分岐点 |
|---|---|---|---|
| 1.4 以前 | 2.5 以前 | 8 / 11 | Springfox（`io.springfox:springfox-swagger2:2.9.2`）。`WebSecurityConfigurerAdapter` 継承が必要 |
| 1.5〜1.7 | 2.6〜2.7 | 11 / 17 | springdoc-openapi v1。1.6 以上で `WebSecurityConfigurerAdapter` 継承不要・`mvcMatchers()`＋`hasRole()`（`ROLE_` プレフィックス付与） |
| 2.0 以上 | 3.0 以降 | 17 | springdoc-openapi v2。`requestMatchers()`＋`hasAuthority()`（プレフィックス不要）。Micrometer Tracing（W3C 伝搬）。トレース相関ログ `logging.pattern.correlation` |
| **3** | **4 系** | **25** | Springer 2 系の構成を継承（springdoc v2・`requestMatchers()`・Micrometer Tracing）。Spring Boot 4 系固有の差分は原典に明記なし。差分が判明した際に本表へ追記する |

## 本プロダクトでの適用

- `{product}` の全 Springer スタック（`bs/`・`us-mpa/`・`us-api/`・`batch/`）は **Springer 3（Spring Boot 4）で確定・統一する**。各スタック `CLAUDE.md` に Springer 3 を宣言する。
- フレームワーク仕様上、1 プロダクト内で Springer 2 と 3 の混在自体は許容される（`.claude/rules/product-rules.md`）が、本プロダクトでは 3 に統一しており混在しない。将来混在を要する場合は本表の該当行に従って各スタックごとにバージョン分岐点（OpenAPI ライブラリ・SecurityConfig 記法・トレーシング方式）を確認する。

## 関連ルール

- OpenAPI ライブラリ選定: `.claude/rules/springer-openapi.md` R-21-C1
- SecurityConfig の認可記法: `.claude/rules/springer-security-auth.md` R-13-C1
- トレーシング（Micrometer Tracing / Spring Cloud Sleuth）: `.claude/rules/springer-resilience.md`
- Azure Spring Cloud の対応バージョン: `.claude/rules/springer-cloud-storage-secret.md`
