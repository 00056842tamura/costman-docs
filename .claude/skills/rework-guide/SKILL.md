---
name: rework-guide
description: rework-trace の診断結果（rework-impact-report-{m}.md）を受けて横展開調査・ADR-rework 記録・再スタート誘導（原因工程・-sp-{m} ブランチ）を実施する。対象レポートが無い場合はエラーで停止する
---

# rework-guide スキル（手戻り横方向処方）

`rework-trace` エージェントが出力した `rework-impact-report-{m}.md` を受けて、
**横展開調査 → ADR-rework 記録 → 再スタート誘導** の 3 ステップを実施します。

> ⚠️ このスキルは `rework-impact-report-{m}.md` が存在することが前提です。格納先は **原因工程**によって分岐します（案件リポ側 `specs/{案件キー}/rework/{m}/` ＝ SA/UI/SS-Plan 起因 ／ スタックリポ側 `{スタック}/specs/{案件キー}/rework/{m}/` ＝ PG-Plan/PT-Plan/SS/PG/PT 起因）。
> 先に `rework-trace` エージェントを起動して診断レポートを作成してください。
> 手戻りは **横断機構**（工程に属さない）。構成は 3 リポジトリ（案件リポ＝上流／スタックリポ＝SS 以降）。詳細は `product-rules.md`。

---

## 入力形式

```
/rework-guide
案件キー: {案件キー}
m: {レポート連番}        # 対象 rework-impact-report-{m}
```

---

## 実行手順

### Step 1 — 前提チェック

- [ ] `rework-impact-report-{m}.md`（原因工程に応じ案件リポ側 `specs/{案件キー}/rework/{m}/` またはスタックリポ側 `{スタック}/specs/{案件キー}/rework/{m}/`）が存在するか確認する
  - 存在しない場合は以下のエラーを表示して **処理を停止** する:
    > ❌ エラー: rework-impact-report-{m}.md が見つかりません。
    > `rework-trace` エージェントを先に起動して診断レポートを作成してください。
    > 起動例:
    > > rework-trace エージェント
    > > 対象案件キー: {案件キー}
    > > 対象機能ID: {機能ID}
    > > 問題の概要: {何が問題だったか}

### Step 2 — ドキュメントを読み込む

以下をすべて読み込む:

- `rework-impact-report-{m}.md`（最新のもの・格納先は原因工程に応じ案件リポ側 `specs/{案件キー}/rework/{m}/` またはスタックリポ側 `{スタック}/specs/{案件キー}/rework/{m}/`） — 診断結果（必須）
- `specs/{案件キー}/meta.md` — 影響スタック・機能一覧
- **案件リポ**: `docs/requirements/要件定義_{要件ID}.md`（当該機能IDの分解元要件IDは `docs/base-design/機能一覧.md` で確認）・`docs/base-design/`・`docs/architecture/stack-dependency.md`
- **スタックリポ**: 影響スタックの `{スタック}/CLAUDE.md`・`{スタック}/docs/detail-design/`・`implementation/`・`unit-test/`（存在する範囲）

### Step 3 — 横展開調査を実施する

`rework-impact-report-{m}.md` の「手戻り先工程」と「変更が必要な成果物」をもとに、
**他スタック・他機能ID・他案件への波及** を調査する。

調査観点:

1. **他スタックへの影響**
   - `docs/architecture/stack-dependency.md` で依存関係を確認する
   - 変更が必要な API・DB スキーマ・認証設定・共有テーブルが他スタックに影響するか
   - US-API / frontend / batch など依存スタックの詳細設計・実装を確認する（存在する場合）

2. **他機能IDへの影響**
   - `docs/base-design/機能一覧.md`・他機能の設計/実装で、同じ API・テーブル・クラスを参照している `{カテゴリ}-{3桁連番}` があるか
   - specs の各計画工程 `plan.md`（依存・`blocked-by`）も確認する

3. **他案件への影響**（横断機構ゆえ案件横断で確認）
   - 同一スタック（スタックリポ）を共有する **別 Milestone（別案件キー）**に波及しないか
   - 影響ありの場合は当該案件の担当へ連携が必要な旨を明記する

調査結果をコンソールに出力する:

```
📊 横展開調査結果:
   他スタックへの影響: {あり / なし}
     - {スタック名}: {影響の内容}（影響ありの場合）
   他機能IDへの影響: {あり / なし}
     - {機能ID}: {影響の内容}（影響ありの場合）
   他案件への影響: {あり / なし}
     - {案件キー}: {影響の内容}（影響ありの場合・要連携）
```

### Step 4 — ADR-rework を起票する

`specs/{案件キー}/rework/{m}/` 配下の既存 rework ADR 番号を確認し、
次の番号で **`ADR-rework-{NNN}.md`**（`{NNN}` ＝ rework ADR の連番・案件内通し）を作成する。

`specs/templates/adr-template.md` を読み込み（存在する場合）、以下の内容で ADR のドラフトを作成する:

| セクション | 記載内容 |
|---|---|
| メタ情報 日付 | 今日の日付（YYYY-MM-DD） |
| メタ情報 ステータス | 承認済み |
| メタ情報 種別 | **手戻り（横断ゲート）** ／ 関連 `ADR-SA/UI/SS-{n}`（存在する場合） |
| 背景 ①状況 | rework-impact-report-{m}.md の「問題概要」をもとに、何が起きたかを説明する |
| 背景 ②課題・問題 | rework-impact-report-{m}.md の「診断結果」に記載された根本原因 |
| 背景 ③制約 | 既存 ADR の決定は変更しない旨、横展開調査で判明した他スタック・他機能ID・他案件への制約 |
| 決定事項 | 手戻り先工程、修正が必要な成果物、他スタック・他機能ID・他案件への影響有無 |
| 理由 | rework-impact-report-{m}.md の「判断根拠」をもとに説明する |
| 影響 | 手戻りによって再実施が必要になる工程の範囲、他スタック・他機能ID・他案件への波及 |
| 未確定事項 | 手戻り対応後に改めて確認が必要な事項があれば記載する |

> **ADR の系列**: 工程内の設計判断は `ADR-SA/UI/SS-{n}`（1 ADR ＝ 1 決定の連番）。
> 手戻り（横断機構）の決定は **`ADR-rework-{NNN}`** として別系列で記録する。

ドラフトをユーザーに提示し、内容の確認・修正・承認を求める。
承認後に、対象レポートと同じリポジトリ側（案件リポ側 `specs/{案件キー}/rework/{m}/ADR-rework-{NNN}.md` またはスタックリポ側 `{スタック}/specs/{案件キー}/rework/{m}/ADR-rework-{NNN}.md`）に書き込む（push し、対応する rework issue の PR に含める）。

### Step 5 — 再スタート手順を出力する（原因工程・`-sp-{m}` ブランチ）

ADR 承認後、`rework-impact-report-{m}.md` で特定された **手戻り先工程**に応じた再スタート手順をコンソールに出力する。

**起こす issue・ブランチ（`product-rules.md`「手戻り（rework）ブランチ・issue」＝ `-sp-{m}`）**:
- **上流・計画**（SA/UI/SS-Plan＝案件リポ ／ PG-Plan/PT-Plan＝スタックリポ）= **新規 issue ＋ ブランチ `feature/{案件キー}-{工程}-sp-{m}`**（例 `-sa-sp-{m}`・`-pgplan-sp-{m}`）
- **実施**（SS/PG/PT＝スタックリポ）= **既存機能改修は当該 issue を Reopen・追加対応は新規 issue**。ブランチは `feature/{案件キー}-{ss|pg|pt}-{機能ID}-sp-{m}`

```
🔄 再スタート手順:

【対象案件】: {案件キー}
【再開工程】: {rework-impact-report-{m}.md で特定された手戻り先工程}
【issue/ブランチ】: {-sp-{m} 体系。実施工程は既存改修=Reopen／追加=新規}

工程別の再開手順:
  - 工程1 SA   → .claude/orchestrators/issue-to-requirement.md → /requirement-doc-gen（docs/requirements/ 修正）
  - 工程2 UI   → .claude/orchestrators/issue-to-design.md → /basic-design-gen（docs/base-design/ 修正）
  - 工程3/5/7 計画 → /issue-plan 工程: {SS-Plan|PG-Plan|PT-Plan}（plan.md 修正・PR）
  - 工程4 SS   → /detailed-design-gen → /detailed-design-review（{スタック}/docs/detail-design/ 修正）
  - 工程6 PG   → /springer-scaffold（frontend は /reacter-code-gen）→ /springer-review（{スタック}/src 修正のみ。docs生成なし）
  - 工程8 PT   → /springer-unit-test-gen（＋/blackbox-test-gen）→ /consistency-check（{スタック}/src/test・docs/unit-test/ 修正）

{他スタック / 他機能ID / 他案件への影響ありの場合:}
⚠️ 以下にも影響があります。再スタート時に合わせて確認してください:
   - {スタック名 / 機能ID / 案件キー}: {必要な対応}

✅ 手戻り完了後の手順:
{手戻りで中断していた工程に戻る旨を出力する}
```

---

## 制約

- 🚫 `rework-impact-report-{m}.md`（原因工程に応じ案件リポ側 `specs/{案件キー}/rework/{m}/` またはスタックリポ側 `{スタック}/specs/{案件キー}/rework/{m}/`）が存在しない場合はエラーで停止する
- 🚫 書き込むファイルは `ADR-rework-{NNN}.md` のみ（push・対応する rework issue の PR に含める。他のファイルは変更しない）
- ✅ 横展開調査結果はコンソール出力のみ（ファイルに書き込まない）
- ✅ 再スタート手順はコンソール出力のみ（`-sp-{m}` 体系・実施工程は Reopen/新規の判断を明示）
- ✅ 工数ログは rework も issue 単位（格納先リポ側の `specs/{案件キー}/rework/{m}/llm-usage.jsonl`・push・PR対象）

---

## 関連ツール・参照先

- 前工程（縦方向診断）: `rework-trace` エージェント（`.claude/agents/rework-trace.md`）
- ブランチ・issue（`-sp-{m}`・Reopen）: `.claude/rules/product-rules.md`「手戻り（rework）ブランチ・issue」
- エスカレーション元: `.claude/skills/consistency-check`（**[設計不整合]かつ上位波及あり**の場合 `rework-trace` へ）
- ADR テンプレート: `specs/templates/adr-template.md`
- スタック間依存: `docs/architecture/stack-dependency.md`
- 手戻りフロー全体: `README.md 付録C`

---

## 作業指示

$ARGUMENTS
