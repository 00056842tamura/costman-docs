---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 06 入力チェック・バリデーション

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

（なし。入力チェックロジックを Controller に書かないことは [05](springer-controller.md)/[01](springer-architecture-layer.md) を参照）

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-06-01 Form クラスでプリミティブ型のフィールドを使用しない
Form のプロパティには `Integer`/`Long`/`BigDecimal`/`Boolean` などの参照型を使用する。
- 理由 / 背景: `int`/`long`/`boolean` 等は `null`（未入力）を表現できないため。

### R-06-02 JSON リクエストの未入力値は「項目を送らない」か null とする
JSON リクエスト（`@RestController` / `@RequestBody`）で値がない場合は「空文字」ではなく「項目を送らない」か `null` とする。
- 理由 / 背景: JSON では空文字→null 変換（`StringTrimmerEditorControllerAdvice`）が効かず、「入力された場合のみチェック」が成立しないため。

### R-06-03 入力チェックは標準＋拡張部品のバリデータで実施する
標準（Jakarta Bean Validation / Hibernate Validator：必須・レングス・最小/最大値・正規表現）と拡張部品（文字種・バイト長・文字エンコーディング・アップロードファイル）のバリデータを使用する。

### R-06-04 項目関連チェックは Spring Validator クラスに実装する
単項目チェックは Form/Request にアノテーションで定義し、項目関連（相関）チェックは `org.springframework.validation.Validator` を実装した `@Component`（Validator クラス）に記述する。Controller では `@InitBinder` で `WebDataBinder.addValidators()` 登録し、`@Validated` を付けて実行する。

## ✨ 推奨 (PREFER)

### R-06-05 入力文字のトリムは既定（有効）に従う
`StringTrimmerEditorControllerAdvice` により入力文字は既定で `String.trim()` される。トリムしない場合のみ `springer.validator.string-trimmer-editor-controller-advice.trim: false` を設定する。

## 条件付き事項

### R-06-C1 サロゲートペア対応が必要な場合は @CodePointLength を使う
文字列長チェックは通常 `@Size`、サロゲートペア対応が必要な場合は `@CodePointLength` を使用する。

### R-06-C2 型変換フォーマット指定が必要な場合は @NumberFormat / @DateTimeFormat を使う
数値は `@NumberFormat(pattern=...)`、日時は `@DateTimeFormat(pattern=...)`。型ミスマッチ時のメッセージキーは `typeMismatch` 系。

### R-06-C3 文字種・バイト長・機種依存文字チェックは拡張部品バリデータを使う
- 文字種: `@Half`/`@HalfAlphaNumeric`/`@HalfKatakana`/`@Full`/`@Hiragana` 等（`acceptable` 属性で容認文字指定、null/空文字はチェックしない）。
- バイト長: `@MinByteLength`/`@MaxByteLength`（`encoding` 既定 `Windows-31J`、空文字もチェック）。
- 機種依存文字: `@SafeEncoding`（既定 `Shift_JIS`）。
- アップロードファイル: `@UploadFileRequired`/`@UploadFileNotEmpty`/`@UploadFileMaxSize`（既定 1048576 byte）。

### R-06-C4 @Validated でグループ指定する場合は Default.class を併記する
`@Validated` にグループを指定する場合、`Default.class` を併記しないとデフォルトグループのチェックが実行されない。

### R-06-C5 バリデータのデフォルト正規表現／メッセージを変更する場合
- 正規表現変更: `springer.validator.constraints.＜アノテーション名＞.regexp` を application yml に定義。
- メッセージ変更: `messages.properties` に定義（標準=`jakarta.validation.constraints.＜名＞.message`、拡張部品=`springer.validator.constraints.＜名＞.message`）。`@Pattern` のデフォルトメッセージは未組込のため定義が必要。

## 参考・推奨実装パターン

- BindingResult を付けると入力エラー有無に関わらずハンドラーが呼ばれ（画面系で `hasErrors()` 判定）、付けないとエラー時は呼ばれずエラーレスポンスが返る（API 向け）。詳細は [05](springer-controller.md)。
- JSON 全項目の空文字→null 変換: `springer.jackson.object-mapper.empty-to-null-string: true`、個別変換: setter に `@JsonDeserialize(using=EmptyToNullStringDeserializer.class)`。