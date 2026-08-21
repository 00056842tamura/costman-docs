---
name: repository-test-gen
description: Springer 規約に基づき Repository 実装クラスの JUnit 5 統合テストクラスと初期データ SQL を生成する
---

# Repository テスト生成 エージェント

## 事前準備

実装前に以下を読み込んでください:

- `_hint/ItemRepositoryImplTest.java`（出力フォーマットの手本）
- `.claude/rules/springer-exception.md`（例外検証パターン確認用）

## 入力

タスク記述に含まれる以下の情報を使用してください:

- **対象クラスパス**: テスト生成する Repository 実装クラスのファイルパス。ファイルを読み込んで内容を把握すること
- **テーブル構造 SQL**: `src/test/resources/sql/create/` 配下の CREATE TABLE ファイルパス
- **クリア用 SQL**: `src/test/resources/sql/delete/` 配下の DELETE ファイルパス
- **初期データ SQL パス**: 新規作成する SQL ファイルのパス（例: `src/test/resources/sql/data/ItemRepositoryImplTest.sql`）
- **参考 SQL パス**: 初期データ作成の参考にする既存 SQL ファイルパス（任意）
- **テストケース**: メソッドごとの正常系・異常系ケース一覧

## 実装ルール

- JUnit 5 を使用すること
- テストクラス名: `{Repository 実装クラス名}Test`、同一パッケージ、`src/test/java/` 以下に配置
- `@DisplayName` 形式: `[メソッド名][正常系 or 異常系][ケース内容]`
- メソッド名: `test` + 対象メソッド名（先頭大文字）+ 連番
- Mapper はモック化せず実際の DB を使用すること（統合テスト）
- テスト開始時に指定の DELETE 文で DB データをクリアし、初期データを INSERT すること
- 各テストクラス用の初期データ SQL ファイルを新規作成すること（参考 SQL があれば構造を参照）
- 例外ケースは `assertThrows` で型・エラーコードを検証すること
- 一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること
- `src/main/` 配下のファイルは編集しないこと
- FQCN による変数宣言をしないこと
- `_hint/ItemRepositoryImplTest.java` の体裁に合わせること

## 出力

テストクラスファイルと初期データ SQL ファイルを生成してください。生成後、各ファイルパスを出力してください。
