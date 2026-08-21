# CHANGELOG（全社ルール・構成変更履歴）

## 形式
```
## [日付] 担当者
### 変更内容
- 変更の詳細
```

---

## [2026-06-19] MIGRATION_GUIDE.md を実体作成（未作成参照の解消）
### 変更内容
- ルートに `MIGRATION_GUIDE.md` を**新規作成**（2026-05-19 の CHANGELOG では作成済みと記録されていたが実体が無く、`/config-review`(2026-06-11) で「未作成（要対応）」と検出されていたリンク切れを解消）。
- 内容を**工程モデル（工程0 + 8工程）・案件キー・F001 採番・直接編集モデル**に整合させて起こした:
  - 新規プロダクト取り込みの 3 パターン（A: ゼロから / B: 他 AI ツール / C: 人手 Excel・Word）と判別フローチャート
  - 共通事前準備（§2）・移植後の検証手順（§6）・トラブルシューティング 8 件
  - 新規スタック追加の詳細は `DEVELOPER_GUIDE.md` §4 へ委譲（重複回避）
- `DEVELOPER_GUIDE.md`・`CLAUDE.md`・`config-review` からの既存導線（リンク）が有効化された。

---

## [2026-05-21] 標準チーム — skills ディレクトリ再編・agents ディレクトリ新設
### 変更内容
- `.claude/skills/` をフラット構成から工程別サブフォルダ構成に変更
  - `issue/issue-init.md` / `implement/{springer-bs,us-api,us-mpa,scaffold}.md`
  - `test/{unit-test-all,controller,controller-validation,service,repository}.md`
  - `review/springer-review.md` / `utility/work-log.md`
- `.claude/agents/` を新設（18 ファイル）
  - スキル対応エージェント 12 種（スキルをサブエージェントとして実行するラッパー）
  - レイヤー特化エージェント 6 種（`springer-review` / `unit-test-all` が並列起動する専用エージェント）
- スキルパス変更に伴い `.claude/commands/*.md`（12ファイル）を更新
- 詳細は `skills-change.log`（モノレポルート）を参照

---

## [2026-05-19] 標準チーム — 作業ログの月次自動アーカイブ
### 変更内容
- `.claude/hooks/log_work.py` に **月次自動アーカイブ処理** を追加
  - Write/Edit のたびに前月以前のデイリーログを `docs/changelog/work-logs/archive/{YYYY-MM}/` に移動
  - 当月分は `work-logs/` 直下に残ったまま日々追記
  - 冪等処理（重複・既存ファイルはスキップ）
  - silent failure（本処理を妨げない）
- `docs/changelog/work-logs/README.md` を更新（自動アーカイブの仕様・参照方法・手動補完手順を追記）
- `docs/changelog/PENDING_CLEANUP.md` のローテーション方針を月次自動に変更、二次整理の方針も整理

---

## [2026-05-19] 標準チーム — 作業ログ自動記録の追加
### 新規追加
- `.claude/hooks/log_work.py` を新規作成（PostToolUse フック）
  - Write / Edit 実行のたびに以下の 2 か所へ自動追記:
    - `docs/changelog/work-logs/{YYYY-MM-DD}.md`（日別の全作業）
    - `specs/issues/{product}/{issue}/work-log.md`（ブランチが `feature/{product}/issue-XXX-...` の場合のみ）
  - 記録情報: 時刻、実装者名（`git config user.name`）、ブランチ、対象ファイル
  - ログ自身の更新は除外（無限ループ防止）
  - silent failure（本処理を妨げない）
- `.claude/settings.json` の PostToolUse hooks に `log_work.py` を登録
- `.claude/skills/work-log.md` を新規作成（手動メモ追記スキル `/work-log`）
- `docs/changelog/work-logs/README.md` を新規作成（仕様・運用手順）
- CLAUDE.md §7 にフック一覧追記・スキル一覧に `/work-log` 追記
- DEVELOPER_GUIDE.md §6.10 に作業ログ確認手順を追記、§8.2 早見表に `/work-log` 追記
- PENDING_CLEANUP.md にローテーション対象として作業ログを追記

---

## [2026-05-19] 標準チーム — 初期移植ガイド追加
### 新規ドキュメント
- `MIGRATION_GUIDE.md` を monorepo ルートに新規作成
  - パターン A: 新規ゼロから Claude Code で生成
  - パターン B: 他 AI ツール（GitHub Copilot / Devin 等）から移植
  - パターン C: 人手作成の Excel / Word 設計書から移植
- 各パターンの判別フローチャート、共通事前準備（§2）、移植後の検証手順（§6）を整備
- トラブルシューティング 8 件を収録
- CLAUDE.md・DEVELOPER_GUIDE.md から `MIGRATION_GUIDE.md` への導線を追加

---

## [2026-05-19] 標準チーム — issue 初期化スキル追加
### 新規スキル
- `.claude/skills/issue-init.md` を新規作成（外部チケットから `specs/issues/` 配下に作業ディレクトリと初期ファイルを生成）
  - 入力: チケット URL（WebFetch で取得）または直接貼り付け
  - 出力: `meta.md` / `requirement.md` / `design.md` / `test-scenario.md` / `discussion-log.md`
  - `meta.md` に基本情報・チケット連携情報を自動記入
  - `discussion-log.md` に元チケット内容を記録
- `specs/templates/meta.md` を Springer 構造（`{product}-bs` / `-us-mpa` / `-us-api` / `-batch`）に更新、チケット連携セクション追加
- 旧 `.claude/hooks/on-issue-open.md` を `/issue-init` スキルへのリダイレクト記述に更新
- CLAUDE.md・DEVELOPER_GUIDE.md のスキル一覧と Step 0 説明を更新

---

## [2026-05-19] 標準チーム — skills 表現への統一・暫定マーカーの整理
### 表現の変更
- `.claude/commands/` を `.claude/skills/` に統合し、各スキルファイルに `name`/`description` の YAML フロントマターを付与
- 全ドキュメント・テンプレートで「スラッシュコマンド」→「スキル」、「commands」→「skills」へ統一
- 「（Springer モノレポ版）」を「（モノレポ版）」へ統一（マーカーを単一形式に正規化）
- `docs/changelog/PENDING_CLEANUP.md` を新規作成（将来削除予定の暫定マーカーを一覧化・一括削除コマンドを記載）

---

## [2026-05-18] 標準チーム — 完全版（P0〜P2 全件実施）
### Springer 規約の完全移植
- スキル 10 本を `.claude/skills/` 配下に追加（YAML フロントマター付き・モノレポ文脈の前文を各スキルに追記）
  - `/springer-bs`, `/springer-us-mpa`, `/springer-us-api`, `/springer-review`, `/springer-scaffold`
  - `/unit-test-controller`, `/unit-test-controller-validation`, `/unit-test-service`, `/unit-test-repository`, `/unit-test-all`
- 自動規約チェックフック 3 本＋ `settings.json` を `.claude/{hooks,settings.json}` に配置
  - `check_java.py`（Lombok / スターインポート / フィールドインジェクション / `protected` / SQL アノテーション / TODO / 広スコープ catch を検出）
  - `check_mapper_xml.py`（`${}` 検出）
  - `check_pom_xml.py`（Lombok 依存検出）
- テスト見本コード `_hint/` を monorepo ルート直下に配置（4 ファイル + README）
- 新規ルールファイル追加
  - `.claude/rules/springer-validator.md`（標準 + Springer Validator 17 個）
  - `.claude/rules/springer-message-id.md`（メッセージ ID 命名規約）
- 既存ルールに具体クラスを補強
  - `.claude/rules/springer-exception.md`: 業務例外 4 クラス・DB 例外 5 クラス・HTTP 例外 6 クラスの実名追記
  - `.claude/rules/springer-masking.md`: OddMasker・Masker 独自実装・YAML 設定を追記
- `packages/_templates/batch-template/` を追加（バッチアプリひな形）
- 全 4 テンプレ（bs / us-mpa / us-api / batch）に最小限の `Application.java` ＋ `application.yml` ＋ src/README を配置
- `specs/templates/design/` ディレクトリで設計書テンプレを Controller / Service / Repository の 3 ファイルに分割
- `docs/operation/{environment_setup.md, deployment.md}` を新規追加
- `DEVELOPER_GUIDE.md` を monorepo ルートに新規作成（初学者向け実務マニュアル）
- ルート `CLAUDE.md` を更新（skills / hooks / `_hint/` / batch / validator / message-id の参照を追加）

---

## [2026-05-18] 標準チーム — 初版移行
### Springer フレームワークを monorepo 構成へ移植
- Springer 規約（§1〜§12）を `.claude/rules/springer-*.md` × 13 ファイルに分割配置
- ルート `CLAUDE.md` を Springer 全社ポリシー＋規約ハブにリライト
- `.claude/rules/monorepo-rules.md` を US/BS パッケージ依存ルールに更新
- `docs/architecture/` を Springer 構成にリライト（`overview` / `package-dependency` / `us-bs-relationship` 新設）
- `docs/glossary/system-terms.md` を Java/Springer 用語へ更新
- `docs/onboarding/claude-code-guide.md` を Springer 版オンボーディングに更新
- `packages/_templates/{bs,us-mpa,us-api}-template/` 3 種ひな形を作成（`CLAUDE.md` + `BUILD_TBD.md`）
- `.claude/rules/reacter-placeholder.md` を新設（Reacter 規約の連携待ち枠）
- 旧汎用ルール（`coding-standards.md` / `naming-conventions.md`）を Springer 規約への参照ノートに置換
- 旧 Next.js/FastAPI サンプル（`packages/product-a-*` / `shared-ui/` / `docs/product-a/` / `specs/issues/product-a/` / `package.json`）を削除

---

## [2026-01-01] 標準チーム
### 初版作成
- 全体ディレクトリ構成の初版を作成
- CLAUDE.md 全社共通ポリシーを策定
- 業務用語辞書（business-terms.md）の初版を作成
- issueフロー（Phase 1〜8）を定義
