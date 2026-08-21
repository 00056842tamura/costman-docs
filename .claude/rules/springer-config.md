---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 19 設定

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-19-01 Cookie の SameSite=None × Secure=false を使わない
`SameSite=None` かつ `Secure=false` の組み合わせはブラウザが Cookie を受け入れないため使用しない。アプリの HTTPS 使用状況に合わせて設定する。

## ⚠ 非推奨 (AVOID)

## ✅ 必須 (ALWAYS)

### R-19-02 拡張部品(Springer)を所定の方法で有効化する
Maven は `<parent>` に `springer-parent`（要 version）＋`<dependency>` に `springer-core`、Gradle は `mavenBom`＋`springer-core` で有効化する。

### R-19-03 アプリ名を個別に設定する
`spring.application.name`（アプリ名）は各アプリごとに必ず設定する。

### R-19-04 タイムアウトは必ず設定する
通信瞬断時の処理滞留・リソース占有防止のためタイムアウトを必ず設定する（デフォルト値が設定済みの項目はデフォルト使用でも可）。タイムアウト到達時の整合性確認の仕組みを用意する。

### R-19-05 タイムアウトは上位のタイムアウトを超えない値にする
「上位タイムアウト値 ≧ 設定値 ≧（呼び出し先処理の想定時間＋ラグ）」を満たす値にする。アプリ処理時間も考慮し、上位タイムアウト到達時にアプリ処理が継続しないようにする。

### R-19-06 JDBC socketTimeout を設定する
DB から一定時間応答がない場合にタイムアウトさせるため、JDBC ドライバの `socketTimeout` は原則必ず設定する（特に SQL Database で必須）。

### R-19-07 KeepAlive を設定し OUT < 呼び出し先 IN にする
自身 (例：USアプリ) の「呼び出し先 (例：BSアプリ) に対する KeepAlive タイムアウト設定 (OUT)」は、呼び出し先側 (例：BSアプリ) の「呼び出し元 (例：USアプリ) に対する KeepAlive タイムアウト設定 (IN)」より短くする。（逆だと Connection Reset By Peer が発生）。複数呼び出し先がある場合は最短の値で算出する。TCP アイドルタイムアウトも含めて確認する。

### R-19-08 Java オプションを環境変数で設定する
`deployment.yaml` の `env` に `JAVA_OPTS` を定義する（`MALLOC_ARENA_MAX=2` も設定）。

## ✨ 推奨 (PREFER)

### R-19-11 application.yml は原則空にし、プロファイル固有設定は application-＜プロファイル＞.yml に書く
`application.yml` は原則空（共通設定の記載は可）とし、固有設定は `application-＜プロファイル＞.yml`（優先される）に書く。設定はブランクプロジェクトの `application-default.yml` をひな形として利用する。

### R-19-12 Bean 定義は原則 AutoConfigure 任せにする
認証・認可設定（Security Config）以外は原則アプリで Bean 定義せず AutoConfigure に任せ、プロパティ/環境変数で設定できない Bean 定義のみ Java Config で行う。

### R-19-13 メモリ管理方式は G1GC を採用する
基本的に G1GC（`-XX:+UseG1GC`）を選択する。`-Xms` は `-Xmx` と同値、`-XX:MetaspaceSize` は `-XX:MaxMetaspaceSize` と同値を推奨。G1GC を阻害する `-XX:MaxNewSize`/`NewSize`/`NewRatio` は設定しない。

### R-19-14 タイムアウト値は可能な限り小さくする
算出範囲内で、呼び出し先処理時間が想定内なら最小値を設定する（想定より延びうる場合はリソース無駄占有防止のため最大値）。

## 条件付き事項

### R-19-C1 プロファイルの指定方法
ローカル開発環境以外は環境変数 `SPRING_PROFILES_ACTIVE` で指定する。ローカルは環境変数を指定せず `application-default.yml`（未指定時 `default`）で設定する。

### R-19-C2 ログの JSON 化はバージョンで方式が異なる
Springer v2.4 以上は `logging.structured.format.console: logstash`＋`logging.structured.json.add.source: application`。v2.3 以下は `logstash-logback-encoder` 依存＋`custom-logging-config.logstash-encoder: true`（DB 接続時は `mybatis-parameter-filter: true` 併設）。

### R-19-C3 Tomcat アクセスログ・環境別ログ設定
Tomcat アクセスログはコンテナ環境用 yml に `server.tomcat.accesslog`（`enabled`/`directory`/JSON `pattern`/`request-attributes-enabled` 等）を設定する（ローカルは不要）。JSON 化する場合は `directory` の `<gcode>` を業務コードに置き換える。ログ出力設定は環境変数または `application-＜プロファイル＞.yml` で環境ごとに設定する。

### R-19-C4 クエリタイムアウト・HikariCP の制約
クエリタイムアウトは JDBC `queryTimeout`／MyBatis（`default-statement-timeout` < Mapper XML `timeout`）／`@Transactional(timeout)` で構成され、`@Transactional` がある場合は MyBatis 値と残り時間の小さい方が優先。HikariCP の `validation-timeout` は `connection-timeout` より小さく（最小 250ms）。

### R-19-C5 REST/SOAP のタイムアウト・KeepAlive 既定
Springer 2.0 以降、`springer.rest.*`/`springer.wstemplate.*` の各タイムアウトは未設定時 3 分、`0` で無制限（単位接尾辞 `m`/`s`/`ms` 指定可）。個別 Bean 設定（`springer.rest.clients.*`）は対象 Bean 名指定が必須。

### R-19-C6 Redis 接続情報・SSL
`spring.data.redis.host`/`password` は環境変数（K8s ConfigMap/Secret）で設定する。Azure マネージド Redis は `spring.data.redis.ssl.enabled: true`（SSL 必須）。

## 参考・推奨実装パターン

- 設定ファイルは Spring 設定／拡張部品設定（`application.yml`・`application-＜プロファイル＞.yml`）／環境変数／Java Config の 4 種別。
- DB タイムアウト実装例: JDBC 接続文字列に `socketTimeout`/`queryTimeout`（例: `jdbc:postgresql://{host}:{port}/{db}?socketTimeout=15&options=-c%20statement_timeout=15000`）、`application-＜プロファイル＞.yml` に HikariCP（`connection-timeout: 30000`・`idle-timeout: 600000`・`validation-timeout: 5000` 等、単位ミリ秒）と MyBatis（`mybatis.configuration.default-statement-timeout`、単位秒）、Mapper XML の `<select timeout="15">` 等、Service クラスの `@Transactional(timeout = 15)` を設定する（優先順位は R-19-C4 を参照）。
- G1GC チューニングオプション例: `-XX:MaxGCPauseMillis`（目標停止時間、既定 200ms）、`-XX:ParallelGCThreads`（GC スレッド数、既定は論理 CPU 数〔最大 8〕）、`-XX:ConcGCThreads`（マーキングスレッド数、既定は `(ParallelGCThreads + 2) / 4` を切り捨て・最低 1・`ParallelGCThreads` を超える値は不可）。
- フィルター/インターセプターの順序: 拡張部品（Springer）が Servlet フィルター・ControllerAdvice・各種インターセプター（Controller/Service/Repository/MyBatis/Rest 等）を既定の順序で自動登録する。独自に Servlet フィルターを追加する場合は、I/F ログ用フィルター（`servletLogFilter`、順序 `HIGHEST_PRECEDENCE + 70`）より後、リクエスト/レスポンスをラップする場合は `requestContextFilter`（順序 `-105`）より前に順序を設定する。
- REST/SOAP の KeepAlive 設定例: `application-＜プロファイル＞.yml` に `springer.rest.keep-alive-duration`（RestClient）／`springer.wstemplate.keep-alive-duration`（WebServiceTemplate）を設定する（本設定値と通信先からの Keep-Alive レスポンス値のうち小さい方が適用され、省略時は通信先の値または既定 3 分。R-19-C5 と整合）。あわせて `springer.rest.evict-idle-connections`／`springer.wstemplate.evict-idle-connections`（コネクションプールの監視間隔・有効期限切れ判定時間、既定 5 秒）を設定する。
- アクセスログパターンは `%I`（スレッド名）でアプリログと紐付ける推奨形。
