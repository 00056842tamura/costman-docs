# GitHub 運用規約（gh CLI 連動）

> このファイルは新ワークフローの **GitHub（`gh` CLI）連動の唯一の規約**。Milestone・issue・ブランチ・ラベルの命名と操作を集約する。
> 背景意図: **チケット（issue）単位で別の作業者が回せる**こと。issue 1 件にその工程を進めるのに必要な情報（工程・案件キー・機能ID・ブランチ・上流成果物・依存・ゲート条件）を載せ、担当者は issue を起点に `/issue-init` から着手できる。

---

## 0. 前提（gh 認証・権限）

- **gh CLI** をインストール（`gh --version`）。`gh auth login`（ブラウザ OAuth）または **PAT**（社内 SSO 必須なら SSO 認可付与）。
- **最小権限**: 原則 `repo`（issue / PR / Milestone を含む）。組織読み取りが要る場合のみ `read:org`。**admin / delete 等は付与しない**。
- 資格情報は OS の資格情報ストア（`gh` 既定）。**トークンをコード・設定・ログに書かない**（`security-policy.md` 準拠）。
- ⚠️ ヘッドレス / CI / cron 実行ではブラウザ OAuth が使えないため、**`GH_TOKEN` 環境変数（最小権限 PAT）** を使う。

---

## 1. リポジトリ・Milestone・統合ブランチ（3 リポ）

- **3 リポジトリ**: LLM機構テンプレート 案件対応リポ（上流 docs ＋ SS-Plan）スタックリポ（SS 以降・スタックごと）。
- **Milestone = 案件**。`{案件キー}`（例 `inventory-2026-001`）を **案件リポ・各スタックリポで同名**作成。
- **統合ブランチ**: `feature/{案件キー}`（`develop` から分岐）を ②③ で**同名**。
- 案件リポ slug・スタックリポ slug は `specs/{案件キー}/meta.md` の「リポジトリ」欄に記録し、各 `gh` 操作の `--repo {owner}/{repo}` に渡す（meta.md は案件ごとの確定記録として git 管理・push 対象）。
- ②③のリポジトリは**プロダクトにつき1つを使い回す**不変値のため、`.claude/repositories.local.md`（個人ローカル・`.gitignore` 対象。ひな形 `.claude/repositories.local.md.example`）に書き溜めておくと、`/case-init` 実行時に owner/repo の再入力が不要になる（`case-init/SKILL.md` Step 2 が優先的に読み込む）。
- **リポジトリ作成（プロダクト開始時・1回のみ）**: 案件リポは `gh repo create {repo} --template {①テンプレリポ}` で生成（`.claude/` 機構を自動コピー）。スタックリポは空リポを手動作成。**案件（Milestone）を追加するたびにリポジトリを作り直すのではなく、既存リポに `case-bootstrap.sh` で Milestone・ラベル・統合ブランチを追加する。** この手順は人間が実行する。`/case-init`・`case-bootstrap.sh` は既存リポジトリの存在を前提とし、リポジトリの新規作成は代行しない（存在しない場合はエラーで停止し人間に作成を依頼する）。
- **機構の最新同期**: `template-sync.sh --template {①} --target {repo} --create-pr`（**機構レイヤのみ**・プロダクト成果物不可侵・`CLAUDE.md`/`.gitignore` は差分報告）。

```bash
# 案件ブートストラップ（Milestone＋統合ブランチを各リポに用意）
.claude/scripts/gh/case-bootstrap.sh --case "{案件キー}" \
  --case-repo "{owner}/{案件リポ}" \
  --stack-repo "{owner}/{bsリポ}" --stack-repo "{owner}/{us-apiリポ}" ...
```

### ⚠️ 並行案件（同時進行）時の運用ガイドライン

案件リポ・スタックリポはプロダクトにつき1つを使い回す設計のため、複数案件が同時に `docs/` へ書き込む工程（SA・UI）では以下のリスクがあります。

| リスク | 対象ファイル | 推奨対処 |
|---|---|---|
| 機能ID採番の重複（カテゴリ別連番） | `docs/requirements/機能一覧.md` | **SA工程は1案件ずつ完了**させてから次案件の SA を開始する。並行が避けられない場合はカテゴリごとに採番範囲をあらかじめ分割（例: 案件A = `bs-001`〜`bs-020`、案件B = `bs-021`〜） |
| 一覧表への同時追記コンフリクト | `docs/base-design/Web_API_IF一覧表.md`・`docs/base-design/テーブル一覧.md` | **UI工程は1案件ずつ完了**させてから次案件の UI を開始する。コンフリクト発生時は両追記行を保持してマージ |
| セクション更新型ファイルのコンフリクト | `docs/requirements/業務概要.md` 等 | 同一セクションを複数案件が同時更新しないよう調整する |
| 案件メタの対応表コンフリクト | `specs/{案件キー}/meta.md`（issue-id 対応表・手戻り管理表） | specs/ も push 対象のため同じリスクを持つ。**案件キーごとにディレクトリが分かれる**ため他案件との衝突は起きないが、同一案件を複数人が同時編集する場合は上記と同様に調整する |

SS 工程以降はスタックリポ（③）管轄に移り、案件リポの `docs/` への変更が発生しないため、上記リスクは SA・UI 工程に限定されます。

---

## 2. ラベル体系（issue の分類＝検索・担当割当の軸）

| ラベル | 値 | 用途 |
|---|---|---|
| `工程` | `sa` `ui` `ss-plan` `ss` `pg-plan` `pg` `pt-plan` `ut` | どの工程の issue か |
| `type` | `phase`（全体/計画ゲート）／`task`（area別実施） | ゲート粒度 |
| `area` | `bs` `us-api` `us-mpa` `frontend` `batch` 等 | task issue の対象スタック |
| `rework` | （付与のみ） | 手戻り（横断機構・`-sp-{m}`） |
| `repo` | `case`（案件）／`stack`（③スタック） | どのリポの issue か（横断検索用） |

- ラベルは案件ブートストラップ時に各リポへ作成（`gh label create`・冪等に `--force`）。

---

## 3. issue 命名・本文スキーマ（チケット引き継ぎの核）

### 命名
- **phase issue**（SA/UI/SS-Plan/PG-Plan/PT-Plan）: `[{工程}] {要約}`（例 `[SA] 要件定義`・`[PG-Plan] bs 実装計画`）。
- **task issue**（SS/PG/PT・先行起票）: `[{SS|PG|PT}-{機能ID}] {スタック} {機能名}`（例 `[SS-bs-001] bs 在庫登録`）。
  > 機能ID（`{カテゴリ}-{3桁連番}`。カテゴリ: `bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter`）は UI 工程で**要件ID×スタック×API/画面/ジョブ単位**（1機能ID=1API/1画面/1ジョブ）に分解・採番される。1 つの業務要求（要件ID）が複数スタック・複数エンドポイントに跨る場合は複数の機能IDに分解されるため、旧バージョン（機能ID=要件相当の粒度）に比べ **task issue 数が増加する**。`{スタック}` は当該機能IDが属する単一スタックを表す（機能ID自体が1スタックに閉じた単位のため area とスタックは常に一致する）。

### 本文スキーマ（issue テンプレートで雛形化・別作業者が単独で着手できる粒度）
> `issue-open.sh`／`subissue-bulk.sh` は `.github/ISSUE_TEMPLATE/{sa|ui|ss-plan|ss|pg-plan|pg|pt-plan|pt}.md` を読み込み、
> 「## メタ」（task issue は「## 依存」含む）ブロックのみ動的値に差し替えて本文を生成する（`lib/issue-template.sh`）。
> 説明文・完了ゲート等はテンプレートを唯一の真実の源とし、手書きの本文生成ロジックを重複させない（冪等性・再現性）。
> テンプレート未検出時のみ簡易 handoff スキーマにフォールバックする。
```
## メタ
- 案件キー: {案件キー}（Milestone）
- 工程: {工程}（type:{phase|task}）
- 機能ID: {カテゴリ}-{3桁連番}（task のみ）／対象スタック: {スタック}
- 作業ブランチ: feature/{案件キー}-{工程}[-{機能ID}]
- リポ: 案件リポ / スタックリポ（{owner}/{repo}）

## 上流成果物（着手前に読む）
- docs/...（リポに push 済みの正本）へのリンク。実装対象クラス一覧・テストシナリオ・テーブル定義書は `docs/base-design/` の正本を直接読む
- 計画 plan.md・discussion-log・adr、cross-phase 中間物（詳細設計WB）も `specs/{案件キー}/...`（SS 以降は `{スタック}/specs/{案件キー}/...`）として push 済みのため、対応する統合ブランチ上で直接読む（§3-B）

## 依存（blocked-by）
- Blocked by: #{N} ...（解消まで着手不可）

## 着手手順
- `/issue-init 工程: {工程} 案件キー: {案件キー}`（必要なら 機能ID/対象スタック）→ 各工程スキル（CLAUDE.md §4）

## 完了ゲート
- [ ] 成果物（docs/・specs/ とも push 済み）
- [ ] ゲート（PR レビュー・マージ）
```

> **blocked-by**: GitHub に専用フィールドは無いため、本文「依存」節に `Blocked by: #N` を明記（＋ GitHub の sub-issue / タスクリストで補助可）。先行起票時に計画の依存（`plan.md` の `blocked-by`）を転記する。

---

## 3-B. specs/ 成果物の受け渡し（git 管理・工程 PR に同梱）

`specs/{案件キー}/...`（discussion-log・ADR・plan.md・詳細設計WB・レビューレポート・work-log・llm-usage 等の中間生成物）は、`docs/` などの正式成果物と同様に **通常の git 管理対象**であり、当該工程の PR に含めて push する。旧運用（`.gitignore` で除外し GitHub issue コメントへの添付を SSOT とする「issue 添付機構」）は廃止した。

### 配置ルール

| 工程 | 配置リポ | パス |
|---|---|---|
| SA / UI / SS-Plan | 案件リポ | `specs/{案件キー}/{requirements\|base-design\|detail-design-plan}/{issue_id}/` |
| SS / PG-Plan / PG / PT-Plan / PT | スタックリポ | `{スタック}/specs/{案件キー}/{detail-design\|implementation-plan\|implementation\|unit-test-plan\|unit-test}/{issue_id}[_{機能ID}]/` |
| rework | 原因工程に従う（上記のいずれかのリポ） | `.../rework/{手戻りID}/` |

### cross-phase の受け渡し（旧: issue 添付・fetch-artifact.sh → 新: git）

SS の詳細設計WB → PG が読む、plan.md → sub-issue 棚卸しが参照する、といった cross-phase の受け渡しは、**各リポの統合ブランチ `feature/{案件キー}` に工程 PR が順にマージされていく**ことで実現する。後工程は自分のブランチが分岐した時点で既に上流工程の specs/ 成果物を含んでいるため、単に該当パスを直接読めばよい（`docs/base-design/` を SS が直接読むのと同じパターン）。

- ローカルに該当ブランチが無い場合は、通常の `git fetch`/`git pull`/`git checkout` で取得する。
- チェックアウトせずに内容だけ確認したい場合は `git show {ref}:{path}` でリモートブランチの内容を直接読める。
- 「作業者交代時に specs が手元に無い」というケースも、上記の通常の git 操作で解決する（issue 添付機構の DL 手順は不要）。

### ADR の記録形式（工程内で複数決定した場合）

ADR は工程ディレクトリ配下の `adr/ADR-{工程}-{n}.md` に決定事項ごとに連番で作成し、そのまま specs/ の一部として push する（issue コメントへの連結は不要。工程 PR の差分としてレビューできる）。

### バックログ記録形式（機械判定用）

discussion-log.md の「## 未解決事項・TODO」節・ADR の「## 未確定事項（バックログ）」節は、以下の3状態を明確に区別できる記法を**今後新規に起票する分から必須**とする（`docs-to-pr` の「Step 0: バックログ解消ゲート」が本記法の `- [ ]`（書式1）を機械的に走査するため）。

- 未解決: `- [ ] {項目}: {内容}`
- 工程内で解消済み: `- [x] {項目}: {内容}（解消: {決定内容・日付}）`
- ユーザー承認済みの次工程持ち越し: `- [ ] {項目}: {内容}（承認済み持ち越し→{次工程}・承認者: {ユーザー名}・{日付}）`（チェックボックスは意図的に未チェックのまま残し、「未解決だが持ち越しが承認されている」ことを `grep` で区別できるようにする）

> ⚠️ 既存ADR（本ルール追記日より前に作成されたもの）は遡及修正しない（履歴〔work-log・CHANGELOG・過去ADR〕は書き換えない原則を踏襲）。旧書式（チェックボックスなしの素の箇条書き等）のまま残る既存ADRの未確定事項は、本ゲート・hook の走査対象外になる。

## 4. ブランチ運用（`feature/{案件キー}` から分岐・1 issue = 1 ブランチ）

| 工程 | ブランチ | リポ |
|---|---|---|
| 工程1 SA | `feature/{案件キー}-sa` | 案件 |
| 工程2 UI | `feature/{案件キー}-ui` | 案件 |
| 工程3 SS-Plan | `feature/{案件キー}-ssplan` | 案件 |
| 工程4 SS | `feature/{案件キー}-ss-{機能ID}` | ③スタック |
| 工程5 PG-Plan | `feature/{案件キー}-pgplan` | ③スタック（スタック単位） |
| 工程6 PG | `feature/{案件キー}-pg-{機能ID}` | ③スタック |
| 工程7 PT-Plan | `feature/{案件キー}-ptplan` | ③スタック（スタック単位） |
| 工程8 PT | `feature/{案件キー}-ut-{機能ID}` | ③スタック |
| 手戻り: 実施工程（SS/PG/PT）| `feature/{案件キー}-{ss\|pg\|pt}-{機能ID}-sp-{手戻りID}` | 原因工程に従う・issue Reopen |
| 手戻り: 計画・上流工程（SA/UI等）| `feature/{案件キー}-{工程}-sp-{手戻りID}` | 原因工程に従う・新規 issue 起票 |

- ブランチは **`feature/{案件キー}` から分岐**（`product-rules.md`）。issue 起票と同時に作成し、issue 本文の「作業ブランチ」に記載。
- 手戻りの手戻りID（`sp-{3桁連番}`）は `specs/{案件キー}/meta.md` の「手戻り管理表」で採番する。詳細は `product-rules.md §手戻り統一ワークフロー`。

---

## 5. gh コマンド早見表（repo/issue/branch）

```bash
# --- Milestone ---
gh api repos/{owner}/{repo}/milestones -f title="{案件キー}" -f state=open   # 作成（冪等は事前 list 確認）
gh api repos/{owner}/{repo}/milestones --jq '.[].title'                       # 一覧

# --- ラベル（冪等）---
gh label create "工程:sa" --repo {owner}/{repo} --color BFD4F2 --force

# --- 統合ブランチ ---
git -C {repo_local} switch develop && git -C {repo_local} switch -c feature/{案件キー}
git -C {repo_local} push -u origin feature/{案件キー}

# --- 工程 issue 起票（phase）---
gh issue create --repo {owner}/{repo} \
  --title "[SA] 要件定義" \
  --label "工程:sa,type:phase,repo:case" \
  --milestone "{案件キー}" \
  --body-file {本文ファイル}

# --- sub-issue 先行起票（task・スタックリポ）---
gh issue create --repo {owner}/{stackリポ} \
  --title "[SS-bs-001] bs 在庫登録" \
  --label "工程:ss,type:task,area:bs,repo:stack" \
  --milestone "{案件キー}" \
  --body "...（メタ＋Blocked by: #N）"

# --- issue ブランチ作成（issue 番号確定後）---
git switch feature/{案件キー} && git switch -c feature/{案件キー}-sa && git push -u origin feature/{案件キー}-sa

# --- PR 作成（全工程共通・pr-open.sh＝gh pr create）---
gh pr create --repo {owner}/{repo} --base feature/{案件キー} --head feature/{案件キー}-ss-bs-001 \
  --title "[ss] 在庫登録 詳細設計" --label "工程:ss"   # マージは人間レビュー後
# ⚠️ PR ボディの `Closes #N` は PR↔Issue の双方向リンク確立が目的。
#    GitHub の自動クローズは「デフォルトブランチへのマージ時のみ」発動するため、
#    feature/{案件キー} へのマージでは自動クローズされない。
#    issue クローズはレビュー確認後に人間が GitHub WEB 画面から行う。

# --- 一覧（担当者がチケットを拾う）---
gh issue list --repo {owner}/{repo} --milestone "{案件キー}" --label "工程:ss" --state open
```

> 操作はヘルパースクリプトに集約（`.claude/scripts/gh/`）。スキルからはスクリプト呼び出しを基本とし、単発操作は上記コマンドを直書きしてよい。**安全のため各スクリプトは `DRY_RUN=1` で実コマンドを表示のみ**にできる。

---

## 6. チケット引き継ぎ（別作業者が回す）フロー

1. **棚卸し担当**: 計画工程（SS-Plan/PG-Plan/PT-Plan）で `plan.md` を作り、`docs-to-pr` の PR マージ後に **sub-issue を先行起票**（`subissue-open.sh`・blocked-by 付き）。
2. **実施担当（別の人でも可）**: `gh issue list --milestone {案件キー} --label 工程:{工程} --state open` で**未着手・blocked-by 解消済み**の issue を選び自分にアサイン（`gh issue edit --add-assignee @me`）。
3. issue 本文の「着手手順」に従い `/issue-init 工程: {工程} ...` → 工程スキル。上流成果物は **リポの docs/・specs/ から読む**（push 済み正本。`git pull`/`git checkout` すれば手元に揃う）。
4. 完了後ゲート（全工程 PR）。PR マージ後、レビュー確認済みの人間が **GitHub WEB 画面から issue をクローズ**する。`Closes #N` は双方向リンク目的のため自動クローズは発生しない。
5. 依存していた後続 issue の blocked-by が解消 → 次の担当が拾える。

> 引き継ぎが成立する条件: ①issue 本文に工程・案件キー・機能ID・ブランチ・依存が揃う ②上流成果物が docs/・specs/（リポ）に push 済み ③blocked-by が明示。この 3 条件を満たすことがチケット引き継ぎ成立の前提。

---

## 7. 実装状況

- ✅ **specs/ の cross-phase 受け渡し**: §3-B（git push・PR マージによる通常の受け渡し。`attach/fetch-artifact.sh` は廃止済み）。
- ✅ **PR 本体作成**: `pr-open.sh`（`gh pr create`・全工程共通・docs=案件リポ／code・計画=スタックリポ or 案件リポ・**マージは人間**）。
- ✅ **工数集計**: `/usage-report` スキル（`usage_report.py`・jq 不要・工程別/手戻り別集計・`exchange_uuid` 重複排除）。
- ✅ **テンプレ取り込み/同期**: `template-sync.sh`（機構レイヤ同期・プロダクト成果物不可侵）＋初回 `gh repo create --template`。
- 🎉 gh 連動（①テンプレ取り込み／②issue・先行起票／③specs push・PR／④工数集計／⑤PR）が**全て実装済み**。

---

## 関連
- ブランチ・マージ・手戻り: `.claude/rules/product-rules.md`
- ヘルパースクリプト: `.claude/scripts/gh/`
- issue / PR テンプレート: `.github/ISSUE_TEMPLATE/`・`.github/PULL_REQUEST_TEMPLATE/{sa,ui,ss-plan,ss,pg-plan,pg,pt-plan,pt}.md`
- セキュリティ（トークン管理）: `.claude/rules/security-policy.md`
