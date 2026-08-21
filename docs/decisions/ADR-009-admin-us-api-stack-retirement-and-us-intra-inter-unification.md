# ADR-009: `admin-us-api` スタックの廃止と `us-intra`/`us-inter` 判定方式の統一

## 目的（この対応で達成したいこと）

- **Why（なぜ対応するか）**: `admin-us-api` は「管理者向け US-API」を一般ユーザー向け `us-api` と分離した独立の物理スタックとして扱ってきたが、両者はController/Service/Repositoryの構成・技術要素（`@RestController`・Springer + Spring Boot）が完全に同一であり、実質的な違いは「ビジネス上、管理者向け機能か一般ユーザー向け機能か」という利用者区分のみである。物理スタックを分離する構造的な必要性がないにもかかわらず、リポジトリ・ディレクトリ・機能IDカテゴリ判定ロジック（`.claude/orchestrators/issue-to-design.md` の旧B-6対応表）の3か所で「admin-us-apiという専用スタックが存在する」という前提が固定化されており、命名の柔軟性を損なっていた。一方frontendは元々物理的な管理者向けスタックを持たず、要件内容（対象ユーザー）に基づいて`front-intra`/`front-inter`を判定する方式（旧B-7）が既に確立していた。US層にも同じ考え方を適用できる。
- **What（何を対応するか）**: `admin-us-api` を独立スタックとして廃止し、`us-api` に統合する（管理者向け・一般ユーザー向けの両方を`us-api`スタック内でロール・権限ベースの認証・認可により分離する）。機能IDカテゴリ `us-intra`/`us-inter` の判定方式を、物理スタックへの機械的マッピング（旧B-6）から、要件内容（対象ユーザー）に基づく判定（旧B-7と同一方式）に変更する。`bs`/`batch`カテゴリの判定方式（物理スタックへの機械的マッピング）は変更しない。
- **How（どう対応するか）**: `.claude/orchestrators/issue-to-design.md`・`.claude/skills/basic-design-gen/SKILL.md` のカテゴリ判定ロジックを書き換え、リポジトリ全体（スキル・オーケストレーター・rules・テンプレート・スクリプト計28ファイル）から `admin-us-api` への参照を削除し、`_templates/admin-us-api-template/` を削除する。

## メタ情報

| 項目 | 値 |
|---|---|
| 日付 | 2026-07-13 |
| ステータス | ✅ 実装済み（2026-07-13） |
| 対象 Issue | N/A（案件に属さない機構メンテナンス） |
| 起票 Step | 工程外（ユーザーからの直接指摘） |
| 関連 ADR | 本ADRが定める新方式は、既存の frontend `front-intra`/`front-inter` 判定方式（要件内容に基づく判定）と同一の考え方を US 層に適用したものである |

## 背景

### ①状況

本フレームワークは当初、US層のバックエンドを「一般ユーザー向け（`us-api`/`us-mpa`）」と「管理者向け（`admin-us-api`）」の物理スタック分離で表現し、機能IDカテゴリ `us-intra`（管理者向け）/`us-inter`（一般ユーザー向け）を、この物理スタックへの機械的マッピングで判定していた（`.claude/orchestrators/issue-to-design.md` 旧Step2）。

一方、frontendには元々「管理者向けフロントエンド」という別の物理スタックは存在せず（`frontend/` の1つのみ）、`front-intra`/`front-inter` は要件定義の「対象ユーザー」欄の記述内容に基づいて機能ID（画面）単位で判定する方式を採用していた（同ファイル旧Step6.5）。

### ②課題・問題

ユーザーから、`admin-us-api` という命名・スタック分離自体の必要性について指摘があった。管理者向けAPIと一般ユーザー向けAPIは、実装パターン（`@RestController`・Springer構成）が完全に同一であり、両者を分けているのはビジネス上の権限区分のみである。この場合、frontendが既に採用している「物理スタックは分けず、要件内容で利用者区分を判定する」方式を、US層にも一貫して適用する方が構造的に自然である。

### ③制約

- `bs`・`batch`カテゴリの判定方式（物理スタックへの機械的マッピング）は変更しない（これらのカテゴリには利用者区分という概念自体が存在しないため、判定方式を変える理由がない）。
- 6つの機能IDカテゴリ（`bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter`）という分類体系自体は変更しない（カテゴリの意味・連番管理方法は不変）。
- `us-mpa`スタックも`us-api`と同様、物理スタックの選択とintra/inter判定を独立した軸として扱う（`us-mpa`で実装された管理者向け機能があれば`us-intra`と判定してよい）。

## 決定事項

1. **スタック構成**: `admin-us-api` を独立スタックとして廃止する。プロダクトのスタック構成は `frontend`/`bs`/`us-mpa`/`us-api`/`batch` の5スタックとする（Springer系4スタック＋Reacter1スタック）。管理者向けAPIは `us-api`（または`us-mpa`）スタック内に実装し、認証・認可はロール・権限ベースで同一スタック内で分ける。
2. **機能IDカテゴリ判定方式の統一**: `us-intra`/`us-inter` の判定は、物理スタック（`us-api`/`us-mpa`のいずれで実装するか）とは独立した軸として扱う。対象APIが一般ユーザー向け（`us-inter`）か管理者相当（`us-intra`）かは、`front-intra`/`front-inter`と同一の方法（`要件定義_{要件ID}.md`「対象ユーザー」欄・`業務概要.md`「利用者・権限」表の記述）に基づき、機能ID（API）単位で判定する。
3. **`bs`/`batch`カテゴリ**: 判定方式は変更しない（物理スタックへの機械的マッピングのまま）。

## 理由

- 物理スタックの分離は「実装パターンの選択（SPA向けJSON APIかMPAサーバーサイドSSRか）」という軸であり、「利用者区分（一般ユーザーか管理者か）」という軸とは本来独立している。両者を同一視して`admin-us-api`という専用スタックを設けていたことが、命名・構成の柔軟性を不必要に損なっていた。
- frontendは既に「物理スタックは1つのみ・利用者区分は要件内容で判定」という方式で運用されており実績がある。US層に同じ方式を適用することで、backend/frontend間の判定ロジックが対称になり、判断基準が一本化される。

## 影響

- **削除**: `_templates/admin-us-api-template/`（スキャフォールド一式）。
- **変更**: `.claude/orchestrators/issue-to-design.md`・`.claude/orchestrators/issue-to-requirement.md`・`.claude/skills/basic-design-gen/SKILL.md`・`.claude/skills/detailed-design-gen/SKILL.md`・`.claude/skills/case-init/SKILL.md`・`.claude/skills/stack-init/SKILL.md`・`.claude/skills/springer-scaffold/SKILL.md`・`.claude/skills/springer-unit-test-gen/SKILL.md`・`.claude/skills/consistency-check/SKILL.md`・`.claude/skills/usage-report/SKILL.md`・`.claude/skills/config-review/SKILL.md`・`.claude/rules/product-rules.md`・`.claude/rules/springer-version-matrix.md`・`.claude/repositories.local.md.example`・`.claude/scripts/usage_report.py`・`.claude/scripts/gh/pr-open.sh`・`.claude/scripts/gh/case-bootstrap.sh`・`.gitignore`・`CLAUDE.md`・`docs/architecture/overview.md`・`docs/architecture/stack-dependency.md`・`docs/design/README.md`・`docs/test/README.md`・`docs/templates/README.md`・`docs/templates/40_基本設計/機能一覧.md`・`docs/templates/50_詳細設計/メッセージ一覧.md`・`specs/templates/plan.md`・`specs/templates/issue-meta.md`。
- **既存案件への影響**: 本ADRはテンプレート機構自体の変更である。過去に本テンプレートを使って`admin-us-api`スタックを実際に構築した案件が既にある場合、当該案件のスタック構成・機能IDカテゴリ割り当てには影響しない（既存の`us-intra`番号は既存の機能一覧.mdにそのまま残る。本ADRは今後の新規判定方法のみを変更する）。

## 未確定事項（バックログ）

- [ ] `us-mpa`で管理者向け機能（`us-intra`）を実装する具体例が実際に発生した際、既存のSecurityConfig・ロールベース認可の実装パターンに支障がないかは、実際のケースで確認する。

## セルフレビュー（8観点チェック）

1. **中核具体性**: ✅解消。判定ロジックの変更箇所（`issue-to-design.md`のStep2/Step6.5・`basic-design-gen/SKILL.md`の3-1節）を具体的に特定し、新旧の判定方式を明記した。
2. **網羅性（実態照合）**: ✅解消。リポジトリ全体を`grep`で走査し、`admin-us-api`への参照43件（アクティブ28件＋テンプレート削除3件＋歴史的記録・非一次資料12件）を確認し、アクティブな参照を全て修正した。
3. **既存分岐の保持**: ✅解消。`bs`/`batch`カテゴリの判定方式（機械的マッピング）は変更していない。既存の連番共通化ロジック（`us-intra`/`us-inter`が単一の`bs-XXX`への単純接続の場合の番号共通化）も変更していない。
4. **決定権者の明示**: ✅解消。物理スタック統合という構造的な変更のため、ユーザーへ選択肢を提示し（B-7型統一／1カテゴリへの統合／名称のみ変更の3案）、ユーザーが「B-7型統一」を選択したことを確認済み。
5. **逆依存の確認**: ✅解消。`docs/decisions/ADR-005`・`ADR-002`・`ADR-001`（歴史的ADR）・`docs/changelog/work-logs/`（自動生成の作業ログ）・`DEVELOPER_GUIDE.md`（非一次資料）は、既存の「履歴は書き換えない」原則・「非一次資料は本対応の対象外」原則に従い意図的に変更対象から除外した。
6. **境界条件**: ✅解消。「既存案件で既に`admin-us-api`を構築済みの場合」の境界条件を「影響」節に明記し、既存の機能ID割り当てには影響しないことを確認した。
7. **義務レベルの明示**: ✅解消。対応計画は実施済みの事実として記載した。
8. **暗黙前提の再言語化**: ✅解消。「管理者向けAPIは専用スタックで分離すべき」という暗黙の前提を検証し、frontendの既存実績（要件内容ベース判定）と対称にする方が構造的に自然であるという根拠を明記した。

**未解決（要確認）**: なし。
