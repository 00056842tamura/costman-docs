---
name: stack-init
description: 新規スタック（bs/us-api/us-mpa/batch/frontend）を作成する際に、_templates/ 配下の受領資材（_received/）を確認し、実スタックディレクトリへ反映してビルド・テスト成立を確認する
---

# 新規スタック構築 — 受領資材の受入・反映・ビルド確認（新規スタック作成時）

新しいスタック（`bs/`・`us-mpa/`・`us-api/`・`batch/`・`frontend/`）を追加するときに実行する。
`_templates/{type}-template/_received/`（Reacter は `_templates/frontend-template/_received/`）に配置された
標準チーム受領資材（ワイヤーフレーム本体）を実スタックディレクトリへ反映し、ビルド・テスト成立を確認する。

> **推奨起動タイミング**: 「UI 工程完了後・SS-Plan 工程の頭（`/issue-init 工程: SS-Plan` を実行するより前）」。
> どのスタックがその案件で必要かは UI 工程（機能ID をスタック単位に分解・採番する工程）で確定するため、
> `/stack-init` はそれより前に実行する必要はない。実装者が `docs/base-design/機能一覧.md`・
> `docs/architecture/stack-dependency.md`（いずれも UI 工程で確定済み）を見て対象スタックを判断し、
> 未構築があれば `/issue-init 工程: SS-Plan` を実行する前に本スキルを実行する。
> `.claude/skills/issue-plan/SKILL.md` Step 3.5 は、この構築が完了していることを再確認する安全網であり、
> 通常はここで検出・案内が発生しない。`/case-init`（案件開始・リポジトリの `git clone` のみを行う）や
> SA 工程で実行する必要はない（`case-init` は本スキルの責務を持たない）。
> ただし、既に他案件で構築済みのスタックを再利用する場合は本スキルの再実行は不要。
>
> 受領資材そのもの（zip 展開後の実体）はリモートにpushしない（`.gitignore` の `_templates/**/_received/*` で除外。
> `_received/` ディレクトリ自体は `.gitkeep` で追跡を維持する）。常に標準チームの最新版・確定版を都度受領して用いるため。
> 前提: `_templates/{type}-template/_received/`（または `frontend-template/_received/`）に、標準チームから受領した資材が展開済みであること（未配置の場合は Step 1 で案内し停止する）。加えて Springer 系スタックの場合は、ビルド確認（Step 3 の `mvn test`）に必要な Springer フレームワークの jar/pom を、**事前にユーザーがローカル `.m2` へ登録済み**であること（本スキルはフレームワークのインストールを行わない）。

## 入力形式
```
/stack-init
スタック種別: bs   （bs / us-mpa / us-api / batch / frontend のいずれか）
```

## 実行手順

### Step 0 — 対象スタック種別の確定
- 入力の「スタック種別」を確認する（省略時はユーザーに問い合わせる）。
- スタック種別から以下を解決する。

| スタック種別 | 受入ひな形 | 実スタックディレクトリ | 種別区分 |
|---|---|---|---|
| `bs` | `_templates/bs-template/` | `bs/` | Springer |
| `us-mpa` | `_templates/us-mpa-template/` | `us-mpa/` | Springer |
| `us-api` | `_templates/us-api-template/` | `us-api/` | Springer |
| `batch` | `_templates/batch-template/` | `batch/` | Springer |
| `frontend` | `_templates/frontend-template/` | `frontend/` | Reacter |

### Step 1 — 受領資材の存在確認（着手前ゲート）
- 対象ひな形の `_received/` に、ビルド成立の最低限のファイルが存在するかを確認する。
  - Springer 系: `pom.xml` または `build.gradle`
  - Reacter: `package.json`
- **無い場合は処理を停止**し、以下を案内する。
  ```
  ⛔ {対象ひな形}/_received/ に受領資材が見つかりません。
     標準チームから受領した資材（zip）を展開し、以下に配置してから再実行してください。
     配置先: {対象ひな形}/_received/
  ```
- 存在する場合、次のステップ（実スタックへの反映）へ進む。

### Step 2 — 実スタックへの反映

**2-1. コピー実行前の確認（2件・いずれもコピー実行より前に行う）**
- **コピー元（`{対象ひな形}/_received/{受領物}/`）直下に `.git/` が存在するか確認する。** 標準チームの配布形式は資材種別によって異なり（Springer系＝zip展開・`.git` なし／Reacter系＝内部GitLabからの `git clone` 配布・`.git` あり）、`.git` を保持したまま配布される資材が存在する。存在する場合、後述の除外コピー手順（2-2）を使う。
- **実スタックディレクトリに既に `src/main/`（Reacter は `src/`）が存在する場合**は、上書きしてよいかを確認する。
  ```
  ⚠️ {実スタックディレクトリ}/src/main/ に既存の実装コードがあります。
     上書きしますか？（意図しない再構築・既存実装の破壊を防ぐための確認です）
  ```
  - 承認が得られない限り上書きしない（処理を停止する）。

**2-2. コピーの実行**
- **コピー元に `.git/` が無い場合**: 通常通り `{対象ひな形}/_received/` の内容を実スタックディレクトリ（例 `bs/`）へコピーする。
- **コピー元に `.git/` がある場合（除外コピーを行う）**: 実スタックディレクトリに既に `.git/`（工程0対応・`case-bootstrap.sh` 実行済み等で正しいリモート・ブランチ・履歴を持つ）が存在する状態で、コピー元の `.git/` を含めて再帰コピー（`cp -r`）すると、コピー先の `.git/` の内部ファイルが上書き・混在し、実スタックリポのリモート・ブランチ・履歴が破壊される。`.git` は特別扱いされないため、除外コピーを必ず行う。
  ```bash
  # Git Bash / bash 環境（tar は Git Bash 標準搭載のため追加インストール不要。rsync は本環境では利用できない場合がある）
  tar -cf - --exclude='.git' -C "{対象ひな形}/_received/{受領物}" . | tar -xf - -C "{実スタックディレクトリ}"
  ```
  ```powershell
  # PowerShell 環境（Windows 標準の robocopy を使う代替手段）
  robocopy "{対象ひな形}\_received\{受領物}" "{実スタックディレクトリ}" /E /XD .git
  ```

**2-3. コピー実行後の検証**
- **コピー先に既存の `.git`（工程0対応済み等）があった場合は、意図した状態を保っているかを必ず検証する。**
  ```bash
  git -C "{実スタックディレクトリ}" remote -v
  git -C "{実スタックディレクトリ}" log --oneline -3
  ```
  - 想定していたリモートURL・直近のコミット（工程0対応時の初期コミット等）と一致しない場合は、**即座に処理を停止**し、「トラブルシューティング」節の復旧手順をユーザーに案内する。
- `_templates/{type}-template/` 直下の既存プレースホルダー（`BUILD_TBD.md`・`CLAUDE.md` の `← TBD` マーカー等）は、コピー後は受領資材の実ファイル（`pom.xml`/`package.json` 等）で置き換わる想定。プレースホルダー自体は `_templates/` 側に残し削除しない（他スタック作成時のフォールバック説明として維持）。

### Step 3 — ビルド・テスト確認（完了ゲート）
- Springer 系: 実スタックディレクトリで `mvn test`（最低限 `mvn compile`）を実行する。
- Reacter: `frontend/` で `npm install && npm run build`（または既存のテストスクリプト）を実行する。
- **失敗した場合は処理を停止**し、エラー内容をそのまま報告する（自動リトライ・自動修正はしない）。人間が資材・設定を確認してから再実行する。

### Step 4 — 完了報告
```
✅ スタック {スタック種別} を構築しました。
📁 {実スタックディレクトリ}/     （_received/ の資材を反映済み）
🏗️ ビルド・テスト成立を確認済み（{実行コマンド}）
{Step2で.gitを除外コピーした場合のみ:}
⚠️ コピー元（{受領物}）に .git/ が含まれていたため除外してコピーしました。

🎯 次のアクション:
   - {実スタックディレクトリ}/CLAUDE.md の Springer バージョン・ベースパッケージ（← TBD マーカー）を確定してください
```

## トラブルシューティング

### `.git` 破壊が発覚した場合の復旧手順（Step 2 の検証で不一致を検知した場合）

コピー先の `.git`（リモートURL・直近コミット）が想定と一致しない場合、以下の手順で復旧する。**リモートには工程0対応で既に正しい履歴がpush済みのため、ローカル `.git/` の削除・再作成は安全である**（この根拠を、破壊的操作に対する安全機構によるブロック時の判断材料としてユーザーに示すこと）。

1. コピー先ディレクトリの `.git/` を削除する（`rm -rf {実スタックディレクトリ}/.git`）。
2. `git init` で再初期化する。
3. 破壊前に想定していたブランチ名で `git checkout -b {ブランチ名}` する。
4. 既存のリモート（`case-bootstrap.sh` 等で既にpush済みのURL）を `git remote add origin {URL}` で再アタッチする。
5. `git fetch` 後、リモートの内容とローカルの実ファイル（コピー済みのワイヤーフレーム等）を照合し、意図した状態であることを確認してから再度コミット・pushする。

## 制約
- 🚫 `_received/` に受領資材が存在しない状態で Step 2 以降を進めない（Step 1 で必ず停止・案内する）
- 🚫 ビルド・テストが失敗した状態で「完了」を報告しない（Step 3 で必ず停止する）
- 🚫 既存の実装コード（`src/main/` 等）がある実スタックディレクトリへ、確認なしに上書きしない
- 🚫 コピー元（`_received/{受領物}/`）に `.git/` が存在する場合、それを除外せずにコピーしない（実スタックの既存 `.git` を破壊するため。Step 2）
- ✅ コピー先に既存 `.git` があった場合は、コピー後に `git remote -v`・`git log --oneline -3` で意図した状態を検証する（Step 2）。不一致時は即座に停止し「トラブルシューティング」節の手順を案内する
- 🚫 `_received/` の資材内容自体をコミット・push しない（`.gitignore` 対象。`_received/` 直下の `.gitkeep` のみ追跡対象）
- ✅ `_templates/{type}-template/` 直下の既存プレースホルダー（`BUILD_TBD.md`・`CLAUDE.md` の `← TBD` マーカー）は削除しない
- 本スキルは新規スタック作成時のフローのみを対象とする。稼働中スタックへの資材バージョンアップ反映は対象外（別途手動対応）

## 関連
- 受入ひな形・新規スタック作成手順: `_templates/README.md`
- 新規スタック作成ルール: `.claude/rules/product-rules.md`「新規スタック作成」節
- 案件開始（リポジトリの `git clone` のみ・スカフォールディングは行わない）: `.claude/skills/case-init/SKILL.md`
- 起動タイミング（SS-Plan 工程の頭・`/issue-init` 実行前）: `README.md` §5.5 Step0〜0c
- 起動漏れの安全網（構築済みの再確認のみ・ブロッキングではない）: `.claude/skills/issue-plan/SKILL.md`（SS-Plan Step 3.5）
- Springer バージョン対応表: `.claude/rules/springer-version-matrix.md`

## 作業指示
$ARGUMENTS
