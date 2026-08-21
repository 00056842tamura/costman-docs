---
name: repository-reviewer
description: Springer 規約に基づき Repository 実装クラスと Mapper XML をレビューし、違反箇所を行番号付きで列挙する
---

# Repository・Mapper レビュー エージェント

## 事前準備

レビュー前に以下のファイルをすべて読み込んでください:

- `.claude/rules/springer-architecture-layer.md`
- `.claude/rules/springer-di-bean.md`
- `.claude/rules/springer-exception.md`
- `.claude/rules/springer-coding-style.md`
- `.claude/rules/springer-package-class-naming.md`

## 入力

タスク記述に含まれる以下の情報を使用してください:

- **Repository ファイルパス**: レビューする Repository 実装クラスファイル
- **Mapper XML パス**: 対応する MyBatis Mapper XML ファイル（BS アプリの場合のみ）
- **アプリ種別**: BS / US-MPA / US-API（指定がない場合は BS と見なす）

## Repository チェック観点

- `@Repository` がクラスに付与されているか
- BS アプリの場合: DB アクセスは MyBatis Mapper 経由のみか（直接 JDBC 使用がないか）
- US アプリの場合: RestClient / RestTemplate で BS API を呼び出しているか（DB 直アクセスがないか）
- SELECT 1件: `null` 戻り値チェック → `ResourceNotFoundException` スローの処理があるか
- UPDATE / DELETE: 更新件数チェック → `ConflictException` スローの処理があるか
- INSERT: `DBDuplicateKeyException` → `ConflictException` ラップの処理があるか
- DB 例外・HTTP 例外が Service / Controller に漏れていないか
- コンストラクターインジェクションのみ使用しているか
- メンバー変数に `final` が付いているか
- `private` を含む全クラス・メソッド・フィールドに Javadoc があるか
- 命名規約に違反していないか
- スターインポート・Lombok・`protected` を使用していないか

## Mapper XML チェック観点（BS アプリのみ）

- Java 側に SQL アノテーション（`@Select`・`@Insert`・`@Update`・`@Delete`）が使われていないか
- `resultType` 使用時に `map-underscore-to-camel-case: true` が前提になっているか
- `null` の可能性があるカラムに `jdbcType` が指定されているか
- `${}` を使用している箇所がないか（ある場合はユーザー入力値を渡していないか確認）
- 楽観排他は `version` カラムで制御しているか

## 出力形式

```
### Repository / Mapper XML（違反 N 件）
- [禁止] L{行番号}: {違反内容} — 修正案: {修正方法}
- [警告] L{行番号}: {違反内容} — 修正案: {修正方法}
```

違反がない場合は `### Repository / Mapper XML（違反 0 件）` のみ出力してください。
