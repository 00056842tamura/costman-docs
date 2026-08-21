---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 14 Java コーディングスタイル

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: 命名規約は [springer-package-class-naming.md](springer-package-class-naming.md)。
> 記法: ルールが多いため Style B（観点別に `**R-14-xx**` で列挙）。各 ID で個別参照可。

## 🚫 禁止 (NEVER)

- **R-14-01 インポート**: `java.lang` の import・重複 import を書かない。`static import` はテストクラスを除き使用しない。使用しないクラスの import を書かない。
- **R-14-02 空文・空ブロック**: 空文（`;` だけの文）・空ブロック・不要なネストブロックを書かない（意図的に処理しない場合はコメント＋必要に応じログ）。
- **R-14-03 メソッド長**: 300 行超のメソッドを書かない（150 行以内推奨）。
- **R-14-04 条件式**: 等値演算子（`==`/`!=`）による真偽値判定をしない（`if(flag==true)` 不可）。式の内部で変数へ代入しない。
- **R-14-05 メンバ変数**: メンバ変数を `null` で初期化しない。`boolean`/`int` 型メンバをデフォルト値で初期化しない。メンバ変数のスコープを `public`/`protected`/package private にしない（**`protected` は全面禁止**。継承先専用フィールドであっても `protected` は許容しない）。
- **R-14-06 文字・数値**: 文字列リテラルをインスタンス化しない。Unicode エスケープを使わない。C 言語スタイルの配列宣言（`int i[]`）をしない。ラッパークラスをコンストラクターでインスタンス化しない（`valueOf()` を使う）。`byte`/`short` 型を使わない。
- **R-14-07 Object メソッド**: `finalize()` をオーバーライドしない。
- **R-14-08 コメント**: ソースコードのヘッダーにコメントを記載しない。自明な／単なるコード翻訳のコメントを書かない。仮コードの TODO はリリース時に原則残さない。

## ⚠ 非推奨 (AVOID)

- **R-14-09**: インポートでワイルドカード `*` を使わない（代替＝個別 import。ただしテストクラスでは許容）。
- **R-14-10**: `@SuppressWarnings` は `value` に `unchecked` を指定するケース以外で使用しない。
- **R-14-11**: `String` の `toString()` 利用は避ける。

## ✅ 必須 (ALWAYS)

- **R-14-12 インポート順序**: `java.` → `javax.` → `org.` → `com.` → その他（各グループ内アルファベット順）。
- **R-14-13 クラス定義**: `private` コンストラクターしか持たないクラスは `final` 宣言。`static` メソッドのみのクラスは明示的に private コンストラクター宣言（`protected` は使わない＝R-14-05）。デフォルトコンストラクターは処理がなければ書かない。インナークラスは `private` スコープ以外作らない。
- **R-14-14 定義順序**: static 変数→インスタンス変数→コンストラクター→メソッド（各アクセス修飾子は public→protected→なし→private 順）。修飾子順序は Annotation→public/protected/private→abstract→final→synchronized→native→strictfp。
- **R-14-15 メソッド**: 非チェック例外はメソッド宣言の `throws` 節に書かない（Javadoc `@throws` には記述）。ローカルメソッド呼出時は `this` を付与。変数宣言は 1 宣言 1 行、処理は 1 行 1 文。オーバーロードは連続して並べる。
- **R-14-16 条件分岐・ループ**: 条件分岐による値セットは 1 箇所にまとめる。不要なら early return でネストを浅くする。`enum` 比較は `==`。`if`/`else if`/`else`/`switch` は常に中括弧 `{}` を明記し `switch` には `default` を書く（fall through 時はコメント）。Collection/配列ループは基本的に拡張 `for`。
- **R-14-17 メンバ変数**: 使用時は `this` を付与。ローカル変数は利用直前で定義しフィールドと重複しない名称にする。
- **R-14-18 文字・数値**: `String` 比較は `equals()`（リテラル/定数を左辺）。プリミティブ→文字列は `String.valueOf()`。null/空文字チェックは Spring `ObjectUtils.isEmpty()`。`Long` リテラルは大文字 `L`。桁あふれ想定・小数演算は `BigDecimal`。
- **R-14-19 Object メソッド・リソース**: `equals()` オーバーライド時は `hashCode()` も。`clone()` は `super.clone()` を呼ぶ。`AutoCloseable` な Stream リソースは `try-with-resources`。
- **R-14-20 フォーマット**: インデントは半角 4 スペース（タブ不可）。1 行最大 160 文字。開始 `{` は文末、終了 `}` はブロック開始文と同じインデント。空白規約（予約語と `(` の間に空白、メソッド呼出 `(` 前は空白なし、二項/三項演算子の両側に空白、Generics `<>` 前後空白なし 等）に従う。
- **R-14-21 Javadoc**: 要約から記載し「ですます」調（ソースコメントは体言止め）。`@param`/`@return`/`@throws` を過不足なく記載し `@throws` にはエラー発生条件を書く。
- **R-14-22 Checkstyle 適用**: 定義ファイル `springer_checks_v1_13.xml`（Checkstyle v8.42〜）＋除外 `checkstyle-suppressions.xml` を適用する（v1.13 で `IllegalImport`＝Lombok 禁止を追加）。
- **R-14-23 SonarQube 追加ルール**: `java:S1166`（例外の握り潰し禁止＝log or rethrow）、`java:S2221`（`Exception` の広域 catch 禁止）、`java:S3749`（`@Controller` 等でのインスタンス変数禁止）を有効化する。

## ✨ 推奨 (PREFER)

- **R-14-24**: 各パッケージごとに `package-info.java` を作成し、パッケージ概要・詳細を記述する。

## 条件付き事項

### R-14-C1 SonarQube 除外ルールは状況に応じて対応する
`java:S107`（引数数）・`java:S1192`（文字列リテラル定数化）・`java:S6353`（正規表現）等は除外（一律適用しない）。ただし状況に応じ対応する（例: エラーコード/メッセージコードや意味不明なマジックナンバーは定数化、意味明確なクエリパラメーター名/View 名は定数化しない）。「指摘をなくすためだけの対応はしない」。

## 参考・推奨実装パターン

- 代表サンプル（`ServiceImpl`＋`AbstractService`〔`protected Logger`〕＋Interface〔検査例外の throws 記述〕＋Utility〔`final`・private コンストラクター・static〕）は `01.Javaコーディング規約.md#サンプルコード` を参照。
- Checkstyle 実行: `mvn -Dcheckstyle.config.location=./etc/springer_checks_v1_13.xml -Dcheckstyle.properties.location=./etc/checkstyle.properties checkstyle:check`（定義ファイル内の `${config_loc}` 解決のため `checkstyle.properties`〔`config_loc=./etc`〕の配置・指定が必要）。SonarQube は SonarLint（Eclipse prefs / VS Code `sonarlint.rules`）。
