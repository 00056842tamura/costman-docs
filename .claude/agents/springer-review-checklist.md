# Springer レビュー観点台帳

`/springer-review` の 3 レビューエージェント（controller / service / repository）が**起点として参照する単一チェックリスト**。
各観点は 1 行 = 1 チェック。詳細な規約文言・実装例が必要な場合のみ「出典」のルールファイルを開く（全ルールの常時全読込は不要）。

**凡例**
- 分類: `MUST`=規約 NEVER / ALWAYS 違反（修正必須）/ `SHOULD`=推奨・改善（修正推奨）/ `MAY`=条件付き・参考（条件成立時のみ評価）
- 適用条件: `常時`=必ず評価 / それ以外=条件に合致する場合のみ評価（非該当時は「非該当」と判定）
- 判定: 各エージェントは担当観点すべてに **準拠 / 違反 / 非該当** のいずれかを必ず付与する

---

## A. 共通観点（全レイヤー）

| ID | 観点 | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|
| CMN-01 | コンストラクターインジェクションのみ（`@Autowired` フィールド禁止・セッター禁止） | MUST | springer-di-bean.md | 常時 |
| CMN-02 | メンバー変数に `final` を付与 | MUST | springer-di-bean.md | 常時 |
| CMN-03 | 1 クラス 1 コンストラクタ・引数/メンバーはインターフェース型 | MUST | springer-di-bean.md | 常時 |
| CMN-04 | Lombok 不使用（`import lombok.` があれば即違反） | MUST | springer-tech-stack-dependency.md | 常時 |
| CMN-05 | スターインポート禁止（テスト用 static import を除く）・未使用 import 禁止 | MUST | springer-coding-style.md | 常時 |
| CMN-06 | `protected` 修飾子不使用 | MUST | springer-coding-style.md | 常時 |
| CMN-07 | 命名規約（クラス=PascalCase / メソッド=camelCase / 定数=SNAKE / パッケージ小文字・`_`禁止） | MUST | springer-package-class-naming.md | 常時 |
| CMN-08 | Service/Repository 実装クラス名は `XxxServiceImpl`/`XxxRepositoryImpl`（`Impl` 必須）・インターフェース `I` 接頭辞禁止・ユーティリティは `***Util`（`***Utils` 不可）・例外 `Exception`/エラー `Error` 接尾語 | SHOULD | springer-package-class-naming.md | 常時 |
| CMN-09 | Javadoc を `private` 含む全要素に記述（`@Override`/`@Test` 等除外） | SHOULD | springer-coding-style.md | 常時 |
| CMN-10 | import 順序 `java.→javax.→org.→com.→その他`（static は末尾）・修飾子順序（public→protected→なし→private）・メソッド 300 行以内（150 行推奨） | MUST | springer-coding-style.md | 常時 |
| CMN-11 | 文字列比較は定数側 `equals`（`定数.equals(変数)`）・ラッパーは `valueOf`・`equals` override 時は `hashCode` も・`finalize` 禁止 | MUST | springer-coding-style.md | 該当時 |
| CMN-12 | 広スコープ catch 禁止（`Exception`/`Throwable`/`RuntimeException`）・例外の握りつぶし禁止（log か rethrow 必須） | MUST | springer-exception.md | 常時 |
| CMN-13 | 独自例外は `ApplicationException`/`SystemException` 継承・第1引数メッセージキー・コンストラクタ引数（第2:配列/第3:詳細/第4:原因） | MUST | springer-exception.md | 例外定義時 |
| CMN-14 | ロガーは `LoggerFactory.APP.getLogger()`・`private static final`・`org.slf4j` 直接禁止 | MUST | springer-logging-masking.md | ログ出力時 |
| CMN-15 | ログは SLF4J プレースホルダ `{}`・例外は第2引数 Throwable・`isDebugEnabled` 事前チェック（コスト大時）・文字列連結/`System.out`/`printStackTrace` 禁止 | MUST | springer-logging-masking.md | ログ出力時 |
| CMN-16 | `toString()` は `MaskingToStringBuilder`・機密フィールドに `@Sensitive`・ログ非出力に `@ToStringExclude`・機密をプレースホルダ直渡し禁止 | MUST | springer-logging-masking.md | モデル/機密扱い時 |
| CMN-17 | メッセージキー命名 `{prefix}.{種別}.{名前}`（業務=`error.business`/システム=`error.system`/画面=`message`/バリデ=`validation`/ログ=`log`）・`ErrorCode` 定数化 | SHOULD | springer-message-i18n.md | メッセージ使用時 |

---

## B. Controller 観点

| ID | 観点 | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|
| CTL-01 | アプリ種別アノテーション（US-MPA=`@Controller` / US-API・BS=`@RestController`） | MUST | springer-controller.md | 常時 |
| CTL-02 | Controller でのクラス継承・インターフェース実装禁止 | MUST | springer-package-class-naming.md | 常時 |
| CTL-03 | ハンドラは `public`・US=`@GetMapping`/`@PostMapping` / BS=`@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`・戻り値型選択（String/ResponseEntity/ModelAndView/void） | MUST | springer-controller.md | 常時 |
| CTL-04 | リクエスト受取（`@PathVariable`/`@RequestParam`/`@RequestHeader`/`@CookieValue`/`MultipartFile`）・`@PathVariable` 変数名はパス変数 `{}` と一致 | SHOULD | springer-controller.md | 常時 |
| CTL-05 | `@Validated` 起動・`BindingResult` は Form の直後に配置・画面系=BindingResult あり/API系=なし | MUST | springer-validation-input.md | 入力チェック時 |
| CTL-06 | バリデーショングループ化（`@Validated({Create.class, Default.class})`）の際は `Default.class` を必ず併記する | MAY | springer-validation-input.md | グループ化時 |
| CTL-07 | 相関チェックは Spring Validator（`implements Validator`）+ `@InitBinder addValidators` で実装する（`@AssertTrue` は Form 内軽量チェックに限る） | MUST | springer-validation-input.md | 相関チェック時 |
| CTL-08 | Form → モデルクラスへ変換してから Service を呼ぶ・変換は専用 private メソッドに分離 | SHOULD | springer-controller.md | 変換時 |
| CTL-09 | レスポンス：`model.addAttribute`/`redirect:`/`RedirectAttributes`(flash)・`HttpServletResponse` 直接利用禁止 | MUST | springer-controller.md | 常時 |
| CTL-10 | `ConflictException`/`ResourceNotFoundException`/`ApplicationException` を `@ExceptionHandler` で処理（API=`RestErrorInfo` / MPA=エラー画面）・該当外は `SystemException` でエスカレーション | MUST | springer-exception.md | 例外ハンドリング時 |
| CTL-11 | グローバル例外ハンドラのレスポンス種別設定（BS=`REST_ONLY`、Thymeleaf+Ajax US=`ANY`）・未ハンドリング MVC 例外ステータス整合（400/404/405/406/415/500/503） | MAY | springer-exception.md | 設定時 |
| CTL-12 | Controller から Service を複数回呼び出さない（1 トランザクション）・Form/Request をそのまま Service に渡さない | MUST | springer-service-transaction.md | 常時 |
| CTL-13 | HTTP ステータス使い分け（200/201/204/400/404/409/503）・201 時 `Location` ヘッダ設定・リソース 0 件は 200 返却・API バージョンはパスに組み込む（ヘッダー・クエリ禁止） | MUST | springer-controller.md / springer-api-style.md | API 系 |
| CTL-14 | セッション：`@SessionAttributes`+`@ModelAttribute`(名前必須/`binding=false`)・`SessionStatus.setComplete()`・`@SessionScope`+`Serializable`・属性キー定数化 | MUST | springer-controller.md | セッション使用時（MPA） |
| CTL-15 | 二重送信防止：更新系に `@TokenCheck`・`th:action`・CSRF 機構流用禁止・Ajax 時はボタン無効化のみ | MUST | springer-file-prevention.md | MPA 更新系 |
| CTL-16 | ページネーション：大量データを全件表示しない・ページサイズ上限設定（`OutOfMemoryError` 防止） | MUST | springer-file-prevention.md | 大量データ一覧時 |
| CTL-17 | `@Controller` 内で JSON を返すメソッドに `@ResponseBody` 付与 | MAY | springer-controller.md | Ajax/JSON 返却時（`@Controller` 使用時） |
| CTL-18 | ファイルアップロード：`MultipartFile` 受取・サイズ上限・`@UploadFile*` バリデーション・`getInputStream()` は使用後 close | MUST | springer-file-prevention.md | アップロード時 |
| CTL-19 | ファイルダウンロード：`ResponseEntity<FileSystemResource>`・`ContentDisposition.filename(name, UTF_8)` | MUST | springer-file-prevention.md | ダウンロード時 |
| CTL-20 | SecurityConfig：認証=401/認可=403・CSP 設定・API は CSRF disable・`BCryptPasswordEncoder(12)` 以上・`@PreAuthorize` は Java Config で表現できない場合のみ使用 | MUST | springer-security-auth.md | SecurityConfig/認可制御時 |
| CTL-21 | GraphQL サーバー：mutation 禁止・`@BatchMapping`（`@SchemaMapping` 禁止）・深度/同時実行制限 Bean 登録 | MUST | springer-api-style.md | GraphQL Controller 時 |
| CTL-22 | Form/Request フィールドはラッパー型使用（`Integer`/`Long`/`Boolean`/`BigDecimal` 等）・`int`/`long`/`boolean` 等プリミティブ禁止（null＝未入力を表現できないため） | MUST | springer-validation-input.md | Form/Request 定義時 |
| CTL-23 | BS・オープン型 US は OpenAPI 対応（Controller に `@Tag`・ハンドラに `@Operation`/`@ApiResponses`・引数に `@Parameter`・モデルに `@Schema`）・本番/開発環境で `api-docs` 無効化（`springdoc.api-docs.enabled: false`） | MUST | springer-openapi.md | BS / オープン型 US 時 |

---

## C. Service 観点

| ID | 観点 | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|
| SVC-01 | `@Service` 付与 | MUST | springer-package-class-naming.md | 常時 |
| SVC-02 | `@Transactional` は BS Service 実装のみ（クラスレベル）・US Service には付けない | MUST | springer-service-transaction.md | 常時 |
| SVC-03 | 同一クラス内の `@Transactional` 自己呼び出し禁止 | MUST | springer-service-transaction.md | 常時 |
| SVC-04 | チェック例外 throw 禁止（`RuntimeException` 系でラップ・ロールバックさせるため非チェック例外を使用） | MUST | springer-exception.md / springer-service-transaction.md | 常時 |
| SVC-05 | データアクセス例外（`DBDataAccessException`/`RestHttp*Exception` 等）を catch しない | MUST | springer-exception.md | 常時 |
| SVC-06 | Controller の Form/Request を引数に受け取らない（モデルクラスを使用） | MUST | springer-architecture-layer.md | 常時 |
| SVC-07 | `@Retryable`：べき等処理のみ適用・`@Transactional` 内部での直接呼び出し禁止（前段に配置）・API は HTTP 502/504 と I/O エラーのみ対象・`maxAttempts`+`@Backoff` 設定必須 | MUST | springer-resilience.md | リトライ時 |
| SVC-08 | リトライ有効化順序：`@Transactional` より前段に配置（Spring Retry 2.0.1 以降は既定前置、明示的な `@Order` または `@EnableRetry` 設定を確認） | MUST | springer-resilience.md | リトライ時 |
| SVC-09 | リトライキャッシュ（`@Cacheable`+`@RequestScope`）をロック付き検索・検索結果で更新するトランザクションに使用しない | MUST | springer-resilience.md | キャッシュ時 |
| SVC-10 | 複数リソース更新：単一リソース原則・「更新→更新」が避けられない場合は TCC/Saga 要否を検討する | MUST | springer-resilience.md | 複数リソース更新時 |
| SVC-11 | TCC：Try/Confirm/Cancel フェーズ実装・Try/Confirm 時は論理削除（物理削除禁止）・Cancel フェーズで Try の仮登録を物理削除・TCC-ID/NO 管理 | MUST | springer-resilience.md | TCC 採用時 |
| SVC-12 | Saga：補償トランザクション実装・Isolation 非担保（ロストアップデート/ダーティリード）を前提に設計 | MUST | springer-resilience.md | Saga 採用時 |
| SVC-13 | 排他例外（`ConflictException` 等）を握りつぶさず上位へエスカレーションする | MUST | springer-repository-mybatis.md / springer-exception.md | 排他制御時 |
| SVC-14 | `@Transactional` はクラスレベル付与を優先・制御不要なメソッドがある場合のみ該当メソッドに個別付与する | SHOULD | springer-service-transaction.md | BS Service 時 |

---

## D. Repository / Mapper 観点

| ID | 観点 | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|
| REP-01 | `@Repository` 付与 | MUST | springer-package-class-naming.md | 常時 |
| REP-02 | BS=MyBatis Mapper 経由（直接 JDBC 禁止）/ US=RestClient・RestTemplate（DB 直アクセス禁止） | MUST | springer-architecture-layer.md / springer-repository-mybatis.md | 常時 |
| REP-03 | SELECT 1 件で null → `ResourceNotFoundException` スロー | MUST | springer-exception.md | 単一検索時 |
| REP-04 | UPDATE/DELETE 更新件数チェック（`count != 1`）→ `ConflictException` スロー（楽観ロック失敗検出） | MUST | springer-exception.md / springer-repository-mybatis.md | 更新時 |
| REP-05 | INSERT `DBDuplicateKeyException` → `ConflictException` ラップ（version 初期値 1 セット） | MUST | springer-exception.md | INSERT 時 |
| REP-06 | DB/HTTP データアクセス例外を Service/Controller に漏らさない（Repository 内で業務例外へ変換） | MUST | springer-exception.md | 常時 |
| REP-07 | Mapper に `@Mapper`・命名 `{エンティティ名}Mapper`・複数引数に `@Param` | MUST | springer-repository-mybatis.md | Mapper 時 |
| REP-08 | Mapper 戻り値型（1件=モデル/複数=`List`/大量=`Cursor<T>`/更新=`int`・`boolean`・`void`）・SELECT 1件で null は「存在しない」を意味する | MUST | springer-repository-mybatis.md | Mapper 時 |
| REP-09 | XML 同名・同一パッケージ配置・`namespace` フルパス・SQL 要素 `id`=メソッド名 | MUST | springer-repository-mybatis.md | Mapper XML 時 |
| REP-10 | `<select>` で `resultType`/`resultMap` 両指定禁止・`map-underscore-to-camel-case` 設定前提 | MUST | springer-repository-mybatis.md | Mapper XML 時 |
| REP-11 | `#{}` 使用・null 許容カラムに `jdbcType` 指定・`${}` にユーザー入力を渡さない（SQL インジェクション対策） | MUST | springer-repository-mybatis.md / springer-security-checklist.md | Mapper XML 時 |
| REP-12 | 親子関係オブジェクトは `resultMap`（`<id>`/`<result>`/`<association>`）・単純マッピングは `resultType` を使い分ける | MAY | springer-repository-mybatis.md | 親子マッピング時 |
| REP-13 | 動的 SQL は `<if>`/`<where>`/`<set>`/`<choose>`/`<foreach>` を使用（文字列連結禁止） | MAY | springer-repository-mybatis.md | 動的 SQL 時 |
| REP-14 | 楽観ロック：`SET version = #{version}+1` ＋ `WHERE ... AND version = #{version}`・INSERT 時 `version=1` | MUST | springer-repository-mybatis.md | 楽観ロック時 |
| REP-15 | ユニーク番号採番は悲観ロック（`SELECT ... FOR UPDATE`）・楽観ロック不可 | MUST | springer-repository-mybatis.md | 採番時 |
| REP-16 | 全外部接続にタイムアウト（JDBC `socketTimeout` 必須・`0` 無制限放置禁止） | MUST | springer-config.md | 外部接続時 |
| REP-17 | RestClient URI はプレースホルダ（`{}`）+ `build` で展開・`@Value` で baseUri 注入（文字列連結禁止） | MUST | springer-api-client.md | US Repository 時 |
| REP-18 | ファイルダウンロードは `exchange()`（`retrieve()` 禁止）・一時ファイルを `TemporaryFileService#register` で削除登録 | MUST | springer-file-prevention.md | ダウンロード時 |
| REP-19 | 一時ファイル：`Files.createTempFile`・`try-finally`/`register` で削除・削除失敗は warn 継続 | MUST | springer-file-prevention.md | 一時ファイル時 |
| REP-20 | SOAP：`WebServiceTemplate` をコンストラクタ注入・3 例外（`WebServiceTransportException`/`SoapFaultClientException`/`WebServiceIOException`）を catch して独自例外ラップ | MUST | springer-api-client.md | SOAP 時 |
| REP-21 | Azure：`BlobContainerClient`/`SecretClient` をコンストラクタ注入・認証情報を環境変数（ConfigMap/Secret）で外部化 | MUST | springer-cloud-storage-secret.md | Azure 時 |
| REP-22 | GraphQL クライアント：`HttpSyncGraphQlClient` 注入・`FieldAccessException`/`ResourceAccessException` 処理 | MUST | springer-api-style.md | GraphQL クライアント時 |

---

## E. 横断観点（orchestrator が補助確認・条件付き）

| ID | 観点 | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|
| X-01 | 設定ファイル構成（profile・`@Value`/`@ConfigurationProperties`・AutoConfigure 優先・`application.yml` 原則空） | SHOULD | springer-config.md | 設定ファイル変更時 |
| X-02 | トレーシング・IF ログ運用レベル（マスク不可カテゴリを OFF/INFO 以上）・MyBatis パラメータ抑止 | MUST | springer-logging-masking.md | logging 設定時 |
| X-03 | Actuator 公開制御（`exposure.include` 限定・`*` 禁止）・BASIC 認証・Readiness/Liveness | MUST | springer-resilience.md | Actuator 利用時 |
| X-04 | 流量制御（Tomcat/HikariCP/HttpClient パラメータ）・閉塞制御（ServletFilter→503） | SHOULD | springer-resilience.md | 該当設定時 |
| X-05 | バッチ：1 ジョブ 1 シェル・OS スケジューラ禁止・リラン性・戻り値変換・タイムアウト | MUST | springer-batch.md | batch スタック時 |
| X-06 | API 認証認可方式：Implicit/ROPC 不使用・PKCE・JWT 署名/exp/iss/aud 検証・トークン/鍵のログ出力禁止 | MUST | springer-security-auth.md | 認証認可方式選定時 |
| X-07 | ユニットテスト：`〇〇Test`（単数）・同一パッケージ・層別構成（Controller=MockMvc / Service=コンストラクタ new / Repository=`@Transactional`+`@Sql`） | SHOULD | springer-testing.md | テストコード時 |
| X-08 | E2E：全層横断シナリオ・外部システムアクセス・実行前後の DB 差分確認 | SHOULD | springer-testing.md | E2E テスト時 |

---

## 使い方（各エージェント共通）

1. 本台帳を読み込み、自レイヤー（共通 ＋ B/C/D のいずれか）＋ 該当する横断観点を担当範囲とする。
2. 対象ファイルを読み、性質（アプリ種別・GraphQL/SOAP/Azure/ファイル入出力/セッション/Ajax/一時ファイル/リトライ 等の使用有無）を判定し、各観点の「適用条件」に照らして該当/非該当を決める（#2 関連性選別）。
3. 詳細な規約文言・実装例が必要な観点、または違反が疑われる観点のみ「出典」列のルールファイル（例: `.claude/rules/springer-di-bean.md`）を絶対パスで開いて確認する（全ルール常時全読込は不要）。
4. 担当観点**すべて**に `準拠 / 違反 / 非該当` を付与し、**未評価 0 件**を保証する（#1 網羅）。
