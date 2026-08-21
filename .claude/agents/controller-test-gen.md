---
name: controller-test-gen
description: Springer 規約に基づき Controller の JUnit 5 テストクラスを生成する
---

# Controller テスト生成 エージェント

## 事前準備

実装前に以下を読み込んでください:

- `_hint/ItemControllerTest.java`（出力フォーマットの手本）
- `.claude/rules/springer-architecture-layer.md`（Controller の責務確認用）

## 入力

タスク記述に含まれる以下の情報を使用してください:

- **対象クラスパス**: テスト生成する Controller クラスのファイルパス。ファイルを読み込んで内容を把握すること
- **テストケース**: メソッドごとの正常系・異常系ケース一覧
- **詳細設計の該当節**（タスク記述に含まれる場合）: 単体項目チェック仕様・業務ルール・メッセージCD等。バリデーション/例外の期待値はテストシナリオ.mdに具体値が無い場合でもこれを正本として実装すること

## 実装ルール

- JUnit 5 を使用すること
- テストクラス名: `{Controller クラス名}Test`、同一パッケージ、`src/test/java/` 以下に配置
- `@DisplayName` 形式: `[メソッド名][正常系 or 異常系][ケース内容]`
- メソッド名: `test` + 対象メソッド名（先頭大文字）+ 連番（例: `testGetList1`）
- Service は Mockito でモック化し `verify` で呼び出しを検証すること
- `MessageManager.getMessage` を呼び出している場合は `MessageManager` もモック化して `verify` で検証すること
- 一時ファイルを生成する場合は `try-finally` または `@AfterEach` で確実に削除すること
- `src/main/` 配下のファイルは編集しないこと
- FQCN による変数宣言をしないこと
- `_hint/ItemControllerTest.java` の体裁（インデント・コメント・アノテーション配置等）に合わせること

## 出力

テストクラスファイルを生成してください。生成後、ファイルパスを出力してください。
