# {product}-us-mpa パッケージ（US-MPA アプリ）

> ⚠️ これはひな形です。コピー後、`{product}` を実プロダクト名に置換し、Springer バージョンを宣言してください。

## 継承元
- 上位ルール: `../CLAUDE.md`（上書き不可）

## Springer バージョン
- Springer バージョン: **3** （Spring Boot 4 系）  ← TBD
- バージョン対応表: `../.claude/rules/springer-version-matrix.md`

## このパッケージの責務
- {product} プロダクトの **MPA 形式フロントエンド**（Thymeleaf によるサーバーサイドレンダリング）
- ブラウザから HTTP リクエストを受け、HTML を返す
- 業務処理は `{product}-bs` の REST API を呼び出して委譲する

## アプリ種別
**US-MPA アプリ**

| レイヤー | アノテーション・特徴 |
|---|---|
| Controller | `@Controller`（Thymeleaf テンプレートを返す） |
| Service 実装 | `@Service`（トランザクション制御 **なし**） |
| Repository 実装 | `@Repository`（BS API クライアントとして実装） |

## 主要技術スタック
- Java（Spring Boot のバージョンに応じる: SB3=Java 17 / SB4=Java 25）
- Springer 3（Spring Boot 4）
- Thymeleaf
- BS との通信: RestClient（Spring Boot 4）または RestTemplate
- テスト: JUnit 5 / Mockito / Spring MockMvc

## 依存してよいパッケージ
- 同プロダクトの `{product}-bs`（HTTP 経由のみ・Java の直接 import はしない）
- ※ 他プロダクトの US / BS パッケージに **依存しない**

## 重要ルールサマリ（必読）

> 詳細は `.java` / `pom.xml` を開いたときに自動ロードされる規約ファイルを参照。

### 絶対禁止事項
- 🚫 Lombok 使用禁止（getter/setter/constructor はすべて手書き）
- 🚫 フィールドインジェクション禁止（`@Autowired` をフィールドに付与しない）
- 🚫 スターインポート禁止（`import java.util.*` 等）
- 🚫 `protected` 修飾子使用禁止
- 🚫 Controller → Repository の直接呼び出し禁止
- 🚫 DB への直接アクセス禁止（必ず BS API を介すこと）

### DI 規約
- ✅ コンストラクターインジェクションのみ使用
- ✅ Bean クラスのフィールドは `private final`
- ✅ 型はインターフェース型で受ける

### トランザクション制御
- 🚫 このパッケージでは `@Transactional` を **使用しない**（BS 側でトランザクションが管理される）

### BS API 呼び出し
- ✅ URI は文字列連結禁止 → プレースホルダー（`/items/{id}`）を使う
- ✅ HTTP エラーは Repository 層のみで catch し独自例外にラップする
- 🚫 Service / Controller で HTTP 例外を直接 catch しない

### 例外ハンドリング
- ✅ 業務例外は `ApplicationException` / `ResourceNotFoundException` / `ConflictException` を継承
- ✅ HTTP データアクセス例外（`RestHttpXxx`）は **Repository 層のみで catch**

### MPA 固有
- ✅ Form クラスは `controller/form/` に配置する
- ✅ 空文字は `StringTrimmerEditorControllerAdvice` により自動的に `null` に変換される
- ✅ 二重送信防止は `DoubleSubmitHandlerInterceptor` で制御する

## 必須参照ルール
（`.java` ファイルを開くと自動ロードされます）
- `.claude/rules/springer-architecture-layer.md`
- `.claude/rules/springer-package-class-naming.md`
- `.claude/rules/springer-di-bean.md`
- `.claude/rules/springer-api-client.md`  ← BS 呼び出しの中核ルール
- `.claude/rules/springer-exception.md`

## トランザクション制御
- 🚫 このパッケージでは `@Transactional` を **使用しない**
- BS 側でトランザクションが管理されるため US 側では張らない

## ベースパッケージ
```
jp.co.example.{product}.usmpa   ← TBD
```

## ディレクトリ構成
```
src/main/java/<base-package>/
├── controller/
│   ├── form/        ... HTML フォーム入力クラス
│   └── validator/   ... Spring Validator
├── service/
│   └── impl/        ... @Service（トランザクションなし）
├── repository/
│   └── impl/        ... @Repository（BS API クライアント）
├── model/           ... DTO
├── config/          ... RestClient Bean 登録など
└── common/          ... 共通ユーティリティ
src/main/resources/
├── templates/       ... Thymeleaf テンプレート
└── static/          ... CSS / JS / 画像
src/test/java/
```

## 辞書追加読み込み
- `../docs/glossary/`
- `../docs/glossary/（プロダクト辞書）`

## 推奨スキル
- 作業開始時: `/springer-us-mpa`
- スケルトン生成: `/springer-scaffold`
- テスト生成: `/springer-unit-test-gen`, `/unit-test-controller`, `/unit-test-controller-validation`, `/unit-test-service`, `/unit-test-repository`
- レビュー: `/springer-review`
