---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 03 パッケージ構成・クラス定義・命名

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-03-05 Controller クラスで継承・インターフェイス実装をしない
Controller クラスでの継承、およびインターフェイスの実装は禁止。共通機能は別クラスへ責務委譲する。
- 理由 / 背景: 共通機能をスーパークラスに実装して継承する設計を避けるため。

### R-03-06 Service・Repository クラスで継承をしない
Service・Repository クラスでの継承は禁止（共通基底クラス／抽象クラスの継承も含む）。
- 補足: `ServiceImpl extends AbstractService` のような継承サンプルを見かけることがあるが、本規約では継承は不可とする（共通機能は委譲で実現）。

### R-03-07 インターフェイスのメソッドに throws 節を書かない
Service・Repository インターフェイスのメソッドシグネチャに、throws 節を記述しない（非チェック例外を throw する場合は、Javadoc の @throws タグに記載する）。
- 補足: サンプルコードにのみ検査例外の `throws` 記述が見られる場合があるが、本ルールは非チェック例外の `throws` 禁止を対象とする。

### R-03-N1 パッケージ名に接続記号を使わない
パッケージ名はすべて小文字とし、連続する単語をそのまま繋げる。アンダースコア等の接続記号を用いた命名はしない。

### R-03-N2 接尾語以外にパッケージ名と重複する単語を使わない
クラス名の接尾語以外に、パッケージ名と重複した単語を使用しない（NG: `UserCommonInfo` / OK: `UserInfoCommon`）。

### R-03-N3 固有名称以外で数字を使わない
固有名称として数字が使われている場合を除き、命名に数字を使用しない。略語は使用せず英単語はフルスペルを基本とする。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-03-01 規定のパッケージ構成に従う
パッケージは `＜ドメイン名の逆順＞.＜システム名＞.＜サブシステム名＞` 配下に、規定の構成（下記「参考」のツリー）で配置する。

### R-03-02 クラス名・インターフェイス名の命名に従う
Controller のクラス名、Service・Repository のインターフェイス名は `XxxController` / `XxxService` / `XxxRepository` とする。クラス名はパスカル形式（先頭大文字）でファイル名と一致させる。

### R-03-03 実装クラス名とアノテーション付与
Service・Repository インターフェイスの実装クラス名は `XxxServiceImpl` / `XxxRepositoryImpl`（接尾語 `Impl`）とし、それぞれ `@Service` / `@Repository` を付与する。

### R-03-04 Service・Repository は「インターフェイス＋実装クラス」構成とする
Service・Repository は「インターフェイス + 実装クラス」の構成とする。インターフェイスには識別用の接頭語・接尾語を付けない。

### R-03-N4 命名規約（種別別フォーマット）に従う
- 抽象クラス: 接頭語 `Abstract`（`Base` は使用しない）。
- 例外クラス: 接尾語 `Exception`。エラークラス（非チェック例外）: 接尾語 `Error`。
- ユーティリティ: 処理内容を表すクラス名を付け、付けられない場合に限り `***Util`（`***Utils` 不可）。
- メソッド名: キャメル形式・先頭小文字。変換`to***`／判定`is***`/`can***`/`has***`／取得`get***`／設定`set***`／永続化取得（単一`get***ByYYY`・複数`find***ByYYY`・全件`findAll***`）／生成`create***`／API 呼出`call***`。
- 変数名: キャメル形式。配列/Collection は `***s`/`***List`/`***Set` 等。boolean は状態が分かる名（`flag` 単独不可、`xxxFlag` 可）。
- 定数名: 全大文字、複数単語は `_` 結合、種別→値の順（例 `DISPLAY_COUNT_MAX`）。
- プロパティファイル名: 全小文字、単語を `_` 連結。

### R-03-N5 テストクラス・テストメソッドの命名・配置に従う
テストクラスは対象クラスと同一プロジェクトの `src/test/java` 内の同一パッケージに配置し、クラス名は `【対象クラス名】Test`、テストメソッド名は `test【対象メソッド名(先頭大文字)】【接尾語(PJ 単位)】` とする。
- 補足: 命名規約文書では `Tests`（複数）表記の例もあるが、ユニットテスト実装ガイド・実装サンプル（全件 `XxxTest`）は `Test`（単数）。本ルール集は **`Test`（単数）を正**とする（[springer-testing.md](springer-testing.md) R-20-04 と統一）。

## ✨ 推奨 (PREFER)

## 条件付き事項

（なし）

## 参考・推奨実装パターン

### パッケージ構成

```
<ドメイン逆順>.<システム名>.<サブシステム名>
  controller/   … Controller クラス
    form/       … Form クラス (MPA系 US アプリ)
    request/    … Request クラス (SPA/API系 US・BS アプリ)
    validator/  … Spring Validator クラス
    view/       … View クラス
  model/        … モデルクラス
  repository/   … Repository インターフェイス
    impl/       … Repository 実装クラス
    mapper/     … MyBatis Mapper インターフェイス
  service/      … Service インターフェイス
    impl/       … Service 実装クラス
```

- import 順序: `java.` → `javax.` → `org.` → `com.` → その他（各グループ内アルファベット順）。詳細スタイルは [springer-coding-style.md](springer-coding-style.md)。
