# コードレビュー基準（Reacter + Springer 版）

## レビュー必須観点（共通）

### 機能
- [ ] `docs/base-design/テストシナリオ.md` の受け入れ条件を全て満たしているか
- [ ] 正常系・異常系の処理が実装されているか
- [ ] エッジケースが考慮されているか

### プロダクト観点
- [ ] スタック間依存が `.claude/rules/product-rules.md` に従っているか
- [ ] PR メッセージに影響スタックが明示されているか

### ドキュメント
- [ ] `docs/` 配下が更新されているか
- [ ] PR メッセージに必要な情報が記載されているか

---

## Reacter（フロントエンド）固有の観点

### アーキテクチャ・規約
- [ ] `.claude/rules/reacter-folder-structure.md` のフォルダ構成・命名規則に従っているか
- [ ] `.claude/rules/reacter-coding-standards.md`・`.claude/rules/reacter-security.md` の禁止事項（`React.FC`・`any` 型・`dangerouslySetInnerHTML` 等）に違反していないか
- [ ] フォームに React Hook Form + Zod を使用しているか（`.claude/rules/reacter-form-validation.md`）
- [ ] Error Boundary が適切に実装されているか（`.claude/rules/reacter-error-handling.md`）

### コード品質
- [ ] TSDoc がすべての関数に記述されているか
- [ ] `any` 型を使用していないか
- [ ] ESLint / Prettier のルールに従っているか
- [ ] 重複コードがないか（DRY 原則）

### セキュリティ
- [ ] `.claude/rules/reacter-security.md` の禁止事項に違反していないか（XSS・CSP ワイルドカード禁止等）
- [ ] API キー・シークレットがハードコードされていないか（`VITE_` 環境変数を使用）
- [ ] 本番ビルドに `sourcemap: true` が含まれていないか

### テスト
- [ ] Vitest ユニットテストが実装されているか（`.claude/rules/reacter-testing-vitest.md`）
- [ ] テストがパスしているか
- [ ] `docs/base-design/テストシナリオ.md` のテストシナリオを網羅しているか

---

## Springer（バックエンド・サーバーサイド）固有の観点

### アーキテクチャ・規約
- [ ] レイヤー構成が正しいか（US→DB 直アクセス禁止・レイヤー越え禁止・DTO 受け渡し）（`.claude/rules/springer-architecture-layer.md`）
- [ ] Lombok 不使用・フィールドインジェクション禁止が守られているか（`.claude/rules/springer-tech-stack-dependency.md`・`.claude/rules/springer-coding-style.md`）
- [ ] コンストラクターインジェクション規約に従っているか（`final` 付与・インターフェース型・1 クラス 1 コンストラクタ）（`.claude/rules/springer-di-bean.md`）
- [ ] アプリ種別に応じたトランザクション制御が正しいか（`.claude/rules/springer-service-transaction.md`）
  - BS Service 実装クラスにのみ `@Transactional`
  - Controller / Repository / US Service には付与しない
  - 同一クラス内の自己呼び出し禁止
- [ ] DB アクセスが MyBatis Mapper XML 規約に従っているか（`.claude/rules/springer-repository-mybatis.md`）
- [ ] 排他制御（楽観ロック・バージョン番号管理・更新件数チェック）が適切か（`.claude/rules/springer-repository-mybatis.md`）
- [ ] 複数リソース更新で結果整合性手法（TCC/Saga）の要否を検討したか（`.claude/rules/springer-resilience.md`）
- [ ] セッション管理（`@SessionAttributes`/`@SessionScope`・`Serializable`）が規約どおりか（`.claude/rules/springer-controller.md`）

### コード品質
- [ ] コーディングスタイル規約に従っているか（import 順・修飾子順・メソッド行数・ラッパー型使用等）（`.claude/rules/springer-coding-style.md`）
- [ ] 命名規約に従っているか（クラス・メソッド・定数・パッケージ・接尾語等）（`.claude/rules/springer-package-class-naming.md`）
- [ ] Form/Request フィールドはラッパー型（`Integer`/`Long`/`Boolean` 等）を使用しているか（`.claude/rules/springer-validation-input.md`）
- [ ] BS・オープン型 US は OpenAPI 対応（`@Tag`/`@Operation`/`@Schema` 等）・本番環境で `api-docs` 無効化しているか（`.claude/rules/springer-openapi.md`）
- [ ] 重複コードがないか（DRY 原則）
- [ ] Javadoc が `private` を含む全要素に記述されているか

### 例外・ロギング
- [ ] 独自例外は `ApplicationException`/`SystemException` を継承しているか（`.claude/rules/springer-exception.md`）
- [ ] データアクセス例外を Repository 以外で `catch` していないか（`.claude/rules/springer-exception.md`）
- [ ] ロガーは `LoggerFactory.APP.getLogger()` で取得しているか（`.claude/rules/springer-logging-masking.md`）
- [ ] 機密情報のマスキング（`@Sensitive` + `MaskingToStringBuilder`）が適切か（`.claude/rules/springer-logging-masking.md`）
- [ ] グローバル例外ハンドラの MVC 例外ステータス（400/404/405/415/500 等）が整合しているか（`.claude/rules/springer-exception.md`）
- [ ] IF ログのマスク不可カテゴリが運用時 `OFF` または `INFO` 以上か（`.claude/rules/springer-logging-masking.md`）

### セキュリティ
- [ ] `.claude/rules/security-policy.md` の禁止事項に違反していないか
- [ ] 入力値のバリデーション（Bean Validation + Spring Validator）が実装されているか（`.claude/rules/springer-validation-input.md`）
- [ ] 認証・認可が適切に実装されているか（`.claude/rules/springer-security-auth.md`）
- [ ] US アプリから DB への直接アクセスがないか（`.claude/rules/springer-architecture-layer.md`）
- [ ] SecurityConfig（認証=401 / 認可=403・CSP・`BCryptPasswordEncoder(12)` 以上・`@PreAuthorize` 限定使用）が規約どおりか（`.claude/rules/springer-security-auth.md`）
- [ ] API 認証認可方式の選定が適切か（Implicit / ROPC 不使用・PKCE・JWT クレーム検証）（`.claude/rules/springer-security-auth.md`）
- [ ] 暗号化・ハッシュ・API キー/シークレット管理が規約どおりか（AES-GCM・Argon2/BCrypt）（`.claude/rules/springer-security-auth.md`）
- [ ] 全外部接続にタイムアウトが設定されているか（JDBC `socketTimeout` 等）（`.claude/rules/springer-config.md`）
- [ ] ファイルアップロード/ダウンロード・一時ファイル削除が規約どおりか（`.claude/rules/springer-file-prevention.md`）

### テスト
- [ ] ユニットテストが実装されているか（テストクラス命名 `〇〇Test`・同一パッケージ・層別構成）（`.claude/rules/springer-testing.md`）
- [ ] テストがパスしているか
- [ ] `docs/base-design/テストシナリオ.md` のテストシナリオを網羅しているか
- [ ] E2E が全層横断・外部システムアクセス・実行前後の DB 差分を確認しているか（`.claude/rules/springer-testing.md`）

---

## 承認条件
上記の全チェックがパスした場合のみ承認する。
未解決のコメントがある状態でのマージは禁止。

## レビュー支援スキル
- Reacter コードレビュー: `/reacter-code-review`
- 全レイヤー横断レビュー（Springer）: `/springer-review`
- Springer レビュー修正リコンサイル: `/springer-review-fix`
