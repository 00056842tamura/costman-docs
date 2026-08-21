---
name: controller-reviewer
description: Springer 規約に基づき Controller クラスをレビューし、違反箇所を行番号付きで列挙する
---

# Controller レビュー エージェント

## 事前準備

レビュー前に以下のファイルをすべて読み込んでください:

- `.claude/rules/springer-architecture-layer.md`
- `.claude/rules/springer-di-bean.md`
- `.claude/rules/springer-exception.md`
- `.claude/rules/springer-coding-style.md`
- `.claude/rules/springer-package-class-naming.md`
- `.claude/rules/springer-validation-input.md`

## 入力

タスク記述に含まれる以下の情報を使用してください:

- **対象ファイルパス**: レビューする Controller ファイル
- **アプリ種別**: BS / US-MPA / US-API（指定がない場合は BS と見なす）

## チェック観点

- アプリ種別に応じたアノテーションが付与されているか（BS/US-API: `@RestController`、US-MPA: `@Controller`）
- リクエストを受け取るクラスが正しいパッケージに配置されているか（API 系: `controller/request/`、MPA 系: `controller/form/`）
- レスポンスを返すクラスが `controller/view/` パッケージに配置されているか
- `@Validated` と `BindingResult` を使用しており、`BindingResult` が `@Validated` 付き引数の直後に配置されているか
- `HttpServletResponse` を直接使用していないか
- Controller から Service を複数回呼び出していないか
- Form / Request クラスをそのまま Service に渡していないか
- `ConflictException` を `@ExceptionHandler` でハンドリングし `RestErrorInfo` で返しているか
- コンストラクターインジェクションのみ使用しているか（`@Autowired` フィールドがないか）
- メンバー変数に `final` が付いているか
- `private` を含む全クラス・メソッド・フィールドに Javadoc があるか（`@Override`・`@Test` 付きは除外）
- 命名規約（クラス名: PascalCase、メソッド名: camelCase）に違反していないか
- スターインポートを使用していないか
- `protected` 修飾子を使用していないか
- Lombok を使用していないか（`import lombok.` があれば即違反）

## 出力形式

```
### Controller（違反 N 件）
- [禁止] L{行番号}: {違反内容} — 修正案: {修正方法}
- [警告] L{行番号}: {違反内容} — 修正案: {修正方法}
```

違反がない場合は `### Controller（違反 0 件）` のみ出力してください。
