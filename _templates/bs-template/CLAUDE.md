# {product}-bs パッケージ（BS アプリ）

> ⚠️ これはひな形です。コピー後、`{product}` を実プロダクト名に置換し、Springer バージョンを宣言してください。

## 継承元
- 上位ルール: `../CLAUDE.md`（上書き不可）

## Springer バージョン
- Springer バージョン: **3** （Spring Boot 4 系）  ← TBD
- バージョン対応表: `../.claude/rules/springer-version-matrix.md`

## このパッケージの責務
- {product} プロダクトの **DB アクセスを伴うバックエンドサービス**
- ビジネスロジックの中心（`@Transactional` をクラスレベルで付与）
- US-MPA / US-API から REST API として呼び出される

## アプリ種別
**BS アプリ**

| レイヤー | アノテーション・特徴 |
|---|---|
| Controller | `@RestController` |
| Service 実装 | `@Service` + `@Transactional`（クラスレベル推奨） |
| Repository 実装 | `@Repository` + MyBatis Mapper 経由で DB アクセス |

## 主要技術スタック
- Java（Spring Boot のバージョンに応じる: SB3=Java 17 / SB4=Java 25）
- Springer 3（Spring Boot 4）
- MyBatis（XML Mapper）
- RDB（プロダクト要件に応じて選定）
- テスト: JUnit 5 / Mockito / AssertJ

## 依存してよいパッケージ
- 同プロダクトの BS パッケージ内のみ（自己完結）
- ※ 他プロダクトの BS / US パッケージに **依存しない**（API 経由のみ）

## 重要ルールサマリ（必読）

> 詳細は `.java` / `pom.xml` / `Mapper.xml` を開いたときに自動ロードされる規約ファイルを参照。

### 絶対禁止事項
- 🚫 Lombok 使用禁止（getter/setter/constructor はすべて手書き）
- 🚫 フィールドインジェクション禁止（`@Autowired` をフィールドに付与しない）
- 🚫 スターインポート禁止（`import java.util.*` 等）
- 🚫 SQL アノテーション禁止（`@Select`, `@Insert`, `@Update`, `@Delete`）→ MyBatis XML に書く
- 🚫 `protected` 修飾子使用禁止
- 🚫 Controller → Repository の直接呼び出し禁止（必ず Controller → Service → Repository）

### DI 規約
- ✅ コンストラクターインジェクションのみ使用
- ✅ Bean クラスのフィールドは `private final`
- ✅ 型はインターフェース型で受ける（`UserRepositoryImpl` ではなく `UserRepository`）

### トランザクション制御
- ✅ `@Transactional` は **BS Service 実装クラスのみ** に付与（クラスレベル推奨）
- 🚫 Controller / Repository / US Service には付与しない

### 例外ハンドリング
- ✅ 業務例外は `ApplicationException` / `ResourceNotFoundException` / `ConflictException` を継承
- ✅ DB 例外・HTTP 例外は **Repository 層のみで catch**（Service / Controller で catch しない）
- 🚫 `Exception`, `RuntimeException`, `Throwable` を catch しない

### ロギング
- ✅ `private static final Logger LOGGER = LoggerFactory.APP.getLogger(Xxx.class);`
- 🚫 `org.slf4j.LoggerFactory` を直接使わない

## 必須参照ルール
（`.java` ファイルを開くと自動ロードされます）
- `.claude/rules/springer-architecture-layer.md`
- `.claude/rules/springer-package-class-naming.md`
- `.claude/rules/springer-di-bean.md`
- `.claude/rules/springer-service-transaction.md`  ← BS の中核ルール
- `.claude/rules/springer-exception.md`

## ベースパッケージ
```
jp.co.example.{product}.bs   ← TBD
```

## ディレクトリ構成
```
src/main/java/<base-package>/
├── controller/
│   ├── request/     ... API リクエストクラス
│   ├── validator/   ... Spring Validator
│   └── view/        ... API レスポンスクラス
├── service/
│   └── impl/        ... @Service + @Transactional
├── repository/
│   ├── impl/        ... @Repository
│   └── mapper/      ... MyBatis Mapper インターフェース
├── model/           ... DTO
└── common/          ... 共通ユーティリティ
src/main/resources/
└── mapper/          ... MyBatis Mapper XML（パッケージと同名構造）
src/test/java/
└── （単体テスト）
```

## 辞書追加読み込み
- `../docs/glossary/`（全社共通辞書）
- `../docs/glossary/（プロダクト辞書）`（プロダクト固有辞書）

## 推奨スキル
- 作業開始時: `/springer-bs`
- スケルトン生成: `/springer-scaffold`
- テスト生成: `/springer-unit-test-gen`, `/unit-test-controller`, `/unit-test-service`, `/unit-test-repository`
- レビュー: `/springer-review`
