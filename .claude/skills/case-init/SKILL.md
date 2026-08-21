---
name: case-init
description: 案件(Milestone)を開始し、案件キー確定・案件メタ・案件統合ブランチ・案件横断の成果物ディレクトリ（要件一覧/機能一覧/Web_API_IF一覧表）を用意する
---

# 案件開始 — Milestone 初期化（工程0 の後・各工程の前）

新しい案件(Milestone)を開始するときに **1 回だけ**実行する。案件キーを確定し、案件レベルの作業基盤
（案件メタ・案件統合ブランチ・R###採番台帳）を用意する。

> 各工程の issue 起票・作業ディレクトリ作成は `/issue-init` が工程ごとに行う（本スキルの責務外）。
> 前提: 工程0（リポジトリ初期化: main/develop・branch protection）は手動で完了済みであること。機構（`.claude/` 等）は①テンプレートから取り込み済み/最新であること（初回 `gh repo create --template`／同期 `.claude/scripts/gh/template-sync.sh`・`github-ops.md`）。

## 入力形式
```
/case-init
プロダクト略称: inventory   （省略時は docs/requirements/プロダクト情報.md の値を使用）
連番: 001            （省略時は既存 specs/ を走査して次番号を提案）
```

## 実行手順

### Step 0 — プロダクト情報の読込
- `docs/requirements/プロダクト情報.md` の存在を確認する。
- **存在しない場合**: 「`docs/templates/プロダクト情報.md` を `docs/requirements/プロダクト情報.md` にコピーし、プロダクト名・パッケージ名・プロダクト略称の3項目を記入してから再実行してください」と案内して **停止**。
- **存在する場合**: プロダクト名・パッケージ名・プロダクト略称を読み込む（未記入の項目があれば警告するが、プロダクト略称は Step 1 で入力形式の明示指定があれば代替可）。
- 本ファイルは**プロダクト全体で不変**の情報（プロダクト名・パッケージ名）と、**案件キー生成の元になる略称**を保持する。案件キー自体はここには固定値として書かず、Step 1 で毎回新規生成する。

### Step 1 — 案件キーの確定
- 形式: `{プロダクト略称}-{YYYY}-{連番3桁}`（例: `inventory-2026-001`）。`YYYY` は今日の年。
- プロダクト略称は「入力形式で明示指定された値」＞「Step 0 で読み込んだプロダクト情報.md のプロダクト略称」の優先順で決定する。
- 連番は同プロダクト内で未使用の 3 桁。省略時は `specs/` 直下の `{プロダクト略称}-{YYYY}-NNN/` パターンのディレクトリを走査し、最大連番 +1 を提案する（`specs/issues/` 等の旧パスは参照しない）。
- 既に `specs/{案件キー}/` が存在する場合は **警告して停止**。

### Step 2 — 案件ディレクトリと案件メタの作成
```bash
mkdir -p specs/{案件キー}
```
- `specs/templates/案件-meta.md` を `specs/{案件キー}/meta.md` にコピーし、案件基本情報を記入する
  （案件キー・プロダクト・案件統合ブランチ `feature/{案件キー}`）。
  「プロダクト:」「パッケージ名:」欄には Step 0 で読み込んだ `docs/requirements/プロダクト情報.md` のプロダクト名・パッケージ名を記入する。
- **リポジトリ欄**に 案件リポ・各スタックリポの slug（`{owner}/{repo}`）を記録する（以降の `gh` 操作の `--repo` に渡す／`github-ops.md`）。②③のリポジトリは**プロダクトにつき1つを使い回す**ため、値の取得元は以下の優先順で解決する。
  1. `.claude/repositories.local.md`（個人ローカル・`.gitignore` 対象）が存在し「## 案件リポ」行が記入済みの場合: そこから案件リポの owner/repo を読み込み、ユーザーに再入力を求めずそのまま記入する。
  2. **`.claude/repositories.local.md` が存在しない、または「## 案件リポ」行が空欄の場合**: 「`.claude/repositories.local.md.example` を `.claude/repositories.local.md` にコピーし、「## 案件リポ」の owner/repo 行を記入してから再実行してください」と案内して**停止**する（`docs/requirements/プロダクト情報.md` の Step 0 と同じ「テンプレートをコピーして記入・再実行」パターンに揃える。対話で値を収集して AI 自身が書き込むことはしない）。
  3. 記入済みの案件リポについて、`gh api repos/{owner}/{repo}` で実在確認を行う（`case-bootstrap.sh` の `ensure_repo_exists` と同一コマンドで統一）。
  4. 存在しない場合（404/403等）: 🚫 AIは`gh repo create`等の作成コマンドを実行しない。「リポジトリ「{owner}/{repo}」が見つかりません。人間が作成の上、再実行してください（作成手順: `docs/onboarding/gh-cli-setup.md` §6.1/§6.2）」と案内し、Step 3以降に進まず**停止**する。
  5. **`.claude/repositories.local.md`「## スタックリポ」の各行（bs/us-api/frontend/us-mpa/batch）はこの時点で確定させる必要はない。** どのスタックを使うかは UI 工程（機能IDのスタック分解）で確定するため、各スタックの owner/repo 記入・実在確認・clone の**保証されたゲートは SS-Plan 工程**（`.claude/skills/issue-plan/SKILL.md` Step 3.5）である。今回の案件で使うスタックが既に分かっている場合は、この時点で「## スタックリポ」の該当行を記入してもよい（後述「スタックリポのセットアップ」で前倒しできる、任意の便宜）。記入済みの行があれば、空欄のままではないかを確認し、Step 4 の Milestone・ラベル設定（`--stack-repo`）にそのまま使ってよい。
- 要件ID採番表・機能ID採番表・issue-id 対応表は空ヘッダのまま（各工程で追記）。
- `work-log.md` は `log_work.py` が自動生成するため作成不要。

#### スタックリポのセットアップ（誤 commit 防止・任意の前倒し）

`.claude/repositories.local.md`「## スタックリポ」に owner/repo が**既に記入済み**のスタックがあれば、この時点で**案件リポ直下に clone** し、`.gitignore` を確認してよい（前倒しの利便性のためのオプション。必須ではない）。未記入のスタックは何もせず SS-Plan 工程（`.claude/skills/issue-plan/SKILL.md` Step 3.5）に委ねる。

```bash
# {スタックリポURL} は .claude/repositories.local.md「## スタックリポ」欄に記入済みの URL
# {スタック名} は bs / us-api / frontend / us-mpa / batch のいずれか
git clone {スタックリポURL} {スタック名}/

# 例（記入済みのスタックごとに繰り返す）
git clone https://github.com/{owner}/{bsリポ名}.git bs/
git clone https://github.com/{owner}/{us-apiリポ名}.git us-api/
git clone https://github.com/{owner}/{frontendリポ名}.git frontend/
```

- clone した場合は、案件リポの `.gitignore` に各スタックディレクトリが除外されていることを確認する（デフォルトで有効）。
- **`.gitignore` が有効でない場合、スタックのコードが案件リポに誤 commit されるリスクがある。**
- 使用しないスタックのエントリは `.gitignore` でコメントアウトしてよい。

### Step 3 — 案件横断の成果物ディレクトリ初期化
- `docs/requirements/要件一覧.md` が無ければ `docs/templates/30_要件定義/要件一覧.md` からコピーして初期化する（R###採番台帳・SA 工程で追記）。
- `docs/base-design/機能一覧.md` が無ければ `docs/templates/40_基本設計/機能一覧.md` からコピーして初期化する（`{カテゴリ}-{3桁連番}`採番台帳・要件ID×スタック×API/画面/ジョブ単位・UI 工程で追記）。
- `docs/base-design/Web_API_IF一覧表.md` が無ければ `docs/templates/40_基本設計/Web_API_IF一覧表.md` からコピーして初期化する（API 一覧・UI 工程で追記）。`docs/base-design/README.md`（成果物構成）は既存。
  - 既にあるファイルはそのまま（採番・追記は各工程で行う）。

### Step 4 — Milestone・ラベル・案件統合ブランチの作成（gh 連動）
> `.claude/rules/github-ops.md` に従い、**案件リポ・各スタックリポ**へ Milestone（=案件キー）・ラベル体系・統合ブランチ `feature/{案件キー}` を用意する（②③で同名）。
```bash
# 冪等。DRY_RUN=1 を付けると変更系を表示のみで確認できる
.claude/scripts/gh/case-bootstrap.sh --case {案件キー} \
  --case-repo {owner}/{案件リポ} \
  --stack-repo {owner}/{スタックリポ1} --stack-repo {owner}/{スタックリポ2}
```
> 案件キー↔Milestone を `meta.md` に記録。**gh 未導入/未認証**なら手動で各リポに Milestone・ラベル・`feature/{案件キー}` を作成（`github-ops.md` §0/§5）。

### Step 5 — 完了報告
```
✅ 案件 {案件キー} を開始しました。
📋 docs/requirements/プロダクト情報.md （プロダクト名・パッケージ名・プロダクト略称）
📁 specs/{案件キー}/meta.md       （案件メタ・リポジトリ欄に slug 記録）
🏁 Milestone「{案件キー}」・ラベル体系（案件リポ・各スタックリポ）
🌿 feature/{案件キー}             （案件統合ブランチ・②③同名）
📋 docs/requirements/要件一覧.md       （R###採番台帳）
📋 docs/base-design/機能一覧.md         （`{カテゴリ}-{3桁連番}`採番台帳・UI 工程で追記）
📋 docs/base-design/Web_API_IF一覧表.md （API 一覧）

🎯 次のアクション — SA（要件定義）工程の着手:
   /issue-init 工程: SA 案件キー: {案件キー} チケットURL: ...
```

## 制約
- 🚫 `docs/requirements/プロダクト情報.md` が無い状態で Step 1 以降を進めない（Step 0 で必ず停止・案内する）
- 🚫 既存の `specs/{案件キー}/` を上書きしない（必ず存在チェック）
- 🚫 AI（Claude）は`gh repo create`等のリポジトリ新規作成コマンドを自己判断で実行しない。リポジトリの実在確認は必須、存在しない場合は人間に作成を依頼し案件開始を停止する
- ✅ `.claude/repositories.local.md`「## スタックリポ」の記入・実在確認・clone は本スキルの必須範囲ではない（保証されたゲートは SS-Plan 工程 Step 3.5）。本スキルは記入済みの行があれば前倒しで使ってよいが、空欄のまま Step 3 以降に進んでよい
- ✅ 案件キーは確定後に変更しない（工程・ブランチ・トレースの背骨）
- 本スキルは案件レベルのみ。工程issue の起票・作業ディレクトリ作成は `/issue-init`

## 関連
- 各工程の着手: `.claude/skills/issue-init/SKILL.md`
- 案件メタ テンプレ: `specs/templates/案件-meta.md`
- プロダクト情報 テンプレ: `docs/templates/プロダクト情報.md`
- リポジトリ情報（個人ローカル）テンプレ: `.claude/repositories.local.md.example`

## 作業指示
$ARGUMENTS
