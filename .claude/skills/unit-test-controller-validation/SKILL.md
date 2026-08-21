---
name: unit-test-controller-validation
description: Springer ユニットテスト生成 — Controller バリデーション
---

# Springer ユニットテスト生成 — Controller バリデーション（プロダクト版）

以下のルールに従って、指定された Controller クラスのバリデーションテストを実装してください。

## プロダクト作業時の注意

- テストは対象 Controller と同じパッケージの `{スタック}/src/test/java/...` に配置します
- `_hint/` はプロダクトルート直下を参照します

## 出力フォーマットの参考コード

改行・コメント・インデント等の体裁はプロダクトルート直下の `_hint/ItemControllerValidationTest.java` に合わせること。

## テスト実装ルール

### 基本ルール

- JUnit 5 を使用すること
- テストクラス名: `[テスト対象クラス名]ValidationTest`（例: `ItemController` → `ItemControllerValidationTest`）
- テストクラスのパッケージ: テスト対象クラスと同一パッケージ
- 配置ディレクトリ: `/src/test/java/` 以下
- mainフォルダ以下のファイルは編集しないこと
- FQCNによる変数宣言をしないこと

### テストメソッド

- `@DisplayName` を付与し、以下の形式で内容を日本語で記載すること
  - 形式: `[テスト対象メソッド名][正常系/異常系][ケース内容]`
  - 例: `@DisplayName("[getList][異常系][メーカーID文字種チェックエラー]")`
- メソッド名: `test` + テスト対象メソッド名（先頭大文字）+ 連番
  - 例: `testGetList1`、`testGetList2`

### バリデーションテストの方針

- `MockMvc` を使用してリクエストを送信し、バリデーションエラーを検証すること
- 期待されるエラーフィールド・エラーメッセージキーを検証すること
- Service クラスはモック化し、バリデーションエラー時に Service が呼ばれないことを確認すること

### 一時ファイル

- テスト内で一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること

---

## テスト対象と実装すべきケース

$ARGUMENTS
