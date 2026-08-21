---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 17 レジリエンス・横断的処理方式

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### リトライ時のキャッシュ利用
- **R-17-23** ロック付き検索処理（`for update` 等）をリトライ用キャッシュ（`@Cacheable`）の対象にしない（ロックが行われない可能性があるため）。
- **R-17-24** 検索結果をもとに更新するトランザクションでリトライ用キャッシュを使用しない（キャッシュ時点の値を参照し、別トランザクションで更新された値でない可能性があり、一貫性を損なうため）。

## ⚠ 非推奨 (AVOID)

### R-17-A1 MDC キー設定で子のない親項目だけを残さない
分散トレーシングの MDC キー設定をコメントアウトする際、子が存在しない親のみの項目を残さない（起動時エラーになる）。

## ✅ 必須 (ALWAYS)

### リトライ制御
- **R-17-01** API 呼び出しのリトライは「べき等な処理の API」に限定する（べき等でないと二重送信で不整合になる）。
- **R-17-02** DB アクセスのリトライは「トランザクション処理単位」で行う（トランザクション内のリトライでは復旧できない）。
- **R-17-03** リトライ機能は `@Transactional` より実行順序を前にする（Spring Retry 2.0.1 以降は既定で前置）。
- **R-17-04** リトライ実装時は「最大試行回数」と「バックオフ」を定義する（`@Retryable` の `maxAttempts`〔既定 3、初回含む〕＋`@Backoff`）。
- **R-17-05** API 呼び出しのリトライ対象エラーは HTTP 502/504 と I/O エラーに限定する（`exceptionExpression` で 502/504 のみ判定）。

### 分散トレーシング
- **R-17-06** トレース情報（traceId/spanId）をログの相関項目として出力する（`logging.pattern.correlation`〔Springer 2 系〕等）。

### Spring Boot Actuator
- **R-17-07** Actuator の公開エンドポイントは `*` とせず必要なものだけを列挙する（`management.endpoints.web.exposure.include`）。Actuator は本体と別ポート（`management.server.port`）にする。
- **R-17-08** Actuator エンドポイントは拡張部品「Actuator セキュリティ設定」で BASIC 認証によりアクセス制限する。

### ヘルスチェック
- **R-17-09** Pod 正常性監視は Readiness Probe と Liveness Probe で行う（Startup Probe は Liveness で代替できるため使わない）。標準エンドポイント `/actuator/health/{readiness|liveness}` を使う（標準 health endpoint の利用には Spring Boot 2.3 以降が必要。Kubernetes 環境では自動構成される）。
- **R-17-10** DB を使用する Pod の Readiness Probe はカスタムヘルスグループで DB 接続監視を行う（`management.endpoint.health.group`）。

### 複数リソース間データ整合性
- **R-17-11** 一連の処理では原則として複数リソースの更新を行わず、単一リソース更新のみとなるよう設計する。
- **R-17-12** 結果整合性手法を適用できないリソースがある場合は「適用できる→適用できない」の順で更新する。
- **R-17-13** TCC パターンでは Try/Confirm での（業務）データ削除を論理削除とする（Cancel で取り消せるように）。**Cancel フェーズでは Try で「仮」登録したデータを物理削除して取り消す。** 状態（仮/確定）・TCC-ID・TCC-NO・削除フラグを各データに用意する。
- **R-17-14** TCC/Saga ともリトライで復旧できない場合に備え、データ整合性復旧の仕組み（障害管理情報＋バッチ監視復旧、またはログ監視＋手動復旧等）を検討する。
- **R-17-15** Saga パターンでは各処理を単一リソースのローカルトランザクションとし、失敗時は補償トランザクションを遡って実行する。

### 閉塞制御
- **R-17-16** 閉塞時はシステム/アプリを完全停止せず、フロント部分を閉塞して新規リクエスト受付を停止する（プログラム閉塞は US 層サーブレットフィルターで 503 を返す）。

### 流量制御
- **R-17-17** 流量制御は待機と遮断を組み合わせ、キャパシティ・プランニングは待機（キューイング）を基本とする。

### リトライ時のキャッシュ利用
- **R-17-25** リトライ用キャッシュの生存期間はリクエスト単位とし、`CacheManager` の Bean 登録に `@RequestScope` を付与する。`@Cacheable` 有効化のため Application クラスに `@EnableCaching` を付与する。`@Cacheable` には利用する `CacheManager` を `cacheManager` プロパティで明示し、`value`（キャッシュ名）はメソッドごとにアプリ内で一意にする。
- **R-17-26** `@Cacheable` メソッドの引数に Model クラスを指定する場合、その Model クラスに `hashCode()`／`equals()` を実装する（キャッシュキーが引数の hashCode から算出されるため）。原則 IDE の自動生成機能を用いる。

## ✨ 推奨 (PREFER)

- **R-17-18** リトライ実装は Spring Retry ライブラリを使用する（Spring Framework 7 の Core 統合は有効化・順序制御ができないため）。
- **R-17-19** 輻輳・過負荷の可能性がある場合はランダムまたは指数（ジッターあり）バックオフを使用する。
- **R-17-20** リトライ状況は `RetryListener` でログ出力する（ロガーは `LoggerFactory.APP`）。
- **R-17-21** 閉塞箇所は AGW → API Management → Ingress → API(プログラム) の順で検討・実装する。
- **R-17-22** 流量増加対応はスケールアップ・スケールアウトしやすい構成にする。

## 条件付き事項

### R-17-C1 DB アクセスのリトライ対象例外・DB 製品別の差異
トランザクション開始時は `CannotCreateTransactionException`、SQL 実行/コミット/ロールバック時は `TransactionSystemException` を対象とする。PostgreSQL（フレキシブル/富士通）はクエリタイムアウトで `DBDataAccessException`（SQLState 57014）を対象に追加。Azure SQL Database はクエリタイムアウトをリトライしない場合 HikariCP の `SQLExceptionOverride` をカスタマイズする。

### R-17-C2 分散トレーシングは Springer バージョンで方式が異なる
Springer 2.0 以降は Micrometer Tracing（伝搬既定 W3C）、1.7 以前は Spring Cloud Sleuth（B3）。Spring Boot 2.x（Springer 1.x）アプリと連携する場合は伝搬タイプに B3 を併用する。

### R-17-C3 更新＋更新を跨ぐ場合のみ TCC/Saga を適用する
「参照＋更新」は不整合が発生しないため適用不要。「更新＋更新」のみ結果整合性手法を適用する。

### R-17-C4 Saga 適用時は Isolation（データ分離）が担保されない前提で設計する
Saga は C/D は担保するが I は担保せず、ロストアップデート・ダーティリード等が発生しうる。外部提供サービスに補償処理が無い場合は元に戻せないため運用も含め考慮する。

### R-17-C5 流量制御は箇所ごとに方針が異なる
AGW=遮断（インスタンス数）、API Management=遮断（アクセス制限ポリシー、超過時 429/403）、Ingress=遮断（レート制限、超過時 503）、アプリケーション=待機/遮断（Tomcat スレッド/キュー・HttpClient 接続数・HikariCP プールサイズ）。複数 Pod の DB 接続数合算が DB 最大接続数を超えないようにする。

### R-17-C6 他キャッシュプロバイダ併存時の自動構成除外
依存関係に他のキャッシュプロバイダ（Redis・Caffeine 等）が存在する状態でリトライ用 `CacheManager` を `@RequestScope` で Bean 登録すると、Bean の Scope 差異でエラーになる。回避するには `application.yml` の `spring.autoconfigure.exclude` で `org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration` を除外する（ただし Spring Boot Actuator のキャッシュ情報は取得できなくなる）。

## 参考・推奨実装パターン

- リトライ基本形: `@Retryable(retryFor={...}, exceptionExpression="@customRetryCondition.shouldRetry(#root)", maxAttempts=3, backoff=@Backoff(delay=1000, maxDelay=5000, multiplier=2, random=true))`。依存は `org.springframework.retry:spring-retry`。
- ヘルスチェック Probe 設定（`failureThreshold`/`periodSeconds`/`initialDelaySeconds` 等）は deployment.yaml に記述。Liveness の `successThreshold` は 1 固定。
- 閉塞制御の場所別実装（AGW/Ingress/API Management/プログラム）、AGW/APIM/Ingress の流量制御パラメータは `208`・`209` を参照。
