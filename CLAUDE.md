# {product} Claude Code ポリシー（Reacter + Springer 版）

このファイルはすべてのスタックに適用される最上位ルールです。
各スタック配下の `CLAUDE.md` はこのファイルに追記する形でのみ記述できます。

---

## このリポジトリの構成

**プロダクト**: `{product}`（プロダクト名・パッケージ名は `docs/requirements/プロダクト情報.md` で確定。未作成の場合は `docs/templates/プロダクト情報.md` をコピーして記入してください）

| スタック | 種別 |
|---|---|
| `frontend/` | Reacter アプリ |
| `bs/` | BS アプリ |
| `us-mpa/` | US-MPA アプリ |
| `us-api/` | US-API アプリ（一般ユーザー向け・管理者向けの両方を含む。機能ID カテゴリ〔`us-intra`/`us-inter`〕は要件内容〔対象ユーザー〕で判定し、物理スタックは分けない） |
| `batch/` | バッチアプリ |

**仕様書**: `docs/`（アーキテクチャ・用語辞書・運用手順）

---

## 1. 技術スタック前提

- **SPA フロント**: **Reacter** フレームワーク（規約: `.claude/rules/reacter-*.md`）
- **バックエンド / サーバーサイドフロント**: **Springer** フレームワーク（Spring Boot 上）。**本プロダクトは全 Springer スタックを Springer 3（Spring Boot 4）で確定・統一する**
  - Springer 2 ↔ Spring Boot 3
  - Springer 3 ↔ Spring Boot 4（本プロダクト採用）

## 2. アプリ種別

| 種別 | 役割 | Controller アノテーション | トランザクション |
|---|---|---|---|
| **Reacter アプリ** | SPA フロント（US-API を経由して BS と連携） | — | — |
| **US-API アプリ** | SPA 向け JSON API フロント | `@RestController` | なし（BS API を呼び出す） |
| **US-MPA アプリ** | Thymeleaf によるサーバーサイド SSR フロント | `@Controller` | なし（BS API を呼び出す） |
| **BS アプリ** | DB アクセスを伴うバックエンド | `@RestController` | あり（Service 実装に `@Transactional`） |

Reacter のワイヤーフレームは `_templates/frontend-template/_received/`、Springer のワイヤーフレームは `_templates/{type}-template/_received/`を受け皿とする（標準チームから受領した実物資材を配置する場所。配置後は `/stack-init` スキルで実スタックへ反映・ビルド確認する。詳細は `_templates/README.md`・`.claude/skills/stack-init/SKILL.md`）。
Reacter の詳細は `.claude/rules/reacter-*.md`、Springer のアーキテクチャ詳細は `.claude/rules/springer-architecture-layer.md` を参照。

## 3. スタック配置ルール

各スタックの種別・役割は「このリポジトリの構成」表（冒頭）を参照。ディレクトリ構成は以下のとおり。

```
frontend/
bs/
us-mpa/
us-api/
batch/
_templates/      新規スタック作成用ひな形（Springer・Reacter 両スタック用。標準チーム受領資材の受け皿 `_received/` を含む）
```

新規スタック追加時の手順は `_templates/README.md` を参照。

## 4. 作業開始時の必須手順（工程モデル）

本プロダクトは **工程0 + 8工程 + リリース前ゲート** の工程モデル（**全8工程（SA・UI・SS-Plan・SS・PG-Plan・PG・PT-Plan・PT）＋手戻り（rework）＋リリース前ゲート（UAT）**）で進める。
> 構成は **3 リポジトリ**（LLM機構テンプレート／案件対応リポ＝上流 docs・計画／スタックリポ＝SS 以降）。`specs/` も `docs/` 同様に **git 管理・push 対象**（SA/UI/SS-Plan は案件リポ、SS 以降は③各スタックリポの `{スタック}/specs/`）。全工程のゲートは **PR** に統一（詳細は `.claude/rules/product-rules.md`）。

**全体像:** 工程0 リポジトリ初期化（手動）→〔案件開始〕→ ①SA 要件定義 → ②UI 基本設計 → ③SS-Plan / ④SS 詳細設計 → ⑤PG-Plan / ⑥PG 実装 → ⑦PT-Plan / ⑧PT 単体テスト →〔リリース前ゲート〕

- **識別子:** 要件ID `R001`（業務要求単位・SA で採番）→ 機能ID `{カテゴリ}-{3桁連番}`（背骨キー・要件ID×スタック×API/画面/ジョブ単位・UI で採番。カテゴリは `bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter` の6種でそれぞれ独立した3桁連番〔例: `bs-001`〕。**1機能ID=1API/1画面/1ジョブ**）／ 案件キー `{プロダクト略称}-{YYYY}-{連番3桁}`（= Milestone）
  - 既存の API ID・ジョブID は廃止せず、機能ID との対応関係を `docs/base-design/機能一覧.md` 等で管理する（識別子は一本化しない）
- **ブランチ:** `feature/{案件キー}-{工程}[-{機能ID}]`（機能ID は `{カテゴリ}-{3桁連番}`。案件統合ブランチ `feature/{案件キー}` から分岐。詳細は `.claude/rules/product-rules.md`）
- **成果物は直接編集:** `docs/`・`{スタック}/docs/`・`{スタック}/src/` へ直接作成。`specs/{案件キー}/`（SS 以降は `{スタック}/specs/{案件キー}/`）は検討メモ・計画・作業ログのみだが、これらも他の成果物と同様に git 管理・push 対象

**手順:**
1. 新規案件は `/case-init` で**案件開始**（案件キー確定・`feature/{案件キー}`・`specs/{案件キー}/meta.md`・`docs/requirements/要件一覧.md`・`docs/base-design/機能一覧.md` 初期化）
2. 各工程は `/issue-init 工程: {工程}` で**着手**（工程issue起票・工程ブランチ・`specs/{案件キー}/{工程}/{issue_id}/` 作成）
3. `docs/glossary/` の辞書・`docs/architecture/stack-dependency.md`（依存・循環チェック）を確認
4. 工程に応じたオーケストレーター/スキルを実行（§6 参照）。**SA・UI は 2 段階方式**（壁打ち→専用 doc-gen スキルで成果物生成）。工程別の詳細は下表を参照

| 工程 | リポ / 単位 | 手順（壁打ち・issue-init → 実行スキル → PR） | 成果物・push 先 | 備考 |
|---|---|---|---|---|
| **SA**（要件定義） | 案件リポ | `issue-to-requirement.md`（壁打ち）→ `/requirement-doc-gen` | PSK30 一覧系文書群 → `docs/requirements/` | 壁打ちで**要件ID（R###）採番**・`ADR-SA-{n}` を決定事項ごとに連番 |
| **UI**（基本設計） | 案件リポ | `issue-to-design.md`（壁打ち・API/DB/ロバストネス分析）→ `/basic-design-gen` | テーブル定義書（bs/batch）・実装対象クラス一覧・テストシナリオ・ロバストネス図・クラス図・画面状態遷移図（frontend）・非機能共通設計.md（アプリ全体の非機能方針）等 → `docs/base-design/` | 壁打ちで `ADR-UI-{n}` を決定事項ごとに連番。`/basic-design-gen` が**要件ID（R###）をスタック×API/画面/ジョブ単位に分解し機能ID（`{カテゴリ}-{3桁連番}`）を採番** |
| **SS-Plan**（詳細設計計画） | 案件リポ・機能ID単位 | `/issue-init 工程: SS-Plan` → `/issue-plan` → `docs-to-pr` → SS sub-issue 先行起票 | `plan.md` → `specs/{案件キー}/detail-design-plan/{issue_id}/`（PR） | `plan.md` に**機能ID単位**の sub-issue 棚卸し（スタック分解は UI 工程で完了済み）・クロススタック依存確定 |
| **SS**（詳細設計） | スタックリポ・機能ID単位 | `/issue-init 工程: SS`（先行起票分の着手）→ `/detailed-design-gen` →（batch: `/batch-design-gen` → `/detailed-design-review-batch`／bs・us-api・us-mpa: `/detailed-design-review-backend`／frontend: `/detailed-design-review-frontend`）→ `docs-to-pr` | **WB(specs)＋外部IF定義書/プログラム仕様書（backend）/コンポーネント仕様書（frontend）/共通設計書（スタック単位の共通設計）** → `{スタック}/docs/detail-design/`。WB/ADR は `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/`（同 PR に含めて push） | テーブル定義書は UI 工程 `docs/base-design/` が正本のため読み込み専用。backend・frontend とも `ADR-SS-{n}` 必須起票 |
| **PG-Plan**（実装計画） | スタックリポ・スタック単位 | `/issue-init 工程: PG-Plan` → `/issue-plan 工程: PG-Plan` → `docs-to-pr` → PG sub-issue（`[PG-bs-001]` 等）先行起票 | `plan.md` → `{スタック}/specs/{案件キー}/implementation-plan/{issue_id}/`（PR） | `plan.md` に実装 sub-issue 棚卸し＋**実装順序・依存**。クロススタック順序は SS-Plan 継承 |
| **PG**（実装） | スタックリポ・機能ID単位 | `/issue-init 工程: PG`（先行起票分の着手）→ `/springer-scaffold`（frontend: `/reacter-code-gen`）→ `/springer-review`（＋`/springer-review-fix`）→ `docs-to-pr` | **コード `src/main` のみ生成**。レビューレポートは `{スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/`（同 PR に含めて push） | プログラム仕様書/メッセージ一覧は SS 工程 `{スタック}/docs/detail-design/` に生成済みのため docs 生成なし |
| **PT-Plan**（単体テスト計画） | スタックリポ・スタック単位 | `/issue-init 工程: PT-Plan` → `/issue-plan 工程: PT-Plan` → `docs-to-pr` → PT sub-issue（`[PT-bs-001]` 等）先行起票 | `plan.md` → `{スタック}/specs/{案件キー}/unit-test-plan/{issue_id}/`（PR） | `plan.md` に PT sub-issue 棚卸し＝テスト対象・観点 |
| **PT**（単体テスト） | スタックリポ・機能ID単位 | `/issue-init 工程: PT`（先行起票分の着手）→ `/springer-unit-test-gen`（frontend: `/reacter-unit-test-gen`）＋ `/blackbox-test-gen` → `/consistency-check`（三者整合・PT 工程内）→ `mvn/npm test` → 人間確認 → `docs-to-pr` | Springer系: テストコード `src/test` ＋テスト仕様書/テスト計画書/機能要件対比表 → `{スタック}/docs/unit-test/`。frontend: Vitest テストコード＋テスト docs → `frontend/docs/unit-test/` | 単体テストの責務に限定（E2E・妥当性確認実施票は本工程の範囲外） |
| **〔リリース前ゲート〕** | 工程外・人間のアクションのみ | 全スタック PT PR マージ後、人間が妥当性確認実施票を確認・記入し、結合テスト・最終判断を実施 → **リリース判定ゲート通過後に `develop→main`**（人間が実施・案件リポ＋各スタックリポ）→ 案件（Milestone）クローズ | — | — |

5. 成果物を `docs/` へ直接作成 → `docs-to-pr` で**工程ゲート PR**（ベース = `feature/{案件キー}`）

## 5. フレームワーク規約参照先（必読）

### Reacter 規約

| 規約 | ファイル |
|---|---|
| ルール索引・制約レベル凡例 | `.claude/rules/rule-overview.md` |
| アプリ設定（package.json / Vite / 環境変数 / テンプレート） | `.claude/rules/reacter-app-config.md` |
| コーディング規約（ESLint/Prettier・TSDoc・実装全般） | `.claude/rules/reacter-coding-standards.md` |
| フォルダー構成・命名規約 | `.claude/rules/reacter-folder-structure.md` |
| ルーティング | `.claude/rules/reacter-routing.md` |
| エラーハンドリング（Error Boundary） | `.claude/rules/reacter-error-handling.md` |
| セキュリティ（XSS / CSP） | `.claude/rules/reacter-security.md` |
| パフォーマンス最適化 | `.claude/rules/reacter-performance.md` |
| CSS Modules | `.claude/rules/reacter-css.md` |
| ブラウザキャッシュ制御 | `.claude/rules/reacter-cache-control.md` |
| 状態管理（React Context / useContext） | `.claude/rules/reacter-state-management.md` |
| フォーム・バリデーション（React Hook Form + Zod） | `.claude/rules/reacter-form-validation.md` |
| サーバ通信（axios） | `.claude/rules/reacter-server-communication.md` |
| ライブラリ部品（タブ / CSV / 帳票 / メッセージ管理等） | `.claude/rules/reacter-libraries.md` |
| ユニットテスト（Vitest） | `.claude/rules/reacter-testing-vitest.md` |
| E2E テスト（Playwright） | `.claude/rules/reacter-testing-playwright.md` |

### Springer 規約

| 規約 | ファイル |
|---|---|
| レイヤー定義 / US・BS / DTO 受け渡し | `.claude/rules/springer-architecture-layer.md` |
| ライブラリ制約 / Lombok 禁止 / 依存管理 | `.claude/rules/springer-tech-stack-dependency.md` |
| パッケージ構成 / クラス定義 / 命名規約 | `.claude/rules/springer-package-class-naming.md` |
| コンストラクタ注入 / final / static 制約 | `.claude/rules/springer-di-bean.md` |
| Controller / Thymeleaf / セッション | `.claude/rules/springer-controller.md` |
| 入力チェック / Form・Request / Validator | `.claude/rules/springer-validation-input.md` |
| Service / @Transactional の範囲と禁則 | `.claude/rules/springer-service-transaction.md` |
| Repository / Mapper XML / 排他制御 | `.claude/rules/springer-repository-mybatis.md` |
| RestClient・RestTemplate / SOAP・GraphQL | `.claude/rules/springer-api-client.md` |
| 例外ハンドリング / 例外クラス体系 | `.claude/rules/springer-exception.md` |
| ログ / IF ログ / マスキング | `.claude/rules/springer-logging-masking.md` |
| メッセージ ID / メッセージ管理 / 国際化 | `.claude/rules/springer-message-i18n.md` |
| 認証・認可 / @PreAuthorize / API キー認証 | `.claude/rules/springer-security-auth.md` |
| Java コーディングスタイル（Checkstyle 等） | `.claude/rules/springer-coding-style.md` |
| ファイル / 一時ファイル削除 / 二重送信防止 | `.claude/rules/springer-file-prevention.md` |
| OWASP セキュリティチェックリスト要点 | `.claude/rules/springer-security-checklist.md` |
| リトライ / トレーシング / 閉塞・流量制御 | `.claude/rules/springer-resilience.md` |
| バッチ設計 | `.claude/rules/springer-batch.md` |
| 設定ファイル / ログ設定 / タイムアウト | `.claude/rules/springer-config.md` |
| ユニットテスト / E2E テスト（Playwright） | `.claude/rules/springer-testing.md` |
| OpenAPI / Swagger 仕様書 | `.claude/rules/springer-openapi.md` |
| API 方式（REST / GraphQL） | `.claude/rules/springer-api-style.md` |
| Azure Blob Storage / Key Vault | `.claude/rules/springer-cloud-storage-secret.md` |
| Springer / Spring Boot / Java バージョン対応表 | `.claude/rules/springer-version-matrix.md` |

## 6. アプリ種別ごとの作業スキル

### Reacter（フロントエンド）

| 用途 | 使用スキル |
|---|---|
| Reacter アプリのコード生成 | `/reacter-code-gen` |
| Reacter アプリのコードレビュー | `/reacter-code-review` |
| ユニットテスト生成（Vitest・工程8 PT） | `/reacter-unit-test-gen` |

### Springer（バックエンド・サーバーサイド）

| 用途 | 使用スキル |
|---|---|
| BS アプリの実装支援 | `/springer-bs` |
| US-MPA アプリの実装支援 | `/springer-us-mpa` |
| US-API アプリの実装支援 | `/springer-us-api` |
| 全レイヤー横断レビュー（観点台帳 MUST/SHOULD/MAY・テーマ分割・2パス検証・summary.md + 観点ID別ファイル出力） | `/springer-review` |
| レビュー修正リコンサイル（MUST→SHOULD→MAY 順消し込み・未対応0件ゲート・差分再レビュー） | `/springer-review-fix` |
| 新機能全レイヤー実装一括生成（スケルトン生成も可） | `/springer-scaffold` |
| 全レイヤーユニットテスト生成 | `/springer-unit-test-gen` |
| Controller ユニットテスト生成 | `/unit-test-controller` |
| Controller バリデーションテスト生成 | `/unit-test-controller-validation` |
| Service ユニットテスト生成 | `/unit-test-service` |
| Repository ユニットテスト生成 | `/unit-test-repository` |
| Controller レイヤー単体レビュー | `/controller-reviewer` |
| Service レイヤー単体レビュー | `/service-reviewer` |
| Repository + Mapper XML 単体レビュー | `/repository-reviewer` |
| Controller テストクラス単体生成 | `/controller-test-gen` |
| Service テストクラス単体生成 | `/service-test-gen` |
| Repository テストクラス・初期データ SQL 単体生成 | `/repository-test-gen` |

### 共通

| 用途 | 使用スキル |
|---|---|
| 案件(Milestone)開始（案件キー確定・案件統合ブランチ・機能一覧初期化） | `/case-init` |
| 新規スタック作成（受領資材の受入・実反映・ビルド確認） | `/stack-init` |
| 工程ごとの issue 着手（工程issue起票・工程ブランチ・作業ディレクトリ生成） | `/issue-init` |
| 要件定義書群の生成（SA・一覧系文書群を `docs/requirements/` へ。`issue-to-requirement` の ADR 後） | `/requirement-doc-gen` |
| 基本設計書群の生成（UI・成果物を `docs/base-design/` へ。`issue-to-design` の ADR 後） | `/basic-design-gen` |
| 基本設計書群のAIレビュー（工程2 UI・要件定義との整合性・用語表記統一） | `/basic-design-review` |
| 計画工程（**SS-Plan / PG-Plan / PT-Plan**・工程パラメータ・`plan.md` に スタック×機能ID の sub-issue 棚卸し＋PG-Plan は実装順序/PT-Plan はテスト観点・PR） | `/issue-plan` |
| 作業ログへ手動メモ追記 | `/work-log` |
| 実装コードレビュー | `/code-review` |
| セキュリティ脆弱性検査 | `/security-check` |
| パッケージ間依存チェック | `/dependency-check` |
| 詳細設計生成（工程4 SS・WB＋外部IF定義書/プログラム仕様書を `{スタック}/docs/detail-design/` へ直接生成。テーブル定義書は UI 工程が正本） | `/detailed-design-gen` |
| バッチ設計生成（工程4 SS・batch・ジョブネット/ジョブフロー → `batch/docs/detail-design/`） | `/batch-design-gen` |
| 詳細設計 AIレビュー（工程4 SS・Springer） | `/detailed-design-review-backend` |
| 詳細設計 AIレビュー（工程4 SS・batch） | `/detailed-design-review-batch` |
| 詳細設計 AIレビュー（工程4 SS・frontend） | `/detailed-design-review-frontend` |
| AI整合性チェック・設計書/コード/テストの三者検証（工程8 PT） | `/consistency-check` |
| ブラックボックステスト生成・Controller APIテスト（工程8 PT） | `/blackbox-test-gen` |
| プロジェクト構成レビュー（構成変更後に実施） | `/config-review` |
| 開発プロセス I-P-O レビュー（テンプレート追加・スキル変更後に実施） | `/process-review` |
| 手戻り＝大規模手戻り（横断機構・横方向処方: 横展開調査・`ADR-rework-{NNN}`・`-sp-{m}` 再スタート誘導／前段の縦診断は `rework-trace` エージェント・`specs/{案件キー}/rework/{m}/`） | `/rework-guide` |
| 案件の LLM 工数を工程別・手戻り別に集計しサマリレポートを生成 | `/usage-report` |

レイヤー特化スキル（`/controller-reviewer`・`/service-reviewer`・`/repository-reviewer`・`/controller-test-gen`・`/service-test-gen`・`/repository-test-gen`）はサブエージェント（`.claude/agents/` 配下）としても呼び出せます。

## 7. 自動規約チェック・作業ログ（Write/Edit/Bash フック）

`.claude/settings.json` で設定された **PreToolUse / PostToolUse フック** が、Write/Edit・Bash ツール呼び出しの前後に以下を自動実行します。

**PreToolUse（実行前に実行 — 違反があれば書き込み・コマンド実行をブロック）**

| スクリプト | 対象 | 動作 |
|---|---|---|
| `.claude/hooks/check_java.py` | `*.java`（`src/test/` 除外） | Lombok・スターインポート・フィールドインジェクション・`protected`・SQL アノテーション・TODO コメント・広スコープ catch・旧一時ファイル API（`File.createTempFile`）・SLF4J `LoggerFactory` 直接利用・`System.out/err`/`printStackTrace`・ログの文字列連結・ラッパークラスの `new`・`WebSecurityConfigurerAdapter` 継承・`BCryptPasswordEncoder()` 強度未指定・`@SchemaMapping` を検出し書き込みをブロック |
| `.claude/hooks/check_mapper_xml.py` | `*Mapper.xml` / `mapper/` 配下の XML | `${}` 使用（SQL インジェクション注意）を検出し書き込みをブロック |
| `.claude/hooks/check_pom_xml.py` | `pom.xml` | Lombok 依存関係を検出し書き込みをブロック |
| `.claude/hooks/check_typescript.py` | `*.ts` / `*.tsx`（テストファイル除外）・`package.json` | `React.FC`・`any` 型・`dangerouslySetInnerHTML`・`innerHTML` 直接代入・`console.log`・`debugger`・バージョンワイルドカード等を検出し書き込みをブロック |
| `.claude/hooks/check_backlog_gate.py` | `Bash`（`gh pr create`／`.claude/scripts/gh/pr-open.sh` を含むコマンドのみ） | ブランチから工程issueスコープを解決し、未解決のバックログ項目（discussion-log.md・adr/ の書式1）が残っていれば列挙してコマンド実行をブロック（`docs-to-pr` Step 0 の見落とし・迂回に対する補助的な安全網） |
| `.claude/hooks/check_review_gate.py` | `Bash`（`gh pr create`／`.claude/scripts/gh/pr-open.sh` を含むコマンドのみ） | SS/PG/PT 工程の機能ID別ブランチについて、レビュー実施時に無条件で生成されるはずの成果物（review-report.md・springer-review-report.md/reacter-code-review-report.md・consistency-report.md 等）の存在を確認し、無ければコマンド実行をブロック（詳細は `.claude/rules/cross-process-consistency.md`） |

**PostToolUse（書き込み後に実行）**

| スクリプト | 対象 | 動作 |
|---|---|---|
| `.claude/hooks/log_work.py` | すべての Write/Edit | 作業ログを `docs/changelog/work-logs/{YYYY-MM-DD}.md` に自動追記。ブランチが `feature/{案件キー}-{工程}[-{機能ID}]`（機能ID は `{カテゴリ}-{3桁連番}`）形式の場合は工程issueスコープ（`specs/{案件キー}/{工程}/{issue_id}/work-log.md` 等）にも併記 |

**Stop / SubagentStop（応答終了後に実行 — 工数計上）**

| スクリプト | 契機 | 動作 |
|---|---|---|
| `.claude/hooks/log_llm_usage.py` | Stop（メイン応答終了）/ SubagentStop（サブエージェント終了） | 1 やり取り分の **使用モデル・トークン数（input/output/cache）・作業時間（壁時計）** を `specs/{案件キー}/{工程}/{issue_id}/llm-usage.jsonl` に 1 行追記する（`source` = `main`/`subagent`）。トランスクリプトから集計し、issue はブランチから解決（`log_work.py` と同一ロジック）。**合算・集計は後段**（`exchange_uuid` で重複排除）。スキーマ依存のため取得不能時はスキップ |

工数計上ログ（`llm-usage.jsonl`）の仕様は `docs/changelog/work-logs/llm-usage-guide.md` を参照。

Python 3.8 以上が PATH に必要です。

作業ログの仕様は `docs/changelog/work-logs/README.md` を参照。

## 8. テスト見本コード `_hint/`

ユニットテスト生成コマンドはプロダクトルート直下の `_hint/` を参照して、生成テストの体裁を揃えます。
詳細は `_hint/README.md` を参照。

## 9. プロダクト共通ルール参照先

- プロダクト固有ルール: `.claude/rules/product-rules.md`
- GitHub（gh CLI）運用: `.claude/rules/github-ops.md`（Milestone=案件キー・issue/ブランチ・チケット引き継ぎ・ヘルパー `.claude/scripts/gh/`）
- セキュリティ: `.claude/rules/security-policy.md`
- AI 利用ガイドライン: `.claude/rules/ai-usage-policy.md`
- レビュー基準: `.claude/rules/review-standards.md`
- 工程間整合性チェック・手戻り連携: `.claude/rules/cross-process-consistency.md`
- 将来削除予定の暫定マーカー一覧: `docs/changelog/PENDING_CLEANUP.md`

## 10. 禁止事項

- スタック間の循環依存を作らない
- US アプリから DB に直接アクセスしない（必ず BS API を介する）
- 上位 CLAUDE.md のルールと矛盾する記述を下位 CLAUDE.md に書かない
- `docs/` 配下の既存ファイルを無断で変更しない
- 変更内容は実行前に差分を提示し承認を得てから適用する
