---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 11 ロギング・マスキング

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-11-N1 センシティブデータを平文でログ出力しない
氏名・クレジットカード番号などセンシティブデータはログに出力しない。出力する場合はマスキングする。出力有無は Logback のログレベルで制御する。通信 I/F ログの URL クエリ文字列はマスクできないため、機密情報をクエリ文字列に含めない。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-11-01 I/F ログのログレベルは運用時 OFF または INFO 以上にする
I/F ログ（Servlet・Controller・通信関連）のリクエスト/レスポンス（ヘッダー・ボディ・パラメーター）のログレベルを運用時 `OFF` または `INFO` 以上にする。
- 理由 / 背景: req/res のヘッダー・パラメーター・ボディは **フィールド値をマスクできない**ため機密情報が平文出力されうる。
- 対象: `SERVLET_IF`/`CONTROLLER_IF`/`REST_IF`/`WEBSERVICE_IF` の req/res 項目。Service/Repository/JDBC/MyBatis の I/F ログは `toString()` でマスキング可のため本ルール対象外。

### R-11-N2 ロガーは LoggerFactory.APP から取得する
ログは `jp.co.nekonet.springer.logging.LoggerFactory` の enum から取得した Logger で出力する（`private static final Logger LOGGER = LoggerFactory.APP.getLogger(Xxx.class);`）。ログ出力ライブラリは Logback + SLF4J を直接呼び出す。

### R-11-N3 引数生成にコストがかかる場合は事前にログレベルをチェックする
ログメッセージ引数の生成にコストがかかる場合、`if (LOGGER.isDebugEnabled()) { ... }` 等でレベルを確認してから生成する。

### R-11-N4 I/F ログは種別ごとの既定ログレベルに従う
I/F ログは種別ごとの既定ログレベルに従う。req/res 系（Servlet/Controller/REST/WebService のヘッダー・パラメーター・ボディ）は運用時 `OFF` または `INFO` 以上（R-11-01）、Controller/Service/Repository のメソッド引数・戻り値は `INFO`、JDBC/MyBatis は `DEBUG` が既定。認証イベントは成功＝INFO・失敗＝WARN・エラー系＝ERROR（[springer-security-auth.md](springer-security-auth.md) R-13-09 と整合）。種別別の一覧は「参考」を参照。

## ✨ 推奨 (PREFER)

（なし）

## 条件付き事項

### R-11-C1 機密情報を含むモデルフィールドは @Sensitive でマスキングする
モデルのフィールドに機密情報（パスワード・個人情報など）が含まれる場合、`@Sensitive`（`jp.co.nekonet.springer.masking.Sensitive`）を付与し、`toString()` 内で `MaskingToStringBuilder.toString(this)` を呼ぶ。
- 条件: ログ出力されうるモデルの機密フィールド（Service/Repository/DB の I/F ログは引数・戻り値の `toString()` を出力するため）。

### R-11-C2 ログに出さないフィールドは @ToStringExclude を付与する
I/F ログ出力量削減やログに含めたくない項目には `@ToStringExclude` を付与し、`toString()`（MaskingToStringBuilder）の出力対象から除外する。

### R-11-C3 マスキング種別は要件に応じて選ぶ
全桁マスキング `@Sensitive`、奇数桁マスキング `@Sensitive(OddMasker.class)`。独自マスキングが必要な場合は `Masker` インターフェイスを実装した `@Component` を作り `@Sensitive(MyMasker.class)` で指定する。

### R-11-C4 マスキングの有効・無効は Spring 設定で制御する
`springer.masking.enabled`（全体）、`springer.masking.category.{パッケージ}.{クラス}.{フィールド}`（個別、深い階層が優先）、`ROOT`（既定）で制御する。全体無効時は個別設定によらず強制無効。

### R-11-C5 大量ログ抑止は Logback の Filter / TurboFilter で行う
条件（レベル・ロガー名・メッセージ・桁数等）でログを抑止する場合、`Filter`（Appender 単位）または `TurboFilter`（全 Appender）を継承した部品を作り、`GenericApplicationListener`＋`ApplicationStartedEvent` で登録する（`LoggingApplicationListener` より後に実行されるよう `Ordered` 実装）。

### R-11-C6 MyBatis のプレースホルダー値はログ出力抑止を有効化する
MyBatis/JdbcTemplate の PreparedStatement プレースホルダー値はマスキング不可のため出力されない。抑止は `springer.database.mybatis.my-batis-parameter-filter.enabled = true`。

## 参考・推奨実装パターン

### I/F ログ種別と規定ログレベル

| ログ種別（代表カテゴリ） | 既定レベル | 運用時の推奨 |
|---|:--:|---|
| Servlet I/F の req/res〔ヘッダー/パラメーター/ボディ〕（`SERVLET_IF.*`） | DEBUG | **OFF または INFO 以上** |
| Controller I/F の req/res〔ヘッダー/パラメーター〕（`CONTROLLER_IF.before/after.*`） | DEBUG | **OFF または INFO 以上** |
| Controller / Service / Repository のメソッド引数・戻り値・処理時間（`CONTROLLER_IF`/`SERVICE_IF`/`REPOSITORY_IF`） | **INFO** | — |
| JDBC / MyBatis〔引数・SQL・処理件数〕（`JDBC_IF`/`MYBATIS_IF` 等） | DEBUG | — |
| REST / WebService の req/res〔ヘッダー/ボディ〕（`REST_IF.*`/`WEBSERVICE_IF.*`） | DEBUG | **OFF または INFO 以上** |
| 認証イベント（`AuthenticationEventLogListeners.*`） | 成功/switchUser=INFO・失敗系=WARN・エラー系=ERROR・他=DEBUG | — |

> req/res 系・JDBC/MyBatis のプレースホルダー値はフィールド値をマスクできないため、運用時に出力する場合は機密が平文化される点に注意（R-11-01）。`SERVLET_IF`/`CONTROLLER_IF` カテゴリを OFF/INFO 以上にすると配下の `before.*`/`after.*` も無効化される。

- I/F ログ種別（Servlet/レイヤー間 Service・Repository/DB 関連 JDBC・MyBatis/通信関連 REST・WebService）とカテゴリ、運用時推奨レベルは `106.00`・`202.01〜05` の各表を参照。
- 認証イベントログ（認証成功 INFO / 失敗系 WARN / その他 ERROR）は `202.05`。詳細は [springer-security-auth.md](springer-security-auth.md)。
- セッション ID・パスワード・CSRF トークン等は拡張部品が自動マスクする。
