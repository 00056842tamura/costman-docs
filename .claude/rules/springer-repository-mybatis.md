---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 08 Repository・DB アクセス（MyBatis）・排他制御

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: Repository のクラス定義/DI は [03](springer-package-class-naming.md)/[04](springer-di-bean.md)、例外変換は [10](springer-exception.md)。

## 🚫 禁止 (NEVER)

### R-08-N1 SQL を文字列結合で組み立てない（プレースホルダを使う）
値・条件値は `#{}`（PreparedStatement の `?` に展開）を使う。`${}` は無エスケープ展開のため、テーブル名・カラム名などプレースホルダにできない箇所に限定する（SQL インジェクション注意）。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-08-01 MyBatis の Mapper インターフェイスを定義する
Mapper インターフェイスを定義し `@Mapper` を付与する。戻り値型は SELECT 1 件→行マッピングクラス、複数→`List<E>`（大量時 `Cursor<T>`）、更新系→`int`（件数）/`boolean`（更新有無）/`void` 等。

### R-08-02 SQL は Mapper XML ファイルに定義する
SQL 文は MyBatis のアノテーションではなく **Mapper XML** に定義する。
- 補足: アノテーションによる SQL 定義は不可とし、XML を使用する方法のみを許可する。

### R-08-03 Mapper XML は対応インターフェイスと同名・同階層に配置する
XML ファイル名は対応インターフェイスと同名、保存場所は `src/main/resources/` 配下の同一パッケージ。`<mapper namespace>` に対応インターフェイスをフルパス指定し、`<select>`/`<insert>`/`<update>`/`<delete>` の `id` をメソッド名に一致させる。

### R-08-04 null 可能カラムに jdbcType を指定する
`INSERT`/`UPDATE`/`DELETE` で `null` が設定される可能性のあるカラムに `#{col, jdbcType=XXX}` を指定する。

### R-08-05 プレースホルダーにできない動的値は正しくエスケープする
テーブル名・カラム名などを `${}` で展開する場合は無エスケープのため正しくエスケープする。

### R-08-06 楽観ロックはバージョン番号カラムで制御する
排他制御（楽観ロック）は「更新日時」ではなく「バージョン番号」カラムで制御する。

### R-08-07 #{} のプレースホルダ名は引数クラスの属性名と一致させる（複数引数は @Param）
`#{名}` は引数クラスの属性名に一致させる（ネストは `#{itemCategory.id}`）。メソッド引数が複数なら各引数に `@Param("名")` を付け `#{名.属性}` で識別する。

### R-08-08 resultType 利用時は camelCase 自動変換設定を行う
`resultType` を使う場合 `mybatis.configuration.map-underscore-to-camel-case: true` を設定する（スネークケース列→Java setter マッピング）。

### R-08-09 更新系 SQL の実行結果件数を必ず確認する
更新系 SQL の結果件数を `int` で受け取り、想定外（楽観ロックで 1 件でない等）の場合は `ConflictException` をスローする。SELECT は 1 件取得で `null`、N 件取得で `List` 要素数 0 を「0 件」と判定する。

### R-08-10 排他制御は DB のロック機能で行う
データ更新時は一貫性・整合性保証のため DB のロック機能による排他制御を行う。基本は楽観ロック。

### R-08-11 ユニーク番号の採番には悲観ロックを使用する
ユニークな番号（予約 ID・在庫 ID 等）の採番は悲観ロックを使用する（楽観ロックでは採番競合を防げない）。

### R-08-12 データアクセス例外は Repository で業務例外へ変換する
Mapper から `DBDuplicateKeyException` を catch した場合は `ConflictException` に、更新件数が想定外の場合も `ConflictException` にリスローする（メッセージコードを設定）。詳細は [springer-exception.md](springer-exception.md)。

## ✨ 推奨 (PREFER)

（なし。基本方針＝楽観ロックは R-08-10 に統合）

## 条件付き事項

### R-08-C1 悲観ロックの採用条件
同時更新の発生頻度が高い場合、または 1 トランザクションの処理が長い場合は悲観ロックを使用する。実装は `SELECT 〜 FOR UPDATE` で行ロックする。

### R-08-C2 SELECT のマッピングは resultType と resultMap のどちらか一方
`<select>` では `resultMap` と `resultType` を同時指定しない。単純マッピングは `resultType`、親子関係オブジェクト等は `resultMap`（`<id>`/`<result>`/`<association>`）。

### R-08-C3 動的 SQL は専用要素で組み立てる
`<if>`/`<where>`（先頭 AND/OR 自動除去）/`<set>`（末尾カンマ除去）/`<choose>`/`<foreach>`（主に `in`）を使用する。

### R-08-C4 大量結果は Cursor + try-with-resources で扱う
検索結果が大量になる場合は戻り値を `Cursor<T>` にし、Repository で `try-with-resources` で受け取り 1 レコードずつ処理する（使用後 `close` が必要）。

### R-08-C5 楽観ロック失敗・キー重複の例外変換
楽観ロックの UPDATE は WHERE に `version = #{version}`、SET に `version = #{version} + 1` を含め、更新件数が 1 以外なら `ConflictException`。INSERT 時のキー重複（`DBDuplicateKeyException`）も `ConflictException` に変換する。排他エラーは Controller で 409 Conflict を返す（[10](springer-exception.md)）。

## 参考・推奨実装パターン

- BS Repository は `@Repository` を付与しインターフェイスを実装、Mapper をコンストラクターインジェクション。責務は「Mapper 呼出」「永続化アクセス」「結果に応じた例外スロー」。
