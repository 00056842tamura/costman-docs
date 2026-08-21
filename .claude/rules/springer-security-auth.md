---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 13 認証・認可

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: OWASP チェックリストは [springer-security-checklist.md](springer-security-checklist.md)。

## 🚫 禁止 (NEVER)

### R-13-N1 外部不特定多数向けに API キー認証を使わない
クライアントの正しさを確認できないため、アクセス元を制限できない／外部の不特定多数からのリクエストを想定するシステムでは API キー認証を利用しない。

### R-13-N2 API キーをログに出力しない
API キーをログ等に出力しない。`ApiKeyPreAuthenticatedProcessingFilter` の DEBUG ログには API キーが平文出力されるため、運用時は当該ログカテゴリを `OFF` または `INFO` 以上にする（[springer-logging-masking.md](springer-logging-masking.md) R-11-01 と同旨）。調査等で必要な場合は十分なマスクまたはハッシュ化を行う。

### R-13-N3 アクセストークンのスコープで権限制御をしない
アクセストークンのスコープを認可（権限制御）に使わない。ユーザーに紐付く権限はアプリ側（DB）で管理し、取得した権限を SecurityContext に渡して `hasAuthority`／`hasAnyAuthority` で制御する。

## ⚠ 非推奨 (AVOID)

### R-13-A1 Azure AD 単体方式は新規採用しない
Azure AD 単体の認証基盤方式は廃止予定のため新規採用しない。現行は Azure AD + Cognito 併用方式（SPA / MPA の 2 構成）を用いる（`@PreAuthorize` の限定は R-13-01）。

## ✅ 必須 (ALWAYS)

### R-13-01 @PreAuthorize の使用は Java Config で表現できない場合に限定する
エンドポイント認可は Spring Security の Java Config（`SecurityConfig` の `authorizeHttpRequests`）で集中・宣言的に表現するのを原則とし、`@PreAuthorize` は Java Config で表現できない場合に限る。
- 理由 / 背景: `@PreAuthorize` は Controller 各所に分散し可読性・一貫性を損なうため。

### R-13-02 認証は Spring Security で実装する
ログイン・権限は Spring Security の機能を使用する。必須クラスは `SecurityConfig`・`LoginController`・`UserDetailsService` 実装・`UserDetails` 実装。認証方式は FORM 認証と API 認証（画面なし、認証後もステートフル）。

### R-13-03 認可は 2 箇所で行う
(1) エンドポイント（URL パス・HTTP メソッド）に対するロールベース権限制御＝Spring Security（`SecurityConfig`）、(2) データ（DB レコード・ファイル）に対する権限制御＝ビジネスロジック。
- 理由 / 背景: URL 認可だけでは他ユーザーのデータ参照を防げない。

### R-13-04 公開リソース以外は認証必須にする
`SecurityConfig` で `anyRequest().authenticated()` とし、公開/静的リソース（`/css/**`,`/js/**`,`/img/**` 等）・ログイン/ログアウト処理のみ `permitAll()` にする。

### R-13-05 DB 格納パスワードはハッシュ化する
パスワードはハッシュ化して DB に格納する。ハッシュ化アルゴリズムは `SecurityConfig` の `PasswordEncoder` Bean で決定し、認証時照合と登録時ハッシュ化で同一 Bean を使う。アルゴリズムは `Argon2PasswordEncoder` または `BCryptPasswordEncoder`（OWASP 推奨）。ソルトはランダム値で付加する。

### R-13-06 パスワード等の機密フィールドは @Sensitive でマスクする
`CustomUserDetails` のパスワードフィールドに `@Sensitive` を付与し、`toString()` は `MaskingToStringBuilder.toString(this)` を使う（[11](springer-logging-masking.md)）。

### R-13-07 API キーはハッシュ化して保存・照合する
API キーは平文保持せず、クライアント毎に異なるソルト付きハッシュ（既定 BCrypt、不可なら PBKDF2）で保存し、送られた API キーをハッシュ化して比較する。API キーは 128bit 以上（256bit 以上推奨）の暗号論的安全乱数で、接続元ごとに別キーとする。

### R-13-08 認証失敗時は詳細を返さず同一メッセージにする
認証失敗（`badCredentials`/`disabled`/`expired`/`locked` 等）はすべて同一メッセージにする。FORM 認証の失敗はログイン画面へ遷移、API 認証（画面なし）の失敗は 401、API キー認証の失敗は原則 403、システムエラーは 500 とする（401/403/500 はボディなし）。
- 理由 / 背景: 攻撃者にヒント（ユーザー ID 存在等）を与えないため。

### R-13-09 認証成否をログに記録する
認証成功＝INFO、認証失敗＝WARN（ユーザー ID・リモート IP 等）で記録する（`AuthenticationEventListeners` の `@EventListener`）。

### R-13-11 認証基盤利用時のトークン検証は US が行う
認証基盤（Azure AD）利用時、アクセストークン/ID トークンの検証は US アプリが行う。SPA の US はステートレス（`SessionCreationPolicy.STATELESS`）で検証結果・ユーザー情報をセッション等に保持しない。MPA の US は `oauth2Login` を用い、認証情報を Spring Security（SecurityContext）/セッションに保持する。

### R-13-12 トークンの検証クレームを満たす
- SPA: ID トークンの 署名（Azure AD/Cognito の公開鍵）・有効期間（`nbf`/`exp`）・発行元（`iss`＝設定値 issuer-uri）・アプリID（`aud`＝設定値 audiences）を検証する。
- MPA: `nbf`/`exp`/`iss`/`sub`/`aud`/`iat`/`azp`/`cnf` の 8 クレームを検証する（OIDC ID Token Validation 準拠）。

### R-13-13 認証基盤の役割分担とレスポンス
認証を行うユーザー情報の DB アクセスは BS アプリが担当する。トークン検証失敗時は US が 401 を返す。CSP ヘッダーを必ず設定する（`default-src 'self'; connect-src 'self' login.microsoftonline.com; frame-ancestors 'none'`）。

## ✨ 推奨 (PREFER)

### R-13-10 BCrypt のストレッチング回数はデフォルト 10 より大きくする
`new BCryptPasswordEncoder(12)` のようにデフォルト 10 より大きい値を使う（ログイン処理が 1 秒未満程度になるよう調整）。

## 条件付き事項

### R-13-C1 バージョンによる認可設定の差異
Springer 2.0 以上は `requestMatchers()`＋`hasAuthority("ADMIN")`（プレフィックス不要）。1.6 以上 2.0 未満は `mvcMatchers()`＋`hasRole("ADMIN")`（`UserDetails` 登録時にロール名へ `ROLE_` を付与）。Springer 1.6 以上では `WebSecurityConfigurerAdapter` の継承は不要。

### R-13-C2 API キー認証の構成（BS アプリ）
`springer.application-layer` を `NONE` に変更し `ApiKeyConfigurer` で有効化、FORM/Basic/匿名認証を無効化、`SessionCreationPolicy.STATELESS`、CSRF 無効化。`CustomUserDetails` と `ApiKeyUserDetailsService` 実装を作成。ヘッダーは `Authorization: ApiKey {ID}.{APIキー}`。

### R-13-C3 API キー認証は権限による出し分けに使わない
API キー認証は正しいキーを無条件で許可する仕様のため、ユーザー毎の情報出し分けが必要な場合はこの方式を使わない。

### R-13-C4 API キー認証をインターネットで使う場合は IP 許可リスト＋HTTPS
インターネットで使う場合は接続元を IP アドレス許可リストで制御し、通信経路を HTTPS で暗号化する（両方必須）。

### R-13-C5 連続認証失敗時のアカウントロック
PCI DSS 対象は必須、それ以外は任意。`isAccountNonLocked()` をカスタム実装し失敗回数/ロック日時を DB 保持する（例: 6 回失敗でロック、30 分で自動解除）。

### R-13-C6 機微情報の暗号化はサーバ側で Spring Security 暗号機能を使う
暗号・復号は Spring Security の `Encryptors`（256-bit AES、暗号利用モードは GCM）を使う。テキストは `Encryptors.delux`、バイナリは `Encryptors.stronger`。パスワード/ソルトは K8s Secret や Azure Key Vault 等から取得する。

### R-13-C8 データ同一性確認のハッシュは MessageDigest（SHA-256 以上）を使う
パスワード/API キー以外で、データの同一性・改竄検知のためにハッシュを使う場合は `MessageDigest` を用い、アルゴリズムは SHA-256 以上（SHA-256/384/512）とする（パスワード保管のハッシュ＝R-13-05 とは用途が異なる）。

## 参考・推奨実装パターン
