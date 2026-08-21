---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 07 Service・トランザクション制御

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-07-02 Service の実装クラス以外で @Transactional を使用しない
`@Transactional` は BS-Service の実装クラス（またはそのメソッド）以外で使用しない。
- 補足: トランザクション境界＝BS 機能単位の趣旨から禁止とする。

### R-07-03 Controller から Service を複数回呼び出さない
Controller から Service を複数回呼び出さない（複数トランザクションになるため）。
- 補足: トランザクション境界＝機能単位の趣旨から禁止とする。

### R-07-04 同一クラス内の @Transactional メソッドを呼び出さない
同一クラス内での `@Transactional` メソッドの自己呼び出しをしない。
- 理由 / 背景: Spring AOP プロキシ経由でないとトランザクションが開始されないため（自己呼び出しでは効かない）。

### R-07-N1 Service でデータアクセス例外を catch しない
Service でデータアクセス例外（DB アクセス／HTTP 通信）を catch しない（[springer-exception.md](springer-exception.md) と整合。Repository で扱う）。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-07-01 DB アクセスは Service でトランザクション制御する
DB アクセスは CRUD によらず、必ず BS-Service で `@Transactional` によるトランザクション制御を行う。
- 理由 / 背景: トランザクション境界は「BS 層の API（機能）単位」と定義され、DB の一貫性・整合性を保証するため。

### R-07-N2 トランザクション分離レベルは READ COMMITTED とする
DB のトランザクション分離レベルは `READ COMMITTED` とする。

### R-07-N3 自動コミットを無効化する
自動コミットは行わない（HikariCP では `spring.datasource.hikari.auto-commit: false`）。

### R-07-N4 ロールバック対象は RuntimeException
Controller に `RuntimeException` が返されたときにロールバックされる（Spring の宣言的トランザクションの既定挙動）。

## ✨ 推奨 (PREFER)

### R-07-05 @Transactional はクラスに付与する
`@Transactional` はクラスに付与する。
- 条件: トランザクション制御不要なメソッドがある場合のみ、制御が必要なメソッドに個別付与する。

### R-07-N5 ビジネスロジックは原則 BS-Service に実装する
ビジネスロジック（チェック・判断・計算）は通常 BS に実装する。US-Service は Repository 呼び出しのための情報加工・呼び分け・他 Service 呼び出しを行う。

## 条件付き事項

### R-07-C1 US-Service は非トランザクションとする
US の Service クラスには `@Transactional` を付与しない。
- 条件: US/BS が分かれていないアーキテクチャの場合は BS-Service のルールに従う。

### R-07-C2 チェック例外を投げてもコミットされる点に注意する
正常終了時、または Controller にチェック例外が返されたときはコミットされる。チェック例外を投げても更新はコミットされる（ロールバックさせたい場合は別途 `rollbackFor` 等の指定が必要）。

### R-07-C3 トランザクションは「他クラスからの呼び出し」で開始される
`@Transactional` 付きクラスのメソッドが他のクラスから呼ばれたときに開始され、呼び出し元へ戻るときに終了（コミット/ロールバック判定）する。自己呼び出しでは開始されない（R-07-04 と表裏）。

## 参考・推奨実装パターン

- BS-Service テンプレート: `@Service @Transactional public class XxxServiceImpl implements XxxService`、Repository をコンストラクターインジェクション。US-Service: `@Service` のみ（`@Transactional` なし）。
- 排他制御（楽観/悲観ロック）は [springer-repository-mybatis.md](springer-repository-mybatis.md)。
