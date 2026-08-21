---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 16 セキュリティチェックリスト（OWASP 準拠）

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: 認証・認可の実装規約は [springer-security-auth.md](springer-security-auth.md)。本ファイルは OWASP カテゴリ別の代表規範を要点化。
> 記法: OWASP カテゴリ（A00〜A13）で grouping し、各規範に `**R-16-xx**` を付与（Style B）。

## 🚫 禁止 (NEVER)

- **R-16-01 A02 暗号化**: 特に重要な機密データを平文でログ出力しない（拡張部品「マスキング」「MyBatis パラメーターログ抑止」を使う）。機密データ送信にクエリ文字列を使わない。
- **R-16-02 A03 インジェクション**: SQL を文字列結合で組み立てない（MyBatis は `#{}`、JdbcTemplate は型付き引数）。OGNL 式を動的生成しない。エスケープなし出力（`th:utext`・`[()]` インライン・Vue.js の `v-html`）を使わない。
- **R-16-03 A05 設定ミス**: エラー時にスタックトレース等の詳細を表示しない（`${exception}`/`${trace}` を有効化しない、Whitelabel Error Page を無効化）。ルートログレベルを DEBUG にしない。
- **R-16-04 API ガイド**: OAuth 2.0 の Implicit Grant・ROPC を使用しない。

## ⚠ 非推奨 (AVOID)

- **R-16-05 A01**: `@PreAuthorize` は Java Config（`authorizeHttpRequests`）で表現できない場合に限る（[13](springer-security-auth.md) R-13-01）。

## ✅ 必須 (ALWAYS)

### A01 アクセス制御
- **R-16-06**: 公開リソース以外は認証必須（`anyRequest().authenticated()`）。リクエスト毎に権限・認証済を確認。
- **R-16-07**: 認可は 2 箇所（Spring Security の URL/HTTP メソッド ＋ ビジネスロジックのデータ認可）。権限はロールベース＋許可リスト方式、ロールは DB から `UserDetailsService` で取得。
- **R-16-08**: 不要な HTTP メソッドを禁止（Controller で `@GetMapping`/`@PostMapping` 等を明示）。

### A02 暗号化
- **R-16-09**: 機密データ暗号化はサーバ側・256-bit AES（暗号利用モードは **GCM**。CBC は GCM 不可時のみ）。
- **R-16-10**: 暗号鍵・パスワード・ソルト・トークンは 16 バイト（128bit）以上のセキュア乱数（`KeyGenerators.secureRandom(int)`、32 バイト＝256bit 推奨）。
- **R-16-11**: パスワード保管は Argon2 または BCrypt（ストレッチング回数 > 10）。
- **R-16-12**: 外部通信は HTTPS (内部通信は可能な場合のみ)。リバプロから `X-Forwarded-Proto`/`X-Forwarded-For` を伝達（`server.tomcat.remoteip` 等）。HTTPS 時 Cookie に Secure 属性。

### A03 インジェクション
- **R-16-13**: 全出力要素をエスケープ（Thymeleaf は `th:text`）。`Content-Type` に charset 指定。
- **R-16-14**: XSS 保護ヘッダ（`X-Content-Type-Options: nosniff` 等）、CSP（`default-src 'self'`）を Java Config で設定。
- **R-16-15**: Cookie に HttpOnly 属性（独自 Cookie は `setHttpOnly(true)`）。URL 出力は `http(s)://` のみ（`UriComponentsBuilder.fromHttpUrl()`）。
- **R-16-16**: HTTP ヘッダ・Cookie・メールは実行基盤の API（`HttpHeaders`/`ResponseCookie`/`MailSender` 等）を使い自前組み立てしない。

### A05 設定ミス
- **R-16-17**: ソフトウェアアップデートを計画的に実行。依存は必要なものだけ（`<dependency>` 最小、バージョンは `<properties>` で一元管理）。
- **R-16-18**: 不要な機能・エンドポイントを無効化（Actuator 公開は必要分のみ）。
- **R-16-19**: リクエスト最大サイズを制限（`spring.servlet.multipart.max-file-size` 等）。

### A06 脆弱なコンポーネント
- **R-16-20**: 使用ライブラリ・バージョンを把握（`dependency:tree` 等）。脆弱性スキャン（`dependency-check`、CVSS しきい値 7 以上）を実施し、フィックス後にアップデート。

### A07 識別と認証
- **R-16-21**: 認証処理はサーバ側に集中実装（Spring Security）。Actuator 公開エンドポイントも認証必須。
- **R-16-22**: 認証失敗時は同一メッセージを返す（ユーザー ID 存在の推測防止）。
- **R-16-23**: 弱いパスワードを拒否（Spring Validator）。管理者初期パスワードは変更。

### A09 ログとモニタリング
- **R-16-24**: 処理の失敗をログ記録（握り潰す例外も）。システムエラー応答は ERROR、認証成功 INFO/失敗 WARN。
- **R-16-25**: トランザクション（アクセス）ログを記録（Tomcat アクセスログ有効化、トレース ID/セッション ID を含める）。

### A12 クリックジャッキング
- **R-16-26**: frame 読み込みを制限（`X-Frame-Options: DENY` は Spring Security 既定。加えて CSP `frame-ancestors` を設定）。

### A13 CSRF
- **R-16-27**: CSRF 対策は Spring Security 既定で有効（変更しない）。Thymeleaf `th:action` で自動トークン。Ajax は `X-CSRF-TOKEN` ヘッダー。Cookie セッション管理の SPA は `CsrfConfigurer::spa`。

### A00 共通
- **R-16-28**: 全入力パラメータを検証（Bean Validation/Spring Validator、保険的対策）。
- **R-16-29**: 処理失敗時に成功処理を継続しない（失敗判定は「成功以外」、例外 catch 時は必ず rethrow）。

### API 認証認可ガイド（リソースサーバ／クライアント）
- **R-16-30**: API キー＝ハッシュ化保存・接続元毎に別キー・128bit 以上乱数・ログ出力禁止・失敗詳細を返さない・インターネット時 IP 制限＋HTTPS（[13](springer-security-auth.md)）。
- **R-16-31**: クライアント側＝事前確認済 URI のみ送信・安全な場所に保存・`Authorization` ヘッダで送信・ログ出力禁止。

## ✨ 推奨 (PREFER)

- **R-16-32**: ディレクトリリスティングは無効（Spring Boot 組み込み Tomcat 既定無効を確認）。CORS／オリジン間リソース共有は最小限にする（v2.5 では A01 アクセス制御配下の規範）。
- **R-16-33**: セッションタイムアウトを設定（既定 30 分、`server.servlet.session.timeout`）。
- **R-16-34**: ログの監視・アラート・外部保管。

## 条件付き事項

### R-16-C1 XXE 対策
外部 DTD は原則受け付けない。受け付ける場合は XML パーサー（`DocumentBuilderFactory` 等）で外部一般実体・外部パラメータエンティティ・外部 DTD・XInclude・エンティティ参照展開を無効化する。

### R-16-C2 アカウントロック
連続認証失敗時にアカウントロック（PCI DSS 対象は必須、それ以外任意）。`isAccountNonLocked()` カスタム実装＋失敗回数/ロック日時を DB 保持。

### R-16-C3 独自トークン発行時
独自トークンを発行する場合は SecureRandom 128bit 以上の乱数、Cookie に `HttpOnly=true`・`Secure=true`。

### R-16-C4 API 認証方式の選定
安全な NW のサーバ間 API＝API キー認証、インターネットのサーバ間＝OAuth2.0 Client Credentials Grant、Web アプリ(MPA)＝OIDC/OAuth2.0 Code Grant、スマホ/SPA＝Code Grant(PKCE あり)。

## 参考・推奨実装パターン

- 暗号: `Encryptors.delux/stronger`(GCM)＋`KeyGenerators.secureRandom(16/32)`。パスワード保存: Repository で `passwordEncoder.encode()`＋Argon2/BCrypt Bean 登録。
- XSS/クリックジャッキング: `headers().contentSecurityPolicy("default-src 'self'; frame-ancestors 'none'")`。
- CSRF: Thymeleaf 自動トークン／Ajax `X-CSRF-TOKEN`／Cookie セッション管理の SPA `CsrfConfigurer::spa`。
- 依存脆弱性スキャン: `dependency-check-maven`/`dependency-check-gradle`（CVSS 7 以上）。

### 正準（canonical）ルール対応
本ファイルは OWASP 観点の**要約ビュー**。各規範の実装上の正典は実装規約ファイル側にある（Phase 5 で整理）。重複時は下表の canonical を優先する。

| 本ファイル | canonical（正典） |
|---|---|
| R-16-01（機密ログ/マスキング） | [11](springer-logging-masking.md) R-11-C1・R-11-N1 |
| R-16-02（SQL 結合禁止・エスケープ出力禁止） | [08](springer-repository-mybatis.md) R-08-N1 ／ [05](springer-controller.md) R-05-01 |
| R-16-05（@PreAuthorize 限定） | [13](springer-security-auth.md) R-13-01 |
| R-16-06（公開以外は認証必須） | [13](springer-security-auth.md) R-13-04 |
| R-16-07（認可は 2 箇所） | [13](springer-security-auth.md) R-13-03 |
| R-16-09/10（暗号 GCM・乱数 128bit） | [13](springer-security-auth.md) R-13-C6 |
| R-16-11（Argon2/BCrypt>10） | [13](springer-security-auth.md) R-13-05・R-13-10 |
| R-16-22（認証失敗は同一メッセージ） | [13](springer-security-auth.md) R-13-08 |
| R-16-24（処理失敗をログ・認証成否ログ） | [13](springer-security-auth.md) R-13-09 ／ [11](springer-logging-masking.md) |
| R-16-27（CSRF/二重送信） | [15](springer-file-prevention.md) R-15-04/05/08 |
| R-16-29（失敗継続しない・rethrow） | [14](springer-coding-style.md) R-14-23（java:S1166） |
| R-16-30（API キー） | [13](springer-security-auth.md) R-13-07/N1/N2/C4 |
| R-16-C2（アカウントロック） | [13](springer-security-auth.md) R-13-C5 |
