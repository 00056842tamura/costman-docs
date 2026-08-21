---
name: unit-test-controller
description: Springer ユニットテスト生成 — Controller
---

# Springer ユニットテスト生成 — Controller（プロダクト版）

以下のルールに従って、指定された Controller クラスのユニットテストを実装してください。

## プロダクト作業時の注意

- テストは対象 Controller と同じパッケージの `{スタック}/src/test/java/...` に配置します
- `_hint/` はプロダクトルート直下を参照します

## 出力フォーマットの参考コード

改行・コメント・インデント等の体裁はプロダクトルート直下の `_hint/ItemControllerTest.java` に合わせること。

## テスト実装ルール

### 基本ルール

- JUnit 5 を使用すること
- テストクラス名: `[テスト対象クラス名]Test`（例: `ItemController` → `ItemControllerTest`）
- テストクラスのパッケージ: テスト対象クラスと同一パッケージ
- 配置ディレクトリ: `/src/test/java/` 以下
- mainフォルダ以下のファイルは編集しないこと
- FQCNによる変数宣言をしないこと

### テストメソッド

- `@DisplayName` を付与し、以下の形式で内容を日本語で記載すること
  - 形式: `[テスト対象メソッド名][正常系/異常系][ケース内容]`
  - 例: `@DisplayName("[getItemList][正常系][商品情報3件取得成功]")`
- メソッド名: `test` + テスト対象メソッド名（先頭大文字）+ 連番
  - 例: `testGetList1`、`testGetList2`

### Service のモック化

- Service クラスは Mockito でモック化すること
- Mock の呼び出し検証には `verify` を使用すること

### MessageManager の検証

- テスト対象クラスが `MessageManager.getMessage` を呼び出している場合:
  - `MessageManager` をモック化すること
  - `verify` で呼び出しを検証すること（引数の MessageID・パラメータが期待通りであることを確認）

### 一時ファイル

- テスト内で一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること

---

## テスト対象と実装すべきケース

$ARGUMENTS
