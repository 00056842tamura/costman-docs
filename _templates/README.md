# パッケージひな形（_templates/）

新規スタックを作成する際のテンプレート集です。`_templates/{type}-template/`（Reacter は `frontend-template/`）配下の
`_received/` に標準チームから受領した実物資材（ワイヤーフレーム・Springer フレームワークのビルド済み jar/pom）を配置し、
そこから実スタックディレクトリへ反映する「受け皿」として機能します。実反映・ビルド確認は `/stack-init` スキルが行います。

## スタック種別とひな形の対応

### フロントエンド（Reacter）

| スタック | アプリ種別 | ひな形 |
|---|---|---|
| `frontend/` | Reacter（SPA フロント） | `frontend-template/`（`_received/` に `reacter-blank-template` を配置） |

> Reacter の開発規約・スキルは整備済みです。フロントエンドの実装を始める場合は `frontend/` スタックに配置し、`frontend/CLAUDE.md` を参照してください。
> コード生成: `/reacter-code-gen` / コードレビュー: `/reacter-code-review` / 規約: `.claude/rules/reacter-*.md`（各トピック別）

### バックエンド・サーバーサイド（Springer）

| ディレクトリ | アプリ種別 | 用途 |
|---|---|---|
| `bs-template/` | BS | DB アクセスを伴うバックエンドサービス |
| `us-mpa-template/` | US-MPA | `@Controller` + Thymeleaf によるサーバーサイド SSR フロント |
| `us-api-template/` | US-API | `@RestController` による SPA 向け JSON API（一般ユーザー向け・管理者向けの両方を含む） |
| `batch-template/` | バッチ | `CommandLineRunner` ベースのバッチアプリ |
| `springer-framework/` | （アプリ種別非依存） | Springer フレームワークのビルド済み jar/pom（例: `springer-core-{version}.jar`/`.pom`）の受け皿。4つの Springer 系スタックが共有 |

## 新規スタック作成手順

新規スタック作成は「①標準チーム受領資材を `_received/` へ配置」→「②`/stack-init` を実行」の2段階で行う。
`/stack-init` が資材存在確認・実スタックへの反映・ビルド／テスト確認までを一括で行う（詳細は `.claude/skills/stack-init/SKILL.md`）。
Springer 系スタックのビルドに必要なフレームワーク jar/pom のローカル `.m2` 登録は、`/stack-init` 実行前に実施者が行う（下記手順を参照）。

### Reacter（フロントエンド）の場合

1. `reacter-blank-template`（GitLab 管理・zip 配布）を入手し、展開した内容を `_templates/frontend-template/_received/` に配置する
2. `/stack-init` を実行する（`スタック種別: frontend`）— `frontend/` への反映・`npm install && npm run build` によるビルド確認まで一括で行う
3. `frontend/CLAUDE.md` を確認し、フレームワーク設定・パッケージ構成・環境変数を記入する
4. `/reacter-code-gen` スキルで実装を開始する

### Springer（バックエンド・サーバーサイド）の場合

#### 1. 受領資材を配置し、フレームワーク jar/pom を `.m2` へ登録する

```bash
# 例: BS スタック用ワイヤーフレームを配置
#     （展開先を _received/ にコピーする。zip そのものを置かない）
cp -r /path/to/展開済みspringer-blank-bs/* _templates/bs-template/_received/

# Springer フレームワークのビルド済み jar/pom（初回のみ・5スタック共有）
#     （フレームワークのソースではなく、受領した jar と pom を配置する。配布物は単一 jar+pom を想定）
cp /path/to/springer-core-3.0.3.jar /path/to/springer-core-3.0.3.pom _templates/springer-framework/_received/

# フレームワーク jar/pom をローカル `.m2` へ登録する（/stack-init は登録を行わないため、実施者が事前に行う）
#     座標（groupId/artifactId/version/packaging）は pom から自動取得されるため個別指定は不要
mvn install:install-file \
  -Dfile=_templates/springer-framework/_received/springer-core-3.0.3.jar \
  -DpomFile=_templates/springer-framework/_received/springer-core-3.0.3.pom
```

#### 2. `/stack-init` を実行する

```
/stack-init
スタック種別: bs
```

`_received/` の資材存在確認 → `bs/` への反映 → `mvn test`（最低限 `mvn compile`）によるビルド確認、までを一括で行う。
資材未配置・ビルド失敗時は停止し、エラー内容が案内される（自動修正はしない）。

#### 3. CLAUDE.md を編集する

`{type}/CLAUDE.md` の冒頭で Springer バージョンを宣言し、ベースパッケージ（`← TBD` マーカー）を確定する。

#### 4. ルート CLAUDE.md の「スタック配置ルール」を確認する

新規スタック命名が `{bs|us-mpa|us-api|batch}/` 形式に従っていることを確認する。

## ひな形を直接編集しないこと

`_templates/` 配下を直接編集すると以降のパッケージ作成に影響します。改善要望がある場合は標準チームに相談してください。
（`_received/` は受領資材の展開先であり本注記の対象外 — 標準チームから最新版を受領するたびに都度上書きされる想定のため、
`_received/` 配下の内容そのものは自由に置き換えてよい。本注記が対象とするのは `_received/` 以外の「型」を表すひな形ファイル
〔`CLAUDE.md`・`BUILD_TBD.md`・`README.md`・`src/` プレースホルダー等〕）。
