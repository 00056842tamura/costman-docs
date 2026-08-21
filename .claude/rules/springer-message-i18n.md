---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 12 メッセージ ID・メッセージ管理・国際化

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

（なし）

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-12-01 メッセージ ID は規定の命名規約に従う
メッセージ ID（`messages.properties` のキー）は種別ごとに規定の命名（下記「参考」）に従う。接頭辞はアプリ名。

### R-12-02 メッセージは messages.properties で管理する
管理対象メッセージ（例外／画面表示・API／バリデーション／ログ出力）は `messages.properties` で管理する。

### R-12-03 メッセージ取得は拡張部品「メッセージ管理」(MessageManager) を使う
メッセージ取得は `jp.co.nekonet.springer.message.MessageManager`（`MessageManager.getMessage("メッセージID", "param1", ...)`）を使う。
- 理由 / 背景: Spring 標準 `MessageSource` はロケール引数が必要だが、MessageManager は内部でロケールを補完するため明示不要。

### R-12-04 エラーコードは例外メッセージのメッセージ ID と同じにする（原則）
拡張部品が規定する例外のエラーコードは、原則として「例外メッセージのメッセージ ID」と同じものとする（グローバル例外ハンドラーが該当メッセージをログ/API レスポンスに利用できる）。

### R-12-05 国際化の言語指定は HTTP ヘッダー Accept-Language で行う
国際化対応の言語指定は、基本方針として `Accept-Language` で行う（Spring MVC・MessageSource とも既定で対応）。US 層・BS 層が対象。

### R-12-06 Rest クライアント呼び出し時はロケールを伝搬させる
アプリ間通信時、拡張部品が RestTemplate/RestClient の `Accept-Language` にロケールを自動付与する（呼び出し先で同一ロケールのメッセージを取得）。

## ✨ 推奨 (PREFER)

### R-12-07 ユーザー操作による言語切替は Cookie 方式を推奨する
ユーザー操作で言語切替する場合は Cookie（`CookieLocaleResolver`＋`LocaleChangeInterceptor`）を推奨する（`Accept-Language` はブラウザ設定変更を要しユーザビリティが低い）。US 層が対象。

## 条件付き事項

### R-12-C1 DB 永続化データ・DEBUG ログはメッセージ管理対象外
DB に永続化するデータ（API レスポンスの業務データ等）はメッセージ管理対象外（多言語化は別カラム）。DEBUG ログはハードコーディングとし対象外。

### R-12-C2 バリデーションは Spring 標準命名で賄える場合は独自規約を使わない
バリデーションメッセージは Spring 標準命名（`アノテーション名.フォーム名.項目名`、`typeMismatch.フォーム名.項目名` 等）で賄える場合は独自規約を使わない。

### R-12-C3 クライアント（モダン JS）は別々にメッセージ定義を保持する
クライアントの国際化は、画面表示・API メッセージは国際化部品、バリデーションエラーはバリデーション部品で対応し、定義はクライアント／サーバーそれぞれで保持する。

## 参考・推奨実装パターン

### メッセージ ID 命名

| メッセージ ID | 用途 |
|---|---|
| `{アプリ名}.error.business.{名前}` | 業務例外 |
| `{アプリ名}.error.system.{名前}` | システム例外 |
| `{アプリ名}.message.{名前}` | 画面表示 / API |
| `{アプリ名}.validation.{名前}` | 画面表示 / API バリデーション |
| `{アプリ名}.log.{info\|warn\|error}.{名前}` | ログ出力 |

- 拡張部品クラス: メッセージ管理＝`MessageConfiguration`/`MessageManager`、ロケール伝搬＝`RestTemplateConfiguration`/`RestTemplateInterceptor`。
- 例外連携例: `throw new ConflictException("webapp.error.business.DuplicateUserName", user);`（エラーコード＝メッセージ ID）。
