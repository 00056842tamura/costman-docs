---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 02 技術スタック・依存関係

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-02-01 Lombok を使用しない
Lombok の使用は禁止。
- 理由 / 背景: 想定外の動作をする場合がある／バージョンアップで動作が変わる場合がある。

## ⚠ 非推奨 (AVOID)

### R-02-02 Bean コピーライブラリの使用を避ける
Bean コピーライブラリ（`BeanUtils.copyProperties`・Apache Commons BeanUtils・MapStruct・ModelMapper・Orika・Dozer など）の使用は原則行わない。
- 理由 / 背景: コピー元/先のプロパティ名が同じである必要があり設計として不自然。片方のプロパティ名を変えるとコピーされず不具合に気付きにくい。
- 代替案: フィールドを明示的に詰め替える（コンストラクタ／setter／ファクトリメソッド）。
- 条件付き（→ R-02-C1）: やむを得ず使用する場合は設計確認とテストを必須とする。

## ✅ 必須 (ALWAYS)

### R-02-03 不要な依存関係は常に削除する
`pom.xml` / `build.gradle` から不要な依存関係は常に削除する。
- 備考: 実装例の `pom.xml` も不要依存を持たない実例。

### R-02-05 採用スタックに従う（Spring Boot 中心 ＋ 拡張部品 Springer）
アプリケーションスタックは独自フレームワークではなく、Spring Boot 中心の OSS と拡張部品（Springer）の組み合わせで構成する。OSS でサポートされない機能は拡張部品（Springer）で共通化して利用する。
- 採用 OSS: 認証認可=Spring Security / セッション=Spring Session / Web=Spring Web / バリデーション=Bean Validation + Hibernate Validator（＋Springer）/ ロギング=SLF4J + Logback / SQL Mapper=MyBatis / UI=Vue.js・React 等。

## ✨ 推奨 (PREFER)

### R-02-04 ライブラリバージョンは spring-boot-dependencies 管理のものを使用する
各ライブラリのバージョンは、原則 `spring-boot-dependencies` で管理されているものを使用する。
- 理由 / 背景: `spring-boot-dependencies` のインポートで Spring 提供ライブラリ・依存 OSS・相性 OSS の依存関係が解決される。

## 条件付き事項

### R-02-C1 Bean コピーライブラリをやむを得ず使用する場合
やむを得ず Bean コピーライブラリを使用する場合は、(1) コピー元/先のプロパティ名が一致していることが設計として適切かの確認、(2) プロパティ名の不一致がないことの担保（テストコードの用意など）を実施する。

## 参考・推奨実装パターン

### 採用 OSS 一覧

| カテゴリ | ライブラリ |
|---|---|
| UI フレームワーク | Vue.js / React |
| アプリ基盤 | Spring Boot |
| 認証・認可 | Spring Security |
| セッション | Spring Session |
| Web(Servlet) | Spring Web |
| バリデーション | Bean Validation + Hibernate Validator（未サポートは Springer 拡張） |
| ロギング | SLF4J（API） + Logback（実装） |
| SQL Mapper | MyBatis |
| 拡張部品 | Springer |