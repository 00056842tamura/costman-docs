# `.claude/scripts/gh/` — GitHub（gh CLI）連動ヘルパー

新ワークフローの **リポジトリ・issue・ブランチ運用／工程ゲート PR／工数集計**を `gh` で自動化する薄いヘルパー群。
cross-phase の中間物（discussion-log・ADR・plan.md・詳細設計WB 等）は `specs/`（SS 以降は `{スタック}/specs/`）として通常の git 管理対象・PR 対象であり、専用の添付/DL ヘルパーは持たない（`git push`/`git pull`/`git show` で受け渡す）。
規約の正本は `.claude/rules/github-ops.md`。

> 背景意図: **チケット（issue）単位で別の作業者が回せる**こと。
> 各スクリプトは **`DRY_RUN=1`** を付けると変更系コマンドを実行せず表示のみ（安全確認用）。
> 前提: `gh auth login` 済み（またはヘッドレスは `GH_TOKEN`）。最小権限 `repo`。

## スクリプト

| スクリプト | 役割 | 主な引数 |
|---|---|---|
| `case-bootstrap.sh` | 案件 Milestone・ラベル・統合ブランチ `feature/{案件キー}` を各リポに用意 | `--case` `--case-repo` `--stack-repo`(複数) `[--base develop]` |
| `issue-open.sh` | 工程 issue を起票（＋任意で作業ブランチ作成） | `--repo --case --phase --title [--feature --area --body/-file --create-branch --reopen-suffix]` |
| `subissue-bulk.sh` | 計画(plan.md)の棚卸しから sub-issue を一括「先行起票」（blocked-by 転記・ブランチは着手時） | `--case --phase {ss|pg|pt} --tsv [--repo-default --plan --plan-issue --update]` |
| `pr-open.sh` | **全工程共通の工程ゲート PR 作成**（`gh pr create`・マージは人間） | `--repo --case --phase {sa\|ui\|ssplan\|ss\|pgplan\|pg\|ptplan\|pt} [--feature] --title [--stacks --issue --draft]` |
| ~~`usage-aggregate.sh`~~ | ⛔ 廃止。`/usage-report` スキル（`.claude/scripts/usage_report.py`）に置き換え済み | — |
| `template-sync.sh` | ①機構レイヤを②③へ**取り込み/同期**（プロダクト成果物不可侵・`CLAUDE.md`/`.gitignore` は差分報告・任意 PR） | `--template {git\|path} [--target --branch --repo --create-pr --mirror]` |

## 典型フロー

```bash
# 1) 案件開始（/case-init から呼ぶ）
.claude/scripts/gh/case-bootstrap.sh --case inventory-2026-001 \
  --case-repo myorg/inventory-case --stack-repo myorg/inventory-bs --stack-repo myorg/inventory-us-api

# 2) 工程 issue 起票＋ブランチ（/issue-init から呼ぶ）
.claude/scripts/gh/issue-open.sh --repo myorg/inventory-case --case inventory-2026-001 \
  --phase sa --title "要件定義" --create-branch

# 3) sub-issue 一括先行起票（docs-to-pr の PR マージ後）
#    plan.md の棚卸しを TSV（機能ID<tab>area<tab>owner/repo<tab>タイトル<tab>blocked-by）に出力して渡す
.claude/scripts/gh/subissue-bulk.sh --case inventory-2026-001 --phase ss --tsv /tmp/ss-subissues.tsv \
  --plan bs/specs/inventory-2026-001/detail-design-plan/20/plan.md --plan-issue https://github.com/myorg/inventory-bs/pull/21

# 4) cross-phase の受け渡し（push・PR マージのみ。専用の添付/DL は無い／github-ops §3-B）
# WB（詳細設計WB）は SS 工程の PR で `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` に push され、
# 統合ブランチ feature/{案件キー} にマージ済みになった時点で PG のブランチから直接読める。
# 実装対象クラス一覧・テストシナリオ・テーブル定義書は docs/base-design/ の正式成果物のため同様に直接読む。

# 5) 工程ゲート PR（全工程共通・docs-to-pr から呼ぶ・マージは人間）
.claude/scripts/gh/pr-open.sh --repo myorg/inventory-bs --case inventory-2026-001 --phase pg --feature bs-001 --title "在庫登録 実装" --stacks bs --issue 34

# 6) 工数集計（/usage-report スキルで実行）
python3 .claude/scripts/usage_report.py --case inventory-2026-001 --out docs/changelog/usage-inventory-2026-001.md
```

## 実装状況
- ✅ gh 連動（①テンプレ取り込み/同期〔`template-sync.sh`／初回 `gh repo create --template`〕・②issue/先行起票・③specs push・PR・④工数集計・⑤PR）は**全て実装済み**。
- 初回リポ生成: `gh repo create {案件リポ} --template {LLM機構テンプレリポ}` → `template-sync.sh` で機構を最新化 → `/case-init`。
