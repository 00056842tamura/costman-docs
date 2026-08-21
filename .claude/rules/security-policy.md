# セキュリティポリシー（全社共通）

## 基本方針
- セキュリティはコードレビューの必須観点とする
- 疑わしい場合は実装を止めてセキュリティチームに相談する

## 禁止事項
- パスワード・API キー・トークンをコードにハードコードしない（環境変数 / `application.yml` の外部化で管理）
- SQL を文字列結合で組み立てない（MyBatis Mapper XML のプレースホルダーを使用）
- ユーザー入力をそのまま HTML に出力しない（Thymeleaf の `th:text` 等でエスケープを徹底）
- 内部エラーの詳細を API レスポンスに含めない（`ApplicationException` 経由でメッセージキー化）

## 認証・認可
- 全 API エンドポイントに認証を必須とする（明示的に除外する場合は Javadoc で理由を記載）
- 認可は2層で分担する（詳細は下記「実装レベル」§アクセス制御）。URL/HTTP メソッドの認可は Spring Security の Java Config（`authorizeHttpRequests`）、データ（レコード・ファイル所有権）の認可はビジネスロジック（Service 層）で実施する
- `@PreAuthorize` の使用は Java Config で表現できない場合に限定する（`.claude/rules/springer-api-call.md` 参照）
- ⚠️ APIキー認証を使用する場合、`ApiKeyPreAuthenticatedProcessingFilter` の DEBUG ログにAPIキーが出力される。**運用時は `OFF` または `INFO` 以上に設定すること**（詳細は `.claude/rules/springer-security-config.md` 参照）

## データ取り扱い
- 個人情報・機密情報はログに出力しない（`@Sensitive` + `MaskingToStringBuilder` を使用）
  - 詳細は `.claude/rules/springer-masking.md` 参照
- パスワードはハッシュ化して保存する（プロダクトごとに採用アルゴリズムを選定）
- 本番 DB への直接アクセスは禁止

## 例外・ログ
- データアクセス例外（DB / HTTP）を Repository 以外で `catch` しない（`.claude/rules/springer-exception.md` 参照）
- ログには機密情報を出力しない（`.claude/rules/springer-logging.md` 参照）

## AI 利用時の注意
- 本番データ・個人情報をプロンプトに含めない
- 生成コードは必ずセキュリティ観点でレビューしてからマージする

---

## 実装レベルのセキュリティ規約

> 出典: アプリケーション開発ガイド `06/02.セキュリティチェックリスト解説`（v2.4 / v2.5）

### ログイン失敗・認証イベントのログ記録

- ✅ **必須 (ALWAYS)**: 認証イベントは拡張部品「認証イベントログ」で出力する（自前実装でなく `logging.level` でリスナーのレベルを設定）

| イベント | ログレベル | 出力項目 |
|---|---|---|
| 認証成功（`success`） | INFO | ユーザー ID、リモート IP |
| セッション ID 変更（`sessionFixationProtection`） | DEBUG | 旧/新セッション ID |
| **認証失敗（`badCredentials`）** | WARN | ユーザー ID、リモート IP、パスワード（ログ例では `password=xxxxxxxx`） |

- ✅ **必須 (ALWAYS)**: グローバル例外ハンドラに委譲された例外は拡張部品が自動で ERROR 出力するため、アプリ側の追加出力は不要
- ✅ **必須 (ALWAYS)**: catch 句や `@ExceptionHandler` で握り潰す例外は、必要に応じてアプリ自身でログ出力する（システムエラーとして応答する場合は ERROR 必須）

### XSS 対策（Thymeleaf）

- ✅ **必須 (ALWAYS)**: 動的値の出力は `th:text` および `th:`プレフィックス属性（`th:value` 等）を使う（`< > & " '` を自動エスケープ）
- 🚫 **禁止 (NEVER)**: 信頼できないデータに `th:utext` 属性・`[(...)]` インライン記法を使わない（エスケープせず生値を出力する）
- 🚫 **禁止 (NEVER)**: `<script>` 要素・イベントハンドラ属性・`<style>` 要素・style 属性を動的生成しない
- ✅ **必須 (ALWAYS)**: 絶対パス URL の生成は `UriComponentsBuilder.fromHttpUrl()` を使い `http:`/`https:` に限定する
- ✅ **必須 (ALWAYS)**: `Content-Security-Policy` はデフォルト未出力のため Java Config（`HttpSecurity#headers().contentSecurityPolicy(...)`）で設定する（例: `default-src 'self'; frame-ancestors 'none'`）

### インジェクション対策（LDAP / OS コマンド / HTTP ヘッダ）

- ✅ **必須 (ALWAYS)**: OS コマンドに動的値を使う場合は実行基盤の同等機能を使うか、シェルを介さず実行する
- ✅ **必須 (ALWAYS)**: LDAP / XPath クエリの動的値は検証・エスケープする（危険文字を含まないことを検証）
- ✅ **必須 (ALWAYS)**: HTTP ヘッダ出力は実行基盤提供 API（Spring `HttpHeaders`/`ResponseEntity`/`ResponseCookie`、Servlet `HttpServletResponse`/`Cookie`、Spring Security）を使う（改行コードを無効化する）
- 🚫 **禁止 (NEVER)**: Servlet API 直接使用は Spring Framework API / Spring Security で実現できない場合に限定する

### アクセス制御

- ✅ **必須 (ALWAYS)**: 権限制御を 2 層で分担する
  - URL パス（＋ HTTP メソッド）→ Spring Security の認可（Java Config `authorizeHttpRequests`）でロールベース制御
  - DB レコード・ファイル等のデータ → ビジネスロジックで権限確認
- ✅ **必須 (ALWAYS)**: 権限はロールベースかつ許可リスト方式で管理し、ロール情報は `UserDetailsService` で取得する
- ⚠️ **要整合**: ガイドは「Controller への `@PreAuthorize` は Java Config で表現できない場合に限る」とし、本ポリシー上部は「認可チェックは Service 層で実施」とする。`.claude/rules/springer-security-config.md` の「要整合」項とあわせて確定すること

## 関連ルール
- 例外ハンドリング: `.claude/rules/springer-exception.md`
- ロギング: `.claude/rules/springer-logging.md`
- マスキング: `.claude/rules/springer-masking.md`
- API 呼び出し: `.claude/rules/springer-api-call.md`
- 認証・認可（SecurityConfig）: `.claude/rules/springer-security-config.md`
- 暗号・ハッシュ: `.claude/rules/springer-crypto.md`

## 補足（原典に未記載・要確認）

- 認証失敗ログの「エラーコード格納」要否はガイド記載なし（個別出力項目として明記されるのは「パスワード」のみ）。
- 認証失敗ログのパスワードが「マスク後」か「実値」かは原典で曖昧。
- ログイン失敗回数閾値・アカウントロック・レート制限の具体仕様はガイド未記載（外部解説リンク参照）。
