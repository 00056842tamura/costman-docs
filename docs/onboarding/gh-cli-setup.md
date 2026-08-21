# gh CLI セットアップ手順書

本書は、gh CLI（GitHub CLI）を使い始めるための手順書です。  
gh CLI の前提知識がなくても、この手順を上から順に実行すれば動作確認まで完了できます。

> **本書の前提:**  
> 現在の作業リポジトリ（`Test_AI_driven_development`）は資材の一時置き場であり、実運用リポジトリではありません。  
> 本書の手順では **仮のテスト用リポジトリ** を使って仕組みの動作確認を行います。  
> 実運用時は `{①テンプレートリポ}` を正式な ① LLM機構テンプレートリポジトリに読み替えてください。

---

## 1. gh CLI とは何か・なぜ必要か

**gh CLI** は GitHub の操作（issue 作成・PR 作成・リポジトリ操作など）をターミナルのコマンドで行うツールです。

このプロジェクトでは以下の操作を gh CLI で自動化しています。

| 操作 | gh CLI がないと |
|---|---|
| Milestone（案件）の作成 | GitHub の画面で手動作成が必要 |
| issue の起票・ラベル付け | 画面で手動 |
| 統合ブランチの作成 | 手動 |
| 案件リポ・スタックリポの初期化 | 手動でのコピー作業 |
| cross-phase 中間物の添付・DL | 手動でのファイル操作 |
| 工程ゲート PR の作成 | 手動 |

スキル（`/case-init`・`/issue-init` 等）の内部では `.claude/scripts/gh/` のヘルパースクリプトが gh CLI を呼び出しています。**gh CLI がなければスキルが正常動作しません。**

---

## 2. 前提として必要なもの

### 2.1 GitHub アカウント

- 個人アカウントまたは組織アカウントでログインできる状態
- 対象リポジトリへの **write 権限以上** が必要（issue・PR・ブランチを作成するため）

### 2.2 必要なアクセス権限（スコープ）

gh CLI の認証時に付与するスコープは **`repo` のみ** で十分です。

| スコープ | 内容 | 必要か |
|---|---|---|
| `repo` | リポジトリ・issue・PR・Milestone の読み書き | **必須** |
| `read:org` | 組織情報の読み取り | 組織リポを使う場合のみ |
| `admin` 等 | 管理者権限 | **不要・付与しない** |

> **社内 SSO を使っている場合**: PAT（Personal Access Token）を作成後、SSO 認可の追加手順が必要です（後述）。

### 2.3 リポジトリ構成の理解

この仕組みは **3 リポジトリ構成** で動作します。

```
① LLM機構テンプレート  ← 規約・スキル・フック等の機構資材を管理する正式テンプレートリポジトリ
② 案件対応リポ          ← ①からテンプレート生成する（プロダクトにつき1つ・使い回し）
③ スタックリポ          ← 手動で空リポを作る（bs・us-api・frontend 等・スタックごとに1つ・使い回し）
```

- ① は実運用では専用の GitHub リポジトリとして管理する。**現在の作業リポジトリ（`Test_AI_driven_development`）は資材の一時置き場であり ① そのものではない。**
- ② は `.claude/`（規約・スキル・フック）を ① から引き継ぐ。**プロダクト開始時に1回だけ作成し、以後は同じリポジトリを使い続ける。** 案件（Milestone）が増えても ② リポジトリは新たに作らない。
- ③ は `.claude/` を持たず、ローカルで ② の配下に clone して使う
- ローカル開発は **② をチェックアウトしたディレクトリで `claude` を起動** する

> **案件追加時の操作:**  
> 2案件目以降は `gh repo create` を実行しない。`/case-init`（内部で `case-bootstrap.sh`）を既存の ②③ リポに対して実行し、新しい Milestone・ラベル・統合ブランチを追加するだけ。

> **テスト時の方針:**  
> 本手順書では ① として **テスト用の仮リポジトリ** を作成して動作確認します。  
> `{①テンプレートリポ}` というプレースホルダーは、実施者が用意したテスト用 ① リポ名に読み替えてください。

---

## 3. gh CLI インストール確認

```bash
gh --version
```

`gh version 2.x.x` が表示されれば OK（インストール済み）。  
表示されない場合は https://cli.github.com/ からインストールしてください。

---

## 4. gh CLI 認証（ログイン）

### 手順

ターミナルで以下を実行します。

```bash
gh auth login
```

対話形式で以下を選択します。

```
? Where do you use GitHub?
> GitHub.com          ← これを選択

? What is your preferred protocol for Git operations on this host?
> HTTPS               ← これを選択

? Authenticate Git with your GitHub credentials?
> Yes                 ← Yes

? How would you like to authenticate GitHub CLI?
> Login with a web browser    ← これを選択
```

ブラウザが開くので、GitHub アカウントでログインして認証を完了させます。

> **注意（社内 SSO）**: 組織で SSO が有効な場合、ブラウザ認証後に SSO の追加認可画面が表示されます。組織名の横にある「Authorize」をクリックしてください。

### 認証確認

```bash
gh auth status
```

以下のような出力が出れば成功です。

```
github.com
  ✓ Logged in to github.com account {あなたのユーザー名} (...)
  - Active account: true
  - Git operations protocol: https
  - Token: gho_****
  - Token scopes: 'gist', 'read:org', 'repo', 'workflow'
```

`repo` がスコープに含まれていれば OK。

---

## 5. 接続確認（テスト用リポジトリで動作確認）

認証後にアクセス権のあるリポジトリで疎通を確認します。  
`{owner}/{repo}` は自分がアクセスできる任意のリポジトリ名に置き換えてください。

```bash
# リポジトリ情報が取れるか確認
gh repo view {owner}/{repo}

# issue 一覧（空でも OK）
gh issue list --repo {owner}/{repo}
```

エラーなく応答が返れば、gh CLI の基本動作は OK です。

---

## 6. 3 リポジトリの用意

### 6.0 （テスト時のみ）① テスト用テンプレートリポジトリの準備

実運用では ① は正式な GitHub リポジトリとして別途管理します。  
**テスト目的で動作確認する場合は、現在の作業リポジトリの内容を ① の代わりとして使います。**

```bash
# 現在の作業リポジトリを ① テスト用テンプレートとして GitHub に push 済みであること
# （既に ai-tech-governance/Test_AI_driven_development として push されていれば利用可能）
# 以降の {①テンプレートリポ} はこのリポジトリ名に読み替える
```

> **実運用時**: 正式な ① テンプレートリポジトリを `gh repo create` 等で用意し、  
> 本資材（`.claude/`・`README.md` 等）を push したものを ① として扱う。

---

### 6.1 ② 案件対応リポの作成（プロダクト開始時・1回のみ）

> ⚠️ この`gh repo create`は人間が手動で実行する。以降の`/case-init`実行時にAIが自動でリポジトリを作成することはない。

**プロダクト開始時に1回だけ実行します。2案件目以降はこの手順は不要です（§6.3 で Milestone を追加するだけ）。**

```bash
gh repo create {案件リポ名} \
  --template {①テンプレートリポ} \
  --private \
  --clone
```

> - `{案件リポ名}` の例: `my-org/test-case`（テスト時）、`my-org/inventory-case`（実運用時）
> - `{①テンプレートリポ}` の例: `ai-tech-governance/Test_AI_driven_development`（テスト時）
> - `--private`: 非公開リポジトリとして作成（公開でよければ `--public`）
> - `--clone`: 作成後にローカルに clone まで行う

これにより `.claude/`（規約・スキル・フック・ヘルパースクリプト）が ① からコピーされます。

### 6.2 ③ スタックリポの作成（スタック追加時・1回のみ）

> ⚠️ この`gh repo create`は人間が手動で実行する。以降の`/case-init`実行時にAIが自動でリポジトリを作成することはない。

**スタック（bs・frontend 等）を追加するときに1回だけ実行します。** 以後は同じリポジトリを使い続けます。スタックリポは **空のリポジトリ** を作成します（テンプレートは使いません）。

```bash
# BS スタック用（テスト時は test-bs 等の仮名でも可）
gh repo create {owner}/test-bs --private --clone

# US-API スタック用（必要に応じて）
gh repo create {owner}/test-us-api --private --clone
```

各スタックリポで `develop` ブランチを作成しておきます。

```bash
cd inventory-bs
git switch -c develop
git push -u origin develop
cd ..
```

### 6.3 案件のブートストラップ（Milestone・ラベル・統合ブランチを一括作成）

② と ③ のリポジトリが揃ったら、以下のスクリプトで各リポに **Milestone・ラベル・統合ブランチ** を一括作成します。

```bash
# ② 案件リポのルートで実行
cd {案件リポ}

bash .claude/scripts/gh/case-bootstrap.sh \
  --case "{案件キー}" \
  --case-repo "{owner}/{案件リポ名}" \
  --stack-repo "{owner}/{スタックリポ名}"
```

> - `{案件キー}` の形式: `{プロダクト略称}-{YYYY}-{連番3桁}`（例: `test-2026-001`・テスト時は任意の名前で可）
> - `--stack-repo` は複数指定可（スタック数だけ繰り返す）
> - 実行前に確認したい場合: `DRY_RUN=1 bash .claude/scripts/gh/case-bootstrap.sh ...`（変更なしで表示のみ）
>
> **Tips**: ②③のリポジトリは以後使い回すため、`.claude/repositories.local.md.example` をコピーして `.claude/repositories.local.md`（個人ローカル・`.gitignore` 対象）に owner/repo を記録しておくと、2案件目以降 `/case-init` で毎回入力し直さずに済みます（`.claude/rules/github-ops.md` §1）。

完了すると各リポに以下が作成されます。

| 作成物 | 内容 |
|---|---|
| Milestone | `{案件キー}` という名前の Milestone |
| ラベル | `工程:sa`〜`工程:pt`・`type:phase`・`area:bs` 等 |
| 統合ブランチ | `feature/{案件キー}`（develop から分岐） |

---

## 7. 動作確認チェックリスト

```
[ ] gh auth status で "Logged in" が表示される
[ ] gh repo view {リポ名} でリポジトリ情報が取れる
[ ] gh issue list --repo {リポ名} がエラーにならない
[ ] ② 案件リポに .claude/ ディレクトリが存在する
[ ] ② 案件リポのルートで claude を起動すると /case-init が使える
[ ] case-bootstrap.sh 実行後、GitHub 画面で Milestone と統合ブランチが見える
```

---

## 8. よくあるエラーと対処

| エラー | 原因 | 対処 |
|---|---|---|
| `You are not logged into any GitHub hosts` | 未認証 | `gh auth login` を実行 |
| `HTTP 403: Resource not accessible by personal access token` | repo スコープが不足 | `gh auth refresh -s repo` でスコープ追加 |
| `HTTP 404: Not Found` | リポジトリが存在しないか権限不足 | リポジトリ名と権限を確認 |
| SSO 認証エラー | 組織の SSO 認可が未完了 | `gh auth login` 後にブラウザで「Authorize」をクリック |
| `base 'develop' が無い` | スタックリポに develop ブランチがない | `git switch -c develop && git push -u origin develop` |

---

## 9. 今後の機構同期（① の更新を ② に取り込む）

① テンプレートリポジトリが更新されたとき、② 案件リポに規約・スキル・フックの最新版を取り込みます。

```bash
cd {案件リポ}

bash .claude/scripts/gh/template-sync.sh \
  --template {①テンプレートリポ} \
  --target . \
  --create-pr
```

- **同期される**: `.claude/rules/`・`.claude/skills/`・`.claude/hooks/`・`README.md` 等（機構レイヤ）
- **同期されない**: `docs/requirements/`・`docs/base-design/`・`specs/` 等（プロダクト成果物）
- **差分報告のみ**: `CLAUDE.md`・`.gitignore`（手動マージ判断）

---

## 関連ドキュメント

- `README.md` — 日常開発フロー
- `MIGRATION_GUIDE.md` — 新規プロダクト立ち上げ手順
- `.claude/rules/github-ops.md` — GitHub 運用規約（詳細）
- `.claude/scripts/gh/README.md` — ヘルパースクリプト一覧
