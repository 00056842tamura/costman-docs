---
name: issue-init
description: 工程ごとに issue を起票し、specs/{案件キー}/{工程}/{issue_id}/ に作業ディレクトリを生成する
---

# 工程issue 着手 — 工程ごとの作業ディレクトリ生成

指定された工程の GitHub issue を起票（または既存 issue を指定）し、その工程の作業ディレクトリと検討メモ雛形を
`specs/{案件キー}/{工程}/{issue_id}/` に生成する。各工程の開始時に実行する。

> 前提: 案件開始（`/case-init`）が完了し `specs/{案件キー}/meta.md`・`feature/{案件キー}` が存在すること。
> **成果物（要件定義書・基本設計書 等）は `docs/` へ直接書く**ため specs にはコピーしない（直接編集モデル）。

## 工程と識別子
| 工程 | issue種別 | ラベル | ブランチ | specs ディレクトリ | 検討メモ雛形 |
|---|---|---|---|---|---|
| SA | phase | `[SA]` | `feature/{案件キー}-sa` | `requirements/{issue_id}/` | meta・discussion-log・adr |
| UI | phase | `[UI]` | `feature/{案件キー}-ui` | `base-design/{issue_id}/` | meta・discussion-log・adr（実装対象クラス一覧・テストシナリオは `docs/base-design/` へ直接生成） |
| SS-Plan | phase | `[SS-Plan]` | `feature/{案件キー}-ssplan`（案件リポ） | `detail-design-plan/{issue_id}/` | meta・discussion-log・**plan**（ADR なし） |
| SS | task | `[SS-{機能ID}]` | `feature/{案件キー}-ss-{機能ID}`（**スタックリポ**） | `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` | WB（controller/service/repository/frontend/batch.md）・adr（**SS-Plan で先行起票済みの着手**） |
| PG-Plan | phase | `[PG-Plan]` | `feature/{案件キー}-pgplan`（**スタックリポ・スタック単位**） | `{スタック}/specs/{案件キー}/implementation-plan/{issue_id}/` | meta・discussion-log・**plan**（ADR なし） |
| PG | task | `[PG-{機能ID}]` | `feature/{案件キー}-pg-{機能ID}`（**スタックリポ**） | `{スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/` | 実装作業メモ・adr（**PG-Plan で先行起票済みの着手**・WB は SS 成果を参照） |
| PT-Plan | phase | `[PT-Plan]` | `feature/{案件キー}-ptplan`（**スタックリポ・スタック単位**） | `{スタック}/specs/{案件キー}/unit-test-plan/{issue_id}/` | meta・discussion-log・**plan**（ADR なし） |
| PT | task | `[PT-{機能ID}]` | `feature/{案件キー}-pt-{機能ID}`（**スタックリポ**） | `{スタック}/specs/{案件キー}/unit-test/{issue_id}_{機能ID}/` | テスト作業メモ・adr（**PT-Plan で先行起票済みの着手**） |

> 全工程（SA・UI・SS-Plan・SS・PG-Plan・PG・PT-Plan・PT）を対象とする（案件開始＝`/case-init`）。残るはリリース前ゲート（工程外）のみ。
> SA/UI/SS-Plan は **案件リポ**（`specs/{案件キー}/...`）、SS 以降の下流工程は **スタックリポ**（`{スタック}/specs/{案件キー}/...`・自リポの git で管理）で作業（同一 Milestone・`feature/{案件キー}` 同名）。
> **SS の issue は SS-Plan で先行起票済み**のため、issue-init（SS）は**着手（ブランチ作成・作業ディレクトリ準備）のみ**（新規起票しない）。

## 入力形式
```
/issue-init
工程: SA            # または UI
案件キー: inventory-2026-001
チケット URL: https://github.com/example/repo/issues/123   （または本文を貼り付け）
担当者: 山田太郎
```

## 実行手順（SA / UI / SS-Plan / PG-Plan / PT-Plan 工程）

> 以下 `{工程dir}` は SA=`requirements` / UI=`base-design` / SS-Plan=`detail-design-plan` / PG-Plan=`implementation-plan` / PT-Plan=`unit-test-plan`、`{工程ブランチ}` は SA=`-sa` / UI=`-ui` / SS-Plan=`-ssplan` / PG-Plan=`-pgplan` / PT-Plan=`-ptplan`。
> SS-Plan・PG-Plan・PT-Plan は**成果物が specs の `plan.md`**（docs 直接ではない）で、**ADR なし・gate は PR**（計画成果物も specs として push し、他工程と同じ PR でレビュー・承認する）。

### Step 1 — 入力チェック
- [ ] `specs/{案件キー}/meta.md` が存在する（無ければ `/case-init` を促して停止）
- [ ] 工程が **SA / UI / SS-Plan**（案件リポ）か **PG-Plan / PT-Plan**（スタックリポ・計画＝PR）か **SS / PG / PT**（スタックリポ・後述の SS/PG/PT 手順へ）であることを確認
  - ※**SS-Plan のみ案件リポ**（UI 工程で確定済みの機能ID〔スタック×API/画面/ジョブ単位〕を棚卸し。スタックへの分解は UI 工程で完了済みのため再分解しない）。**PG-Plan/PT-Plan はスタックリポ**（SS から降りるスタック単位の計画・詳細は `product-rules.md`）
- [ ] SS-Plan / PG-Plan / PT-Plan の入力（実装対象クラス一覧・テストシナリオ・テーブル定義書等）は `docs/base-design/`（push 済み正本）から直接読む
- [ ] 工程が **SS / PG / PT** の場合: issue 本文の「依存（blocked-by）」節または plan.md の `blocked-by` 欄を確認し、前工程 PR がマージ済みであることを確認する。未マージの場合は「⚠️ 確認: #{前issue番号} の PR がまだオープンです。blocked-by が解消されていませんが着手しますか？」と確認する
- [ ] 工程が **SS / PG / PT** の場合: `specs/{案件キー}/meta.md` の「スタックリポ」欄がプレースホルダー（`{owner}/{repo}` のまま）でないことを確認する。プレースホルダーのままなら **エラーで停止**し「meta.md のスタックリポ欄に実際のリポジトリスラッグ（例: `ai-event-sol/TEST_BS`）を設定してから再実行してください」と案内する
- [ ] 工程が **SS / PG / PT** の場合: 対象スタックディレクトリ（例: `bs/`）に `.git` が存在することを確認する。存在しない場合は **エラーで停止**し「`{スタック}/` を独立した git リポジトリとして clone してから再実行してください（`git clone {URL} {スタック}/`）」と案内する（`.git` がない = 案件リポの一部として追跡されている可能性があり誤 commit リスクがある）
- [ ] issue-id を確定（GitHub issue 番号。URL 末尾／引数から抽出）
- [ ] `specs/{案件キー}/{工程dir}/{issue_id}/` が未存在（存在すれば **警告して停止**）

### Step 2 — チケット情報の取得
- URL 指定: WebFetch で取得（失敗時は本文貼り付けを促す）。本文貼り付け: そのまま使用。
- 抽出: タイトル・本文・ラベル・起票日・担当者。

### Step 3 — issue 起票・ブランチ作成（gh 連動）
> `.claude/rules/github-ops.md` に従い、工程 issue 起票＋作業ブランチ作成。SA/UI/SS-Plan=案件リポ・PG-Plan/PT-Plan=スタックリポ（`meta.md` のリポジトリ欄の slug を `--repo` に渡す）。
```bash
.claude/scripts/gh/issue-open.sh --repo {owner}/{リポ} --case {案件キー} \
  --phase {sa|ui|ssplan|pgplan|ptplan} --title "{要約}" --create-branch
# 本文は .github/ISSUE_TEMPLATE/{phase}.md を読み込み「## メタ」ブロックのみ動的値に差し替えて自動生成
# （テンプレート未検出時のみ簡易 handoff スキーマにフォールバック）
# 作成 issue 番号/URL を meta.md の issue-id 対応表へ記入
```
> gh 未導入/未認証なら手動（`github-ops.md` §3/§5）: ラベル `工程:xx,type:phase,repo:{case|stack}`・Milestone=`{案件キー}` で起票 → `git switch feature/{案件キー} && git switch -c feature/{案件キー}-{工程}`。

作業ブランチ作成直後、issue番号が確定した時点で、**このリポジトリ（cwd）のローカル・未追跡ファイル**にスコープ確定情報を1回だけ書き込む（`log_work.py`/`log_llm_usage.py` がブランチ名解析より優先して読む。ADR-010参照）:
```bash
mkdir -p .claude
cat > .claude/.issue-scope.json <<EOF
{
  "case_key": "{案件キー}",
  "phase": "{sa|ui|ssplan|pgplan|ptplan}",
  "issue_id": "{issue_id}",
  "branch": "feature/{案件キー}-{工程ブランチ}",
  "work_log": "specs/{案件キー}/{工程dir}/{issue_id}/work-log.md"
}
EOF
```
（`{工程dir}` は Step 1 冒頭の表を参照。このファイルは `.gitignore` 済み・他作業者のフレッシュチェックアウトには引き継がれない。無い場合は既存のブランチ名解析へ自動フォールバックするため、書き込みを失敗させても後続工程を止めない）

### Step 4 — 作業ディレクトリと検討メモ雛形の作成
```bash
mkdir -p specs/{案件キー}/{工程dir}/{issue_id}        # SA/UI は配下に adr/ も作成（mkdir .../adr）
```
`specs/templates/` から **検討メモ／計画雛形のみ**コピー（docs 成果物テンプレはコピーしない）:

| コピー元 | コピー先 | 対象工程 |
|---|---|---|
| `specs/templates/issue-meta.md` | `specs/{案件キー}/{工程dir}/{issue_id}/meta.md` | SA・UI・SS-Plan・**PG-Plan・PT-Plan** |
| `specs/templates/discussion-log.md` | `specs/{案件キー}/{工程dir}/{issue_id}/discussion-log.md` | SA・UI・SS-Plan |
| `specs/templates/adr-template.md` | `specs/{案件キー}/{工程dir}/{issue_id}/adr/adr-template.md` | SA・UI（**SS-Plan は ADR なし**） |
| `specs/templates/plan.md` | `specs/{案件キー}/{detail-design-plan\|implementation-plan\|unit-test-plan}/{issue_id}/plan.md` | **SS-Plan / PG-Plan / PT-Plan** |

### Step 5 — 工程issue meta の初期値設定
コピーした `meta.md`（issue-meta）に記入: 案件キー・工程（SA/UI）・issue種別=phase・issue-id・タイトル・issueURL・担当者・ブランチ・起票日=今日。

### Step 6 — discussion-log に元チケットを記入
`discussion-log.md` の「元チケット内容」セクションにタイトル・本文・ラベル・担当者を転記する。

### Step 7 — 案件 meta の issue-id 対応表へ追記
`specs/{案件キー}/meta.md` の「issue-id 対応表」の **当該工程行（SA / UI / SS-Plan）**に issue-id・ブランチを記入する
（`log_work.py` の issue-id 解決に必要なため必須）。

### Step 8 — 完了報告
```
✅ {案件キー} の {工程} 工程に着手しました（issue-{issue_id}）。
📁 specs/{案件キー}/{工程dir}/{issue_id}/
🌿 feature/{案件キー}{工程ブランチ}

🎯 次のアクション:
   SA → .claude/orchestrators/issue-to-requirement.md（壁打ち）
   UI → .claude/orchestrators/issue-to-design.md（壁打ち）
   SS-Plan → /issue-plan 工程: SS-Plan 案件キー: {案件キー} issue-id: {issue_id}（詳細設計計画）
   PG-Plan → /issue-plan 工程: PG-Plan 案件キー: {案件キー} issue-id: {issue_id}（実装計画）
   PT-Plan → /issue-plan 工程: PT-Plan 案件キー: {案件キー} issue-id: {issue_id}（単体テスト計画）
   の手順に従い、{案件キー} の {工程} 工程（issue-{issue_id}）を進めてください。
```

## 実行手順（SS / PG / PT 工程・スタックリポ）
> SS/PG/PT issue は計画工程（SS-Plan / PG-Plan / PT-Plan）で**先行起票済み**（スタック×機能ID）。issue-init は**着手のみ**（新規起票しない）。対象スタックリポで実行。
> 工程別: **SS** → ブランチ `-ss-{機能ID}`・dir `detail-design/{スタック}/{issue_id}_{機能ID}/`（WB テンプレ配置）／ **PG** → ブランチ `-pg-{機能ID}`・dir `implementation/{スタック}/{issue_id}_{機能ID}/`（実装作業メモ）／ **PT** → ブランチ `-pt-{機能ID}`・dir `unit-test/{スタック}/{issue_id}_{機能ID}/`（テスト作業メモ）。WB/設計/実装は上流工程の成果を参照。

### Step S1 — 対象の確認
- [ ] 対象 issue（SS=`[SS-{機能ID}]` / PG=`[PG-{機能ID}]`・スタックリポ・同一 Milestone）が計画工程で先行起票済みか確認（未起票なら SS-Plan/PG-Plan の計画 PR へ戻る）
- [ ] スタック・機能ID・issue-id を確定
- [ ] **上流成果物を直接読む**（手元に無い/別作業者引き継ぎ時は該当ブランチを `git fetch`/`git checkout` する）: **PG→WB**（`{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/`。SS 工程の PR がスタックリポの統合ブランチ `feature/{案件キー}` にマージ済みであれば、そのまま同ブランチから分岐した PG ブランチ上で直接読める）。**PT の受け入れ条件（テストシナリオ）は `docs/base-design/テストシナリオ.md`（push 済み正本）から直接読む**

### Step S2 — ブランチ作成・アサイン（スタックリポ・gh 連動／issue は先行起票済み）
> issue は計画工程で**先行起票済み**のため、ここでは **作業ブランチ作成＋担当アサイン**のみ（新規起票しない・`github-ops.md` §4/§6）。
```bash
# 着手する sub-issue を自分にアサイン（チケットを拾う）
gh issue edit {issue番号} --repo {owner}/{スタックリポ} --add-assignee @me
# 作業ブランチを feature/{案件キー} から作成
git switch feature/{案件キー} && git switch -c feature/{案件キー}-{ss|pg|pt}-{機能ID} && git push -u origin HEAD
# ローカル clone が無ければリモートに直接 ref を作成
# gh api -X POST repos/{owner}/{スタックリポ}/git/refs -f ref="refs/heads/feature/{案件キー}-{ss|pg|pt}-{機能ID}" \
#   -f sha="$(gh api repos/{owner}/{スタックリポ}/git/ref/heads/feature/{案件キー} --jq .object.sha)"
```
ブランチ作成直後、**当該スタックリポ（`{スタック}/`）内**のローカル・未追跡ファイルにスコープ確定情報を1回だけ書き込む（`log_work.py`/`log_llm_usage.py` がブランチ名解析より優先して読む。ADR-010参照。編集対象ファイルの帰属先リポジトリ判定〔`find_owning_repo_root`〕自体は変更しない — このマーカーは「どのリポジトリか」が判明した後の「そのリポジトリ内のどの issue か」の解決にのみ使う）:
```bash
mkdir -p {スタック}/.claude
cat > {スタック}/.claude/.issue-scope.json <<EOF
{
  "case_key": "{案件キー}",
  "phase": "{ss|pg|pt}",
  "issue_id": "{issue_id}",
  "feature_id": "{機能ID}",
  "branch": "feature/{案件キー}-{ss|pg|pt}-{機能ID}",
  "work_log": "specs/{案件キー}/{detail-design|implementation|unit-test}/{issue_id}_{機能ID}/work-log.md"
}
EOF
```
（他作業者のフレッシュチェックアウトには引き継がれない前提のローカル・未追跡ファイルとして扱う。無い場合は既存のブランチ名解析へ自動フォールバックする。**スタックリポの `.gitignore` に `.claude/.issue-scope.json` の除外が無い場合は追記する**（標準チーム受領テンプレート由来の `.gitignore` は本リポジトリの管理外のため、スタックリポ側で個別に確認・追記が必要））

### Step S3 — 作業ディレクトリ準備
```bash
# SS:  mkdir -p {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/adr
# PG:  mkdir -p {スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/adr
# PT:  mkdir -p {スタック}/specs/{案件キー}/unit-test/{issue_id}_{機能ID}/adr
```
- **SS**: `specs/templates/design/phase-detailed/` から対象スタックの WB（bs/us-*: `controller/service/repository.md`、frontend: `frontend.md`、batch: `batch.md`）＋ `issue-meta.md`・`discussion-log.md`・`adr/adr-template.md` を配置。
- **PG**: `issue-meta.md`・`discussion-log.md`（実装作業メモ）・`adr/adr-template.md` を配置（WB は新規作成せず SS 成果 `detail-design/...` を参照）。
- **PT**: `issue-meta.md`・`discussion-log.md`（テスト作業メモ）・`adr/adr-template.md` を配置（テストコードは新規作成・実装は PG 成果 `implementation/...` を参照）。

### Step S4 — 案件 meta 対応表の更新
当該 issue（スタック×機能ID）の issue-id・ブランチ・スタックを対応表へ記入（計画工程の先行起票時に登録済みなら確認）。

### Step S5 — 完了報告
```
✅ {案件キー} の {SS|PG|PT} 工程（{スタック}×{機能ID}・issue-{issue_id}）に着手しました。
📁 {スタック}/specs/{案件キー}/{detail-design|implementation|unit-test}/{issue_id}_{機能ID}/
🌿 feature/{案件キー}-{ss|pg|pt}-{機能ID}（スタックリポ）
🎯 次のアクション:
   SS → /detailed-design-gen 案件キー: {案件キー} 機能ID: {機能ID} スタック: {スタック} SS issue-id: {issue_id}
   PG → /springer-scaffold 案件キー: {案件キー} 機能ID: {機能ID} スタック: {スタック} PG issue-id: {issue_id}（frontend は /reacter-code-gen）
   PT → /springer-unit-test-gen 案件キー: {案件キー} 機能ID: {機能ID} スタック: {スタック} PT issue-id: {issue_id}（＋/blackbox-test-gen）
```

## 制約
- 🚫 既存の作業ディレクトリを上書きしない（必ず存在チェック）
- 🚫 成果物テンプレ（requirement / 基本設計書 等）を specs にコピーしない（成果物は `docs/` 直接＝直接編集モデル）
- ✅ 案件 meta の issue-id 対応表を必ず更新する（自動作業ログのため）

## 関連
- 案件開始: `.claude/skills/case-init/SKILL.md`
- SA 壁打ち: `.claude/orchestrators/issue-to-requirement.md`
- UI 壁打ち: `.claude/orchestrators/issue-to-design.md`
- SS-Plan 詳細設計計画: `.claude/skills/issue-plan/SKILL.md`
- SS 詳細設計生成: `.claude/skills/detailed-design-gen/SKILL.md`（＋batch: `/batch-design-gen`）
- テンプレート一覧: `specs/templates/`（SS-Plan は `plan.md`・SS は `design/phase-detailed/`）

## 作業指示
$ARGUMENTS
