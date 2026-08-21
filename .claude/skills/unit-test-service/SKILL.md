---
name: unit-test-service
description: Springer ユニットテスト生成 — Service
---

# Springer ユニットテスト生成 — Service（プロダクト版）

以下のルールに従って、指定された Service 実装クラスのユニットテストを実装してください。

## プロダクト作業時の注意

- テストは対象 Service 実装と同じパッケージの `{type}/src/test/java/...` に配置します
- `_hint/` はプロダクトルート直下を参照します

## 出力フォーマットの参考コード

改行・コメント・インデント等の体裁はプロダクトルート直下の `_hint/ItemServiceImplTest.java` に合わせること。

## テスト実装ルール

### 基本ルール

- JUnit 5 を使用すること
- テストクラス名: `[テスト対象クラス名]Test`（例: `ItemServiceImpl` → `ItemServiceImplTest`）
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

### Repository のモック化

- Repository クラスは Mockito でモック化すること
- `when(...).thenReturn(...)` で戻り値を設定すること
- `verify` で呼び出しを検証すること

### MessageManager の検証

- テスト対象クラスが `MessageManager.getMessage` を呼び出している場合:
  - `MessageManager` をモック化すること
  - `verify` で呼び出しを検証すること（引数の MessageID・パラメータが期待通りであることを確認）

### 例外検証

- 例外がスローされるケースは `assertThrows` で検証すること
- 例外のエラーコード・メッセージ・詳細リスト（`detail`）の内容まで検証すること

### 一時ファイル

- テスト内で一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること

---

## テスト対象と実装すべきケース

$ARGUMENTS
