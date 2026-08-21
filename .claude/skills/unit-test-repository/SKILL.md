---
name: unit-test-repository
description: Springer ユニットテスト生成 — Repository
---

# Springer ユニットテスト生成 — Repository（プロダクト版）

以下のルールに従って、指定された Repository 実装クラスのユニットテストを実装してください。

## プロダクト作業時の注意

- テストは対象 Repository 実装と同じパッケージの `{スタック}/src/test/java/...` に配置します
- SQL ファイルは `{スタック}/src/test/resources/sql/...` に配置します
- `_hint/` はプロダクトルート直下を参照します

## 出力フォーマットの参考コード

改行・コメント・インデント等の体裁はプロダクトルート直下の `_hint/ItemRepositoryImplTest.java` に合わせること。

## テスト実装ルール

### 基本ルール

- JUnit 5 を使用すること
- テストクラス名: `[テスト対象クラス名]Test`（例: `ItemRepositoryImpl` → `ItemRepositoryImplTest`）
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

### Mapper のモック化方針

- **Mapper クラスはモック化せずに実際の DB を使用すること**（統合テスト）

### DB テストデータの準備

- テスト開始時に指定された DELETE 文で DB データをクリアすること
- クリア後、テストに必要な初期データを INSERT 文で投入すること
- 各テストクラス用の初期データ SQL ファイルを新規作成すること
- 初期データの `INSERT` 文は参考 SQL ファイルを基に作成すること

### 例外検証

- 例外がスローされるケースは `assertThrows` で検証すること
- 例外の型・エラーコードを検証すること

### 一時ファイル

- テスト内で一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること

---

## テスト対象と DB 情報

以下の情報を指定してください:

- **テスト対象クラス**: （例: `ItemRepositoryImpl`）
- **テーブル構造 (CREATE 文 SQL ファイルパス)**: （例: `src/test/resources/sql/create/T_ITEM.sql`）
- **クリア用 SQL ファイルパス**: （例: `src/test/resources/sql/delete/T_ITEM.sql`）
- **初期データ SQL ファイルパス（新規作成）**: （例: `src/test/resources/sql/data/ItemRepositoryImplTest.sql`）
- **初期データ参考 SQL ファイルパス**: （例: `src/test/resources/sql/data/sample_insert.sql`）

## 実装すべきケース

$ARGUMENTS
