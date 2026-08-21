# {product}-batch パッケージ（バッチアプリ）

> ⚠️ これはひな形です。コピー後、`{product}` を実プロダクト名に置換し、Springer バージョンを宣言してください。

## 継承元
- 上位ルール: `../CLAUDE.md`（上書き不可）

## Springer バージョン
- Springer バージョン: **3** （Spring Boot 4 系）  ← TBD
- バージョン対応表: `../.claude/rules/springer-version-matrix.md`

## このパッケージの責務
- {product} プロダクトの **バッチ処理（定期実行・大量データ処理）**
- 起動形態: コマンドライン実行（cron / Job スケジューラ / Argo Workflows 等から起動）
- DB アクセスまたは BS API 呼び出しによるデータ処理

## アプリ種別
**バッチアプリ**

| 区分 | アノテーション・特徴 |
|---|---|
| エントリポイント | `CommandLineRunner` または `ApplicationRunner` を実装したクラス |
| Service 実装 | `@Service` + `@Transactional`（DB 直アクセスの場合） |
| Repository 実装 | `@Repository` + MyBatis Mapper（DB の場合）/ RestClient（BS API の場合） |

## 主要技術スタック
- Java（Spring Boot のバージョンに応じる: SB3=Java 17 / SB4=Java 25）
- Springer 3（Spring Boot 4）
- MyBatis（DB アクセス時）または RestClient（BS API 呼び出し時）
- テスト: JUnit 5 / Mockito / AssertJ

## 依存してよいパッケージ
- 同プロダクトの `{product}-bs/`（HTTP 経由）
- ※ 直接 Java import せず、必ず HTTP API を介すこと
- ※ 他プロダクトのパッケージに **依存しない**

## 重要ルールサマリ（必読）

> 詳細は `.java` / `pom.xml` / `Mapper.xml` を開いたときに自動ロードされる規約ファイルを参照。

### 絶対禁止事項
- 🚫 Lombok 使用禁止（getter/setter/constructor はすべて手書き）
- 🚫 フィールドインジェクション禁止（`@Autowired` をフィールドに付与しない）
- 🚫 スターインポート禁止（`import java.util.*` 等）
- 🚫 SQL アノテーション禁止（`@Select`, `@Insert`, `@Update`, `@Delete`）→ MyBatis XML に書く
- 🚫 `protected` 修飾子使用禁止
- 🚫 大量データをオンメモリに全件読み込み禁止（`Cursor<T>` を使うこと）

### DI 規約
- ✅ コンストラクターインジェクションのみ使用
- ✅ Bean クラスのフィールドは `private final`
- ✅ 型はインターフェース型で受ける

### トランザクション制御
- ✅ `@Transactional` は **DB 直アクセスの Service 実装クラスのみ** に付与
- 🚫 BS API 呼び出し型バッチでは `@Transactional` を使用しない

### 例外ハンドリング
- ✅ 業務例外は `ApplicationException` / `ResourceNotFoundException` / `ConflictException` を継承
- ✅ DB 例外は **Repository 層のみで catch**
- ✅ 異常終了時は `System.exit(1)` で Exit Code を返す

### リトライ
- ✅ べき等な API 呼び出しのみリトライ適用可
- 🚫 INSERT 等べき等でない処理へのリトライ禁止

## 必須参照ルール
（`.java` ファイルを開くと自動ロードされます）
- `.claude/rules/springer-architecture-layer.md`
- `.claude/rules/springer-package-class-naming.md`
- `.claude/rules/springer-di-bean.md`
- `.claude/rules/springer-service-transaction.md`
- `.claude/rules/springer-exception.md`

## バッチ固有の注意点

### 大量データ処理
- ✅ MyBatis の `Cursor<T>` を使って 1 件ずつ処理する（オンメモリ全件読み込み禁止）
- ✅ トランザクション境界を適切に区切る（巨大トランザクションを避ける）

### Cursor の使用例
```java
try (Cursor<Item> cursor = this.itemMapper.selectListCursor(condition)) {
    for (Item item : cursor) {
        // 1件ずつ処理
        this.processOne(item);
    }
} catch (IOException e) {
    throw new SystemException("bs.error.system.FileFailed", e);
}
```

### 異常終了時のリカバリ
- ✅ 中断・再実行を考慮した冪等な設計にする
- ✅ 処理済みレコードを記録し、再実行時はスキップする
- ✅ Exit Code を返してジョブスケジューラに成功/失敗を伝える

### ロギング
- ✅ 開始ログ / 終了ログ / 件数サマリを必ず出力する
- ✅ メッセージキーは `{prefix}.log.info.BatchStart` 等を使用する（`.claude/rules/springer-message-i18n.md`）

## ベースパッケージ
```
jp.co.example.{product}.batch   ← TBD
```

## ディレクトリ構成
```
src/main/java/<base-package>/
├── runner/          ... CommandLineRunner / ApplicationRunner 実装クラス
├── service/
│   └── impl/        ... @Service + @Transactional
├── repository/
│   ├── impl/        ... @Repository
│   └── mapper/      ... MyBatis Mapper（DB アクセス時）
├── model/           ... DTO
├── config/          ... バッチ専用 Configuration
└── common/          ... 共通ユーティリティ
src/main/resources/
├── application.yml  ... バッチアプリ設定ファイル
└── mapper/          ... MyBatis Mapper XML
src/test/java/
└── （単体テスト）
```

## エントリポイント実装例

```java
@Component
public class ItemSummaryBatchRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.APP.getLogger(ItemSummaryBatchRunner.class);

    private final ItemSummaryService itemSummaryService;

    public ItemSummaryBatchRunner(ItemSummaryService itemSummaryService) {
        this.itemSummaryService = itemSummaryService;
    }

    @Override
    public void run(String... args) {
        LOGGER.info(MessageManager.getMessage("batch.log.info.BatchStart", "ItemSummary"));
        try {
            int count = this.itemSummaryService.summarize();
            LOGGER.info(MessageManager.getMessage("batch.log.info.BatchEnd", "ItemSummary", count));
        } catch (ApplicationException e) {
            LOGGER.error(MessageManager.getMessage("batch.log.error.BatchFailed", "ItemSummary"), e);
            System.exit(1);
        }
    }
}
```

## 辞書追加読み込み
- `../docs/glossary/`
- `../docs/glossary/（プロダクト辞書）`

## 推奨スキル
- 作業開始時: `/springer-bs`（バッチも BS と同じトランザクション規約のため）
- スケルトン生成: `/springer-scaffold`（アプリ種別: batch を指定）
- テスト生成: `/springer-unit-test-gen`, `/unit-test-service`, `/unit-test-repository`
- レビュー: `/springer-review`
