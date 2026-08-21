# オーケストレーター定義: 工程ゲート PR 作成フロー

## 目的
工程の成果物作成完了後に、コミット・プッシュ・PR メッセージ生成・PR 作成を行う。**工程ごとに成果物・確認観点を出し分ける**。

## 入力
- 案件キー / 工程（SA / UI / SS-Plan / SS / PG-Plan / PG / PT-Plan / PT）/ issue-id /（area工程なら）機能ID

## 共通手順

### Step 0: バックログ解消ゲート（全工程共通・必須）
工程内で発生した「作業バックログ」（サブエージェント等が暫定判断のままTODO化した未確定情報）を、次工程へ持ち越す前に必ずユーザーと合意する。書式は `.claude/rules/github-ops.md` §3-B「バックログ記録形式」を参照（3状態: 未解決 `- [ ]` ／解消済み `- [x]（解消: ...）` ／承認済み持ち越し `- [ ]（承認済み持ち越し→...）`）。

- **対象**:
  - discussion-log.md（`specs/{案件キー}/{工程dir}/{issue_id}/discussion-log.md`。area別工程〔SS/PG/PT〕は `{スタック}/specs/{案件キー}/{工程dir}/{issue_id}_{機能ID}/discussion-log.md`）の「## 未解決事項・TODO」節。
  - 同スコープ配下の `adr/ADR-{工程}-{n}*.md` 全件の「## 未確定事項（バックログ）」節（**SS-Plan/PG-Plan/PT-Plan は対象外**。ADR を起票しないため）。
  - 同 discussion-log.md の「## Phase 1」「## Phase 2」セクション（壁打ち系工程のみ）。意思決定はされているが記録に反映されていない、という「## 未解決事項・TODO」とは別種の欠落を検知する。
- **手順**:
  1. 対象ファイル群から `- [ ]` で始まる行（書式1のみ。書式3〔承認済み持ち越し〕は対象から除く）を全件抽出する。テンプレートの未記入プレースホルダー（`<!-- -->` のみの行）は対象外とする。
  2. discussion-log.md の「## Phase 1」「## Phase 2」セクション配下が、テンプレートのプレースホルダー（`<!-- ... -->` のみの行・セル）のまま残っていないかを確認する。1行でも実内容が記入されていれば当該セクションは対象外とする（判定粒度: セクション単位。表の全行が `<!-- -->` のみの場合のみ「未記入」とみなす）。
  3. 抽出0件（手順1・2とも）なら Step 0 完了・Step 1 へ進む。
  4. 1件以上ある場合、対応方針を選択肢としてユーザーに提示する**前に**、`.claude/rules/cross-process-consistency.md`「手順1: インラインフィックス判定」の3条件を必ず自己チェックする（AIが自己判断で3条件の成立を省略しない）。3条件のいずれかが不成立の場合は (a)/(b) を選択肢として提示せず、直接 (c) へ誘導する。
  5. **1項目ずつ**ユーザーへ提示し、以下いずれかの結論を得る（AI が自己判断で(b)を選択することは禁止）:
     - **(a) 工程内で解消**: 追加の壁打ち・調査の上で決定し、当該行を書式2（`- [x] ...（解消: ...）`）に更新する。決定内容が既存 ADR への追加条項に相当する場合は新規 ADR（`ADR-{工程}-{n+1}`）を起票し関連付ける。Phase1/2の未記入が対象の場合は、対象の意思決定内容をセクションへ記入することで解消する。
     - **(b) ユーザー承認済み持ち越し**: ユーザーが明示的に次工程への持ち越しを承認した場合のみ、当該行を書式3に更新し、`specs/{案件キー}/meta.md`「バックログ持ち越し管理表」に1行追加する。
     - **(c) 手戻りとして処理**: 解消のために**別工程が管理する正本ドキュメント**（`docs/` 配下・他issueの管理範囲）への変更が必要な場合、(a)/(b) を選ばず `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」（手戻りID採番→影響工程に応じたissue運用→ブランチ作成→`rework-impact-report.md`作成）に直接接続する。手順4のインラインフィックス自己チェックで3条件が不成立と判定された場合、原則この(c)に該当する。
  6. 全項目が (a)・(b)・(c) のいずれかで記録済み（(c)の場合は手戻りワークフローへの接続が完了した状態）になるまで Step 1 へ進まない。
- **次工程への引き継ぎ**: (b) で記録した持ち越しは、次工程の sub-issue 起票（`subissue-bulk.sh`／`issue-open.sh`）時に、issue 本文「上流成果物（着手前に読む）」節へ `案件-meta.md`「バックログ持ち越し管理表」の該当行への参照を追記する（`.claude/rules/github-ops.md` §3 のissue本文スキーマ）。
- 補足: バックログが無い工程issueでは「0件確認・即Step 1へ」という短い確認で完了する。本ゲートは手戻り（`.claude/rules/product-rules.md` 手戻り統一ワークフロー）とは別概念であり、一度もPRを作っていない工程内での合意形成に限定する。ただし手順4・5の(c)に該当する場合は、Step 0 自体が手戻りワークフローへの入口として機能する。

### Step 1: 変更ファイルの最終確認
- 変更ファイル一覧を表示する。
- 影響スタックを工程issue meta（`specs/{案件キー}/{工程}/{issue_id}/meta.md`）から確認する。
- **コードを伴う工程（SS/PG/PT）のみ**: パッケージ間依存チェック・Swagger 整合を実施する。
  **SA/UI など docs のみの工程ではスキップ**する。

### Step 2: コミット
```
git add -A
git commit -m "feat({案件キー}-{工程}[-{機能ID}]): {簡潔な説明}"
# 例: feat(inventory-2026-001-sa): ユーザー登録の要件定義
```

### Step 3: プッシュ
```
git push origin feature/{案件キー}-{工程}[-{機能ID}]
```

### Step 4: PR メッセージ生成（工程で出し分け）
**共通セクション**: 概要 / 影響スタック / 成果物（ドキュメント更新一覧）/ 設計の意思決定（discussion-log・ADR から）/ マージ先（`feature/{案件キー}`）。対応issue（issue-id・案件キー・Milestone）は下記テンプレートの「## 工程 / 案件」ブロックに `{{ISSUE_LINE}}`・`{{CASE}}` として統合済みのため、別節での重複記載は不要。

> **重要**: `specs/`（discussion-log・ADR・plan 等）は `docs/` などの正式成果物と同様に **push 対象**。**PR には `docs/`（or `{スタック}/docs/`・`{スタック}/src/`）成果物と `specs/`（or `{スタック}/specs/`）の検討記録を同じ PR に含める**（Step 2 の `git add -A` で両方まとめてコミットされる）。計画工程（SS-Plan 等）も他工程と同じ **PR** ゲートで進める（旧: issue ゲート／添付＋コメント承認は廃止）。実装対象クラス一覧・テストシナリオ・テーブル定義書は `docs/base-design/` の正式成果物のため PR 対象。

**工程別の節:**
- **SA / UI（上流・docs のみ）**: コード・テスト結果・Swagger・パッケージ依存の節は **N/A（省略）**。成果物は SA=`docs/requirements/`、UI=`docs/base-design/`（＋ specs/ の discussion-log・ADR）。
- **SS-Plan / PG-Plan / PT-Plan（計画）**: 成果物は `specs/{案件キー}/...`（or `{スタック}/specs/{案件キー}/...`）の `plan.md`・discussion-log のみ。コード/テスト・Swagger 節は N/A。
- **SS（詳細設計・スタックリポ）**: `{スタック}/docs/detail-design/`（テーブル定義書・外部IF定義書・batch 設計）＋ `{スタック}/specs/{案件キー}/detail-design/...`（WB・ADR-SS-{n}・レビューレポート）を同じ PR に含める。コード/テスト・Swagger 節は SS では N/A。
- **PG（実装・スタックリポ）**: コード `{スタック}/src/main/` のみ（docs生成なし。プログラム仕様書等はSS工程で生成済み）＋ `{スタック}/specs/{案件キー}/implementation/...`（作業メモ・ADR・レビューレポート）を PR。テスト結果は PT（後）。
- **PT（単体テスト・スタックリポ）**: テストコード `{スタック}/src/test/`＋テストdocs `{スタック}/docs/unit-test/`（テスト仕様書・テスト計画書・機能要件対比表）＋ `{スタック}/specs/{案件キー}/unit-test/...`（作業メモ・consistency レポート）を PR。テスト結果（人間確認済み）を本文に。

**本文の見出し構造は `.github/PULL_REQUEST_TEMPLATE/{phase}.md` を唯一の真実の源とする**（`sa.md`/`ui.md`/`ss-plan.md`/`ss.md`/`pg-plan.md`/`pg.md`/`pt-plan.md`/`pt.md`。工程→ファイル名は `pr-template.sh` の `pr_template_name()` と同一マッピング）。`--body-file` で本文を渡す際は、対象工程のテンプレートファイルをコピーし、`{{CASE}}`・`{{ISSUE_LINE}}` 等の機械的プレースホルダー相当部分と、「## 概要」「## 成果物」「## 設計の意思決定」（または「## 検討の記録」）等の自由記述セクションを、本工程で実際に生成した成果物の内容に基づいて埋める。**見出し自体（特に「## チェック」の各項目）を削除・改変してはならない**（フック警告解消・バックログ解消・レビュー実施記録確認等の完了ゲート項目が本文に反映されなくなるため）。

### Step 5: PR 作成（`pr-open.sh`・全工程共通）
ベースブランチ = `feature/{案件キー}`（SS・PG-Plan・PG・PT-Plan・PT は**スタックリポ**の同名ブランチ）。**全工程（SA/UI/SS-Plan/SS/PG-Plan/PG/PT-Plan/PT）**は `gh pr create` で工程ゲート PR を作成する:
```bash
.claude/scripts/gh/pr-open.sh --repo {repo} --case {案件キー} --phase {sa|ui|ssplan|ss|pgplan|pg|ptplan|pt} [--feature bs-001] \
  --title "{要約}" --stacks "{影響スタックCSV}" --issue {N}
```
> 本文は `.github/PULL_REQUEST_TEMPLATE/{phase}.md`（工程別8ファイル）ベース＋影響スタック＋`Closes #{N}` で自動生成。**マージは人間レビュー後**（`develop→main` は別途・`product-rules`）。gh 未導入なら手動 PR（`github-ops.md §5`）。

### Step 6: 工数ログ（工程完了時・集計の入力源）
`llm-usage.jsonl`（`specs/{案件キー}/{工程dir}/{issue_id}/` or `{スタック}/specs/{案件キー}/{工程dir}/{issue_id}[_{機能ID}]/`）は Step 2 の `git add -A` で他の specs/ 成果物と一緒に本 PR に含まれる。追加の添付作業は不要。
> Milestone 集計は `/usage-report` スキル（`.claude/scripts/usage_report.py`・案件リポ＋各スタックリポの specs/ を横断集計・jq 不要・工程別/手戻り別集計）。

---

## 計画工程（SS-Plan / PG-Plan / PT-Plan）の sub-issue 先行起票（PR マージ後）

> 計画工程も Step 1〜5 の通常 PR フローで進める（`plan.md`・discussion-log を specs/ として push → PR → レビュー → マージ）。PR マージ後に以下の手順で sub-issue を先行起票する。

**手順（計画工程共通・SS-Plan / PG-Plan / PT-Plan）:**
1. 計画工程 PR がマージされたことを確認する（`feature/{案件キー}` へマージ済み）。
2. マージ済みの `plan.md` の棚卸しを **TSV**（`機能ID<tab>area<tab>owner/repo<tab>タイトル<tab>blocked-by`）に書き出し、**sub-issue を各スタックリポへ一括先行起票**する:
```bash
.claude/scripts/gh/subissue-bulk.sh --case {案件キー} --phase {ss|pg|pt} --tsv {棚卸し.tsv} \
  --plan {マージ済み plan.md のパス} --plan-issue {計画工程 PR の URL}
```
   （`[SS-{機能ID}]`/`[PG-{機能ID}]`/`[PT-{機能ID}]`・機能ID単位・同一 Milestone・`blocked-by` 転記。ブランチは着手時に `/issue-init` が作成）
3. 案件 `meta.md` の対応表へ **当該計画工程行＋先行起票した sub-issue 群**を登録する。

**確認事項（レビュワー向け・PR レビュー時）:**
- [ ] 棚卸しが **機能ID単位**（`docs/base-design/機能一覧.md` の全機能ID）で漏れなく列挙されているか（実装対象クラス一覧と整合）
- [ ] 依存（`blocked-by`）・並走可否が妥当か（スタック間 I/F の確定順）
- [ ] 優先順位・着手順が妥当か

## バックログ解消フォローアップPR（工程完了後に発見された記録欠落・同期漏れの修正）

Step 0（上記）は「これから工程ゲートPRを作る工程issue」のバックログを対象とするが、**本体（フェーズゲート）PRが既にマージされた後**に、discussion-log・ADR間の同期漏れ（例: discussion-logでは解消済みだがADR側のチェックボックスが未同期）や記録の欠落（例: Phase1/2未記入の見落とし）が別途発覚することがある。この場合の対応（フォローアップPR）は以下の手順に従う。都度その場で `gh pr create` の本文を即席で組み立てず、必ず本手順を経由する。

1. **対象issueの本体PRの状態を確認する**: `gh pr view {本体PR番号} --json mergedAt` で本体PRがマージ済みかどうかを確認する。
2. **手戻り判定**: 本体PRがマージ済みの場合、`.claude/rules/cross-process-consistency.md`「本体PRマージ後に発覚した場合の手戻り判定」の基準に従い、手戻り（軽量・本格を問わず統一的に手戻りID採番）として扱う。未マージの場合は通常のStep 0（(a)/(b)/(c)）で処理する。
3. **PR本文の「## 工程 / 案件」ブロックに対象issue番号を必須で含める**: `{{ISSUE_LINE}}` 相当部分に、対象issue番号を本体PRと同じ書式（`Closes #{N}（[{タイトル}] task・{スタック}リポ）`）で明記する。`pr-open.sh --issue {N}` の利用を推奨する（`Closes #N` の自動生成・一貫したラベル付与の恩恵を受けるため）。

## 制約
- PR メッセージの影響スタックを空欄のままにしない
- 工程に応じて不要な節（SA/UI のコード・テスト節、計画工程のコード/テスト/Swagger 節）は明示的に **N/A** とし、誤解を生む空欄を残さない
- 案件統合ブランチ（`feature/{案件キー}`）をベースにする（`develop`/`main` へ直接 PR しない）
- US-API の API 仕様変更がある工程では破壊的変更の有無を必ず明記する
