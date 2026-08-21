# プロダクト固有ルール（Reacter + Springer 版）

## スタック間依存ルール

### 許可される依存関係
```
frontend/（Reacter）→ us-api    （SPA フロント → US-API）
us-mpa   → bs        （US-MPA から BS API 呼び出し）
us-api   → bs        （US-API から BS API 呼び出し）
```

### 禁止される依存関係
```
frontend/ → bs      （フロントから BS への直接アクセス禁止。必ず US-API を介する）
us-*      → DB      （US から DB への直接アクセス禁止）
bs        → us-*    （逆方向の依存禁止）
```

## 循環依存の防止

- 実装前に `docs/architecture/stack-dependency.md` で依存関係を確認する
- 新たなスタック間依存を追加する場合は標準チームに相談する

## バージョン管理

### Reacter（`frontend/`）
- Node.js バージョンを `frontend/.nvmrc` で固定する
- npm パッケージバージョンはワイルドカード（`*`/`^`/`~`）指定を禁止する（`package-lock.json` で固定）
- 詳細は `.claude/rules/reacter-app-config.md` を参照

### Springer（`bs/` 等）
- **本プロダクトは全 Springer スタックを Springer 3（Spring Boot 4）で確定・統一する**。各スタックの `CLAUDE.md` 冒頭にも Springer 3 を宣言する
- フレームワーク仕様上、1 プロダクト内で Springer 2 と 3 の混在は許容されるが、本プロダクトでは 3 に統一し混在させない（将来混在が必要になった場合は別途決定する）
- 詳細は `.claude/rules/springer-version-matrix.md` を参照

## ブランチ命名規則

> **構成は 3 リポジトリ**（LLM機構テンプレート 案件対応リポ スタックリポ）。
> Milestone（=案件）と統合ブランチ `feature/{案件キー}` は **②③ で同名**にして対応づける。作業は `feature/{案件キー}` から **issue ブランチ**を切って進める。
> `specs/`（discussion-log・adr・plan.md 等）は他の設計ドキュメント資材と同様に **git 管理・push 対象**。SA/UI/SS-Plan は案件リポの `specs/{案件キー}/...`、SS 以降（SS/PG-Plan/PG/PT-Plan/PT）は各スタックリポの `{スタック}/specs/{案件キー}/...` で管理する。全工程のゲートは **PR** に統一する。

### 案件対応リポ（上流＝SA/UI ＋ SS-Plan＝機能ID単位の棚卸し）
```
develop
└── feature/{案件キー}                     案件統合ブランチ（/case-init で develop から分岐）
    ├── feature/{案件キー}-sa              工程1 要件定義 SA（全体ゲート・docs/requirements/・要件ID採番）
    ├── feature/{案件キー}-ui              工程2 基本設計 UI（全体ゲート・docs/base-design/・要件ID→機能ID〔スタック×API/画面/ジョブ単位〕へ分解・採番）
    └── feature/{案件キー}-ssplan          工程3 詳細設計計画 SS-Plan（全体ゲート・UI 工程で確定済みの機能ID単位に sub-issue 棚卸し＋クロススタック依存を確定・PR）
```

### スタックリポ（下流＝SS・PG-Plan・PG・PT-Plan・PT・スタックごと・②と同名 Milestone/統合ブランチ）
```
feature/{案件キー}                          案件統合ブランチ（②と同名）
├── feature/{案件キー}-ss-{機能ID}         工程4 詳細設計 SS（機能ID単位）
├── feature/{案件キー}-pgplan              工程5 実装計画 PG-Plan（スタック単位・計画は specs/PR）
├── feature/{案件キー}-pg-{機能ID}         工程6 実装 PG（機能ID単位）
├── feature/{案件キー}-ptplan              工程7 単体テスト計画 PT-Plan（スタック単位・PR）
└── feature/{案件キー}-pt-{機能ID}         工程8 単体テスト PT（機能ID単位）
```
- **SS-Plan のみ案件リポ**（UI 工程で確定済みの機能ID〔スタック×API/画面/ジョブ単位〕を棚卸しするのみ。スタックへの分解は UI 工程で完了済みのため SS-Plan では再度行わない）。**PG-Plan/PT-Plan はスタックリポ**（SS の per-stack 成果から降りる計画。各 stack が `SS→PG-Plan→PG→PT-Plan→PT` を自リポで完結）。**クロススタック実装順序は SS-Plan の依存（`blocked-by`）を継承**する。
- 下流の実施 issue は**機能ごと**（機能ID＝UI 工程でスタック単位に分解済み）。計画工程（PG-Plan/PT-Plan）は**スタック単位**で plan.md を作り、`{スタック}/specs/{案件キー}/...` に push して PR でレビューする。
- 案件キー: `{プロダクト略称}-{YYYY}-{連番3桁}`（例: `inventory-2026-001`）／要件ID: `R001`（SA 採番・業務要求単位・3桁ゼロ埋め連番）／機能ID: `{カテゴリ}-{3桁ゼロ埋め連番}`（UI 採番・要件ID×スタック×API/画面/ジョブ単位。カテゴリ〔`bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter`〕ごとに独立した3桁ゼロ埋め連番。既存 API ID・ジョブID は維持し対応表で管理）
- 例: 案件リポ `feature/inventory-2026-001-sa`／スタックリポ `feature/inventory-2026-001-pgplan`・`-ss-bs-001`

### 手戻り（rework）統一ワークフロー

> 手戻りは横断機構（工程に属さない）。発生トリガーを問わず（consistency-check / rework-trace / 人間の気づき）単一フローで処理する。
> 判定ロジックの詳細手順（インラインフィックス判定・既存チェックとの重複確認・上位設計書確認・大規模手戻りエスカレーション）の正典は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」にある。本節は手戻りID採番・ブランチ命名・issue運用の規約を定義する。

#### 手戻りID の採番

```
手戻りID: sp-{3桁連番}（例: sp-001, sp-002 ...）
採番単位: 案件単位（Milestone 内でユニーク）
採番場所: specs/{案件キー}/meta.md の「手戻り管理表」に追記して払い出す
```

#### issue 運用ルール

| 対象工程 | 操作 | 理由 |
|---|---|---|
| **実施工程（SS/PG/PT）** かつ影響が閉じる | 既存 issue を **Reopen** | コード・設計書の修正が主体。工程の再実行 |
| **計画・上流工程（SA/UI/SS-Plan/PG-Plan/PT-Plan）** | **新規 issue 起票** | 意思決定のやり直しが主体 |
| 他スタック・他機能・他案件への波及（実施工程でも） | **新規 issue 起票** | 管理複雑性を避けるため例外なし新規 |

#### ブランチ命名

- 実施工程（SS/PG/PT）: `feature/{案件キー}-{ss|pg|pt}-{機能ID}-sp-{手戻りID}`（例: `-ss-bs-001-sp-001`）
- 計画・上流工程（SA/UI/SS-Plan/PG-Plan/PT-Plan）: `feature/{案件キー}-{工程}-sp-{手戻りID}`（例: `-sa-sp-001`・`-pgplan-sp-001`）

#### 成果物

- `rework-impact-report.md`（`ADR-rework-{NNN}` 含む）→ **`specs/{案件キー}/rework/{手戻りID}/`**（push・PR対象。原因工程が SS/PG/PT・PG-Plan/PT-Plan の場合は当該**スタックリポの `{スタック}/specs/{案件キー}/rework/{手戻りID}/`**に配置する）
- `specs/{案件キー}/meta.md` の「手戻り管理表」にステータスを更新する

## マージ方針

```
工程ブランチ → feature/{案件キー}（案件統合ブランチ・工程ゲートPR）
feature/{案件キー} → develop（案件完了時）→ main（リリース判定ゲート通過後）
```

- マージは merge commit（`--no-ff`）で工程の履歴を残す
- `develop → main` はリリース前ゲート（結合テスト・UAT 合格）を満たした場合のみ
- **マルチリポ**: 上記は **案件リポ・各スタックリポそれぞれ**で適用する（同名 `feature/{案件キー}`）。
- **ゲートは全工程 PR に統一**: docs 成果物（SA/UI・SS設計書等）・specs 成果物（plan.md・discussion-log・adr 等）とも同一 PR に含めて push し、通常の PR レビュー・マージで承認する（計画・検討工程のみの「issue ゲート」という特別扱いは廃止）。

## PR の影響スタック表記

PR メッセージに変更が入ったスタックを必ず列挙すること。

```markdown
## 影響スタック
- [x] frontend/
- [x] us-api/
- [ ] bs/（変更なし）
```

## 新規スタック作成

新規スタック作成は「①標準チーム受領資材を `_templates/{type}-template/_received/` へ配置」→「②`/stack-init` を実行」の
2段階で行う（`.claude/skills/stack-init/SKILL.md`）。`/stack-init` が資材存在確認・実スタックディレクトリへの反映・
ビルド／テスト成立確認までを一括で行う。Springer 系スタックのビルドに必要なフレームワーク jar/pom のローカル `.m2`
登録（`mvn install:install-file`）は、`/stack-init` はインストールを行わないため、実施者が `/stack-init` 実行前に行う。

### Reacter（フロントエンド）
- **新規スタック作成時は GitLab 管理の `reacter-blank-template` を使用する**
- その資材を `_templates/frontend-template/_received/` に配置してから `/stack-init`（`スタック種別: frontend`）を実行する
- `frontend/` ディレクトリに反映される（`_templates/frontend-template/`）
- `frontend/CLAUDE.md` に Node.js バージョン・パッケージ構成を記入する

### Springer（バックエンド・サーバーサイド）
- 標準チームから受領したワイヤーフレーム資材を `_templates/{type}-template/_received/` に、Springer フレームワーク
  のビルド済み jar/pom（4スタック共有）を `_templates/springer-framework/_received/` に配置してから `/stack-init` を実行する
- スタック名は `{type}` 形式（`{type}` は `bs` / `us-mpa` / `us-api` / `batch`）
