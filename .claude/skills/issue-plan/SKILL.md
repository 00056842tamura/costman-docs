---
name: issue-plan
description: 計画工程（SS-Plan/PG-Plan/PT-Plan）の上流成果物を読み、次工程の sub-issue 棚卸し（機能ID・スタックはUI工程で確定済み）を plan.md に生成する。工程パラメータで出し分け。
---

# 計画工程 計画作成スキル（SS-Plan 詳細設計計画 / PG-Plan 実装計画 / PT-Plan 単体テスト計画）

上流成果物（`docs/`・`{スタック}/docs/`・`{スタック}/src/`）と検討メモを読み、計画工程の `plan.md` に
**次工程の sub-issue 棚卸し（粒度＝機能ID）**を生成する。**工程パラメータ（SS-Plan / PG-Plan / PT-Plan）**で入出力を出し分ける。

> 計画工程共通の生成器。**SS-Plan / PG-Plan / PT-Plan に対応**。
> 実行リポ: **SS-Plan＝案件リポ**（案件レベル棚卸し）／**PG-Plan・PT-Plan＝スタックリポ・スタック単位**（SS から降りる計画。クロススタック順序は SS-Plan 継承・詳細は `product-rules.md`）。
> **成果物 `plan.md` は specs（push・PR 対象）**。承認は他工程と同じ **PR レビュー**。**ADR は起票しない**。
>
> **機能IDはすでにスタック単位（UI工程で要件ID×スタック×API/画面/ジョブに分解・採番済み）**。
> SS-Plan は「スタックへの分解」を再度行わず、`docs/base-design/機能一覧.md` の各機能IDをそのまま sub-issue 化する（棚卸しのみ）。

## 工程パラメータ
| 工程 | 入力（主） | 出力先 | sub-issue（棚卸し） | 特記 |
|---|---|---|---|---|
| **SS-Plan** | `docs/base-design/機能一覧.md`（機能ID確定済み）＋`Web_API_IF定義書_{機能ID}`＋`テーブル一覧.md`＋`テーブル定義書_{テーブルID}_{テーブル名}`＋`実装対象クラス一覧.md` | `specs/{案件キー}/detail-design-plan/{issue_id}/plan.md`（案件リポ） | `[SS-{機能ID}]`（作成設計書） | 詳細設計の棚卸し（スタック分解はUI済み） |
| **PG-Plan** | **SS 成果物 `{スタック}/docs/detail-design/`**（backend/batch: 外部IF定義書・プログラム仕様書・メッセージ一覧／frontend: コンポーネント仕様書・画面アクション遷移図・メッセージ一覧）＋`docs/base-design/機能一覧.md`・`実装対象クラス一覧.md` | `{スタック}/specs/{案件キー}/implementation-plan/{issue_id}/plan.md`（スタックリポ） | `[PG-{機能ID}]`（実装クラス） | **実装順序・ビルド通過依存**を付ける |
| **PT-Plan** | **PG 実装コード `{スタック}/src/main/`**＋`docs/base-design/テストシナリオ.md`（受け入れ条件）＋SS 詳細設計 | `{スタック}/specs/{案件キー}/unit-test-plan/{issue_id}/plan.md`（スタックリポ） | `[PT-{機能ID}]`（テスト対象・観点） | ホワイト/ブラックボックスの対象・受け入れ条件番号 |

## 入力形式
```
/issue-plan
工程: PT-Plan            # SS-Plan / PG-Plan / PT-Plan
案件キー: inventory-2026-001
issue-id: {issue_id}     # 当該計画工程の issue-id
対象機能ID: bs-001, bs-002    # 省略時は機能一覧・上流成果物から確認
```

---

## 実行手順

### Step 1 — 入力チェック
- [ ] `specs/{案件キー}/{計画工程dir}/{issue_id}/`（SS-Plan・案件リポ）または `{スタック}/specs/{案件キー}/{計画工程dir}/{issue_id}/`（PG-Plan/PT-Plan・スタックリポ）（`/issue-init 工程: {計画工程}` 済み）が存在するか確認
  （SS-Plan=`detail-design-plan` / PG-Plan=`implementation-plan` / PT-Plan=`unit-test-plan`）。無ければ `/issue-init` を促す
- [ ] `plan.md`（雛形）が存在するか確認（無ければ `specs/templates/plan.md` からコピー）
- [ ] 工程に応じた上流成果物が存在するか確認（PG-Plan は **SS 成果物**、PT-Plan は **PG 実装コード**が必須）

### Step 2 — ドキュメント読み込み（工程で出し分け）
共通: `docs/base-design/機能一覧.md`（機能ID・要件ID・スタック確定済み）・`docs/base-design/実装対象クラス一覧.md`・`specs/{案件キー}/meta.md`・対象スタックの `CLAUDE.md`
- **SS-Plan**: 基本設計（機能一覧/機能概要/Web_API_IF定義書/シーケンス図/テーブル一覧・テーブル定義書_{テーブルID}_{テーブル名}/ロバストネス図）を中心に詳細設計対象を洗い出す
- **PG-Plan**: **SS 成果物 `{スタック}/docs/detail-design/`**（backend/batch: 外部IF定義書・プログラム仕様書・WB／frontend: コンポーネント仕様書・画面アクション遷移図・WB）＋ `docs/base-design/テーブル定義書_{テーブルID}_{テーブル名}.md`（実装参考）を中心に実装クラスを洗い出す
- **PT-Plan**: **PG 実装コード `{スタック}/src/main/`** ＋ `docs/base-design/テストシナリオ.md`（受け入れ条件カバレッジ）を中心にテスト対象・観点を洗い出す

### Step 3 — sub-issue の棚卸し（粒度＝機能ID）
`docs/base-design/機能一覧.md` の各機能ID（すでにスタック単位に分解済み）を 1 sub-issue として漏れなく列挙する。
- **SS-Plan**: 作成する設計書（外部IF定義書 等。テーブル定義書は UI 工程の既存正本を参照のみ）・依存・優先
- **PG-Plan**: **実装クラス**（Controller/Service/Repository/Mapper/Model/Request/View/Flyway/SecurityConfig/Frontend/Runner 等）・成果物（`{スタック}/src/main/` のみ。プログラム仕様書等はSS工程で生成済みのためPGではdocs生成なし）・**実装順序・依存（blocked-by）**・優先
- **PT-Plan**: **テスト対象**（クラス・メソッド）と**テスト観点**（ホワイトボックス＝Service/Repository 単体／ブラックボックス＝Controller API）・対応する `docs/base-design/テストシナリオ.md` の受け入れ条件番号・成果物（`{スタック}/src/test/`＋`{スタック}/docs/unit-test/`＝PT 工程で作る）

> **スタックへの分解は UI 工程で完了済み**（機能ID自体が1スタックに閉じた単位）。SS-Plan はスタック分解を再度行わない。未棚卸しの機能IDが残った状態で次に進まない。

### Step 3.5 — スタック構築状況の確認（SS-Plan のみ・新規スタック確認ゲート）
> `/stack-init`（`.claude/skills/stack-init/SKILL.md`）の推奨起動タイミングは「UI 工程完了後・SS-Plan 工程の頭（`/issue-init 工程: SS-Plan` を実行するより前）」であり、通常は本ステップに到達する時点で対象スタックは構築済みのはずである。本ステップはその構築が完了していることを再確認する**安全網**として機能する（`README.md` §5.5 Step0〜0c）。あわせて `.claude/repositories.local.md`「## スタックリポ」の記入・clone についても、本ステップが保証されたゲートになる（`/case-init` 時点での記入は任意の前倒しに過ぎない）。

- Step 3 で棚卸した sub-issue の対象スタックを重複排除して一覧化する。
- **各スタックについて `.claude/repositories.local.md`「## スタックリポ」の該当行を確認する。**
  - **空欄の場合**: 「`.claude/repositories.local.md` の {スタック名} 行に owner/repo が未記入です。記入してから再実行してください」と案内して**停止**する（`docs/requirements/プロダクト情報.md`・案件リポと同じ「ファイルを直接編集して再実行」パターン）。
  - **記入済みの場合**: 案件リポ直下に `{スタック名}/` が既に clone 済みか（ディレクトリと `.git` の存在）を確認する。未 clone であれば `git clone {URL} {スタック名}/` を実行し、案件リポの `.gitignore` に当該スタックディレクトリが除外されていることを確認する（`case-init/SKILL.md`「スタックリポのセットアップ」と同一の確認内容）。
- 上記が揃った各スタックについて、`{スタック}/` が `/stack-init` で実体化済み（受領資材が反映されビルド可能な状態）かを確認する。具体的には Springer 系は `{スタック}/pom.xml`（Reacter は `{スタック}/package.json`）が存在し、暫定プレースホルダー（`BUILD_TBD.md`・`CLAUDE.md` の `← TBD` マーカーのみの状態）のままでないことを確認する。
- **実体化されていないスタックがあれば**（＝このプロダクトでそのスタックに `/stack-init` がまだ実行されておらず、`_templates/` の暫定プレースホルダーのままビルド不能な可能性がある）、一覧を提示し「これらのスタックはまだ `/stack-init` が実行されていません。本来は `/issue-init 工程: SS-Plan` を実行する前に構築する手順ですが、未実行のまま本ステップに到達しています。SS 工程（`/issue-init 工程: SS`）に着手する前に `/stack-init` を実行してください」と案内する。
- **`/stack-init` 未実行の確認（最後の箇条書き）は plan.md の生成・PR ゲートをブロックしない**（plan.md 自体はスタックのビルド可否に関わらず作成できるドキュメントである）。Step 6 の完了報告に注意事項として含めるのみとする。**一方、`.claude/repositories.local.md` の空欄・clone 未実行は上記の通り本ステップ自体を停止させる**（plan.md 生成に進む前に解消が必要）。
- PG-Plan・PT-Plan では本ステップを行わない（SS-Plan を通過済みの時点で対象スタックは構築済みであるはずのため）。

### Step 3.6 — 工程内整合性チェック（PG-Plan / PT-Plan のみ・横串確認）
> 前工程（SS / PG）は機能ID単位で並走（issue が並列に完了する）するため、各 issue は他の並列 issue の内容を見ながら作業できない。そのため、同一スタック内の全機能IDの前工程成果物が揃った時点（＝本工程の開始時点）が、横串での整合性を確認できる最初の機会になる。本ステップはこの確認を行う。

- **PG-Plan の場合**: Step 2 で読み込んだ SS 成果物（`{スタック}/docs/detail-design/外部IF定義書.md`・`プログラム仕様書_{機能ID}.md`・`メッセージ一覧.md`・WB）を、対象スタックの**全機能ID分**まとめて確認し、以下の観点で機能ID間の矛盾を確認する。
  - **CPC-1（構造的一致）**: 複数機能IDが同じクラス・テーブル・メッセージコードを参照している場合、定義が矛盾していないか（例: 機能ID Aのプログラム仕様書が定義したクラスを機能ID Bが異なるシグネチャで前提にしている等）。
  - **CPC-2（用語・表記の統一性）**: 複数機能IDの設計書で同じ用語・項目名が異なる表記・意味で使われていないか（`.claude/rules/cross-process-consistency.md` の判定基準に従う）。
  - 不整合を発見した場合は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を適用する。
- **PT-Plan の場合**: Step 2 で読み込んだ PG 実装コード（`{スタック}/src/main/`）を対象スタックの**全機能ID分**まとめて確認し、同様に機能ID間の矛盾（共有クラス・共有メッセージコードの不整合等）を確認する。
- 確認結果（矛盾なし／不整合を発見し手戻り対応した）を discussion-log に記録する。**本ステップは plan.md 生成をブロックする**（棚卸し自体はスタック横断の全機能ID成果物が揃っていないと正確に行えないため）。
- SS-Plan では本ステップを行わない（SS-Plan の入力である基本設計〔`docs/base-design/`〕は UI issue 単位で既に横串の整合性がレビュー済み・`/basic-design-review`）。

### Step 4 — 依存・順序の検証
- 各 sub-issue の `blocked-by` が棚卸し内で解決可能か（循環依存なし）を確認。
- **PG-Plan は**各 PG issue が**ビルド通過単位**になる実装順序を検証（旧 Phase 境界原則）。
- **PT-Plan は**テスト対象が PG 実装済みであること（PG issue 完了が前提）を確認。

### Step 5 — plan.md の生成
`specs/templates/plan.md` の構造に従い、工程に応じた出力先に生成:
- SS-Plan → `specs/{案件キー}/detail-design-plan/{issue_id}/plan.md`（案件リポ）
- PG-Plan → `{スタック}/specs/{案件キー}/implementation-plan/{issue_id}/plan.md`（スタックリポ）
- PT-Plan → `{スタック}/specs/{案件キー}/unit-test-plan/{issue_id}/plan.md`（スタックリポ）

メタ（案件キー・計画工程・issue-id・対象機能ID・影響スタック）／分割方針／**sub-issue 棚卸し表**（PG-Plan は実装順序、PT-Plan はテスト観点・受け入れ条件番号を埋める）／依存関係／先行起票の状態／PR レビュー確認チェックリスト。
discussion-log は同ディレクトリへ。**ADR は起票しない**。

> PT-Plan の**テスト計画書・機能要件対比表**（`{スタック}/docs/unit-test/`）は本工程では作らない。**PT 工程**でテストコードと同所に生成する。

### Step 6 — 完了報告
```
✅ {SS-Plan / PG-Plan / PT-Plan} の plan.md を生成しました（specs・push・PR 対象）。
   {plan.md のパス（SS-Plan は specs/{案件キー}/... ・PG-Plan/PT-Plan は {スタック}/specs/{案件キー}/...）}/{計画工程dir}/{issue_id}/plan.md

📋 sub-issue 棚卸し（機能ID・スタックは機能一覧.mdで確定済み）:
   - [{SS|PG|PT}-{機能ID}] bs : {作成設計書 | 実装クラス | テスト対象・観点}…
   - ...

{SS-Planのみ・Step3.5で未構築スタックを検出した場合に表示:}
⚠️ 以下のスタックはまだ /stack-init が実行されていません:
   - {スタック名}
   SS工程（/issue-init 工程: SS）に着手する前に /stack-init を実行してください。

🎯 次のアクション — 計画ゲート（PR レビュー）:
   docs-to-pr.md の手順で plan.md・discussion-log を push → PR 作成 → レビュー・マージ →
   マージ後に {SS|PG|PT} sub-issue を各スタックリポへ一括先行起票:

   subissue-bulk.sh --case {案件キー} --phase {ss|pg|pt} --tsv {棚卸し.tsv} \
     --plan {マージ済み plan.md のパス} \
     --plan-issue {この計画工程 PR の GitHub URL}

   plan.md 更新後（PR マージ後に軽微修正が発生した場合）は --update で open issue の本文を同期:
   subissue-bulk.sh --case {案件キー} --phase {ss|pg|pt} --tsv {棚卸し.tsv} \
     --plan {マージ済み plan.md のパス} \
     --plan-issue {この計画工程 PR の GitHub URL} --update
```

---

## 制約
- 🚫 `plan.md` を `docs/` に置かない（specs 配下のまま。specs 自体は push・PR 対象）
- 🚫 ADR を起票しない／`phase-{N}` 概念は使わない（粒度＝機能ID）
- 🚫 スタックへの分解を SS-Plan で再度行わない（UI 工程で機能ID採番時に完了済み）
- ✅ 上流成果物から **漏れなく** sub-issue を列挙する（PG-Plan は実装順序、PT-Plan はテスト観点・受け入れ条件番号を必ず付ける）
- ✅ PT-Plan のテスト計画書・機能要件対比表は本工程で作らない（PT 工程の成果物）
- ✅ 既存 `plan.md` を上書きする前にユーザーに確認を取る
- ✅ SS-Plan は Step 3.5 で `.claude/repositories.local.md`（スタック行の記入・clone）と `/stack-init` 実行済みかを確認する。前者は空欄・未cloneなら本ステップを停止させ、後者（未構築）は完了報告で案内するのみで plan.md 生成自体はブロックしない
- ✅ PG-Plan・PT-Plan は Step 3.6 で対象スタックの前工程成果物（SS成果物／PG実装コード）を全機能ID横断でCPC-1/CPC-2の観点で確認し、不整合が無いことを確認してから plan.md を生成する（本チェックは plan.md 生成をブロックする）

## 関連スキル・参照先
- 前工程（計画工程 着手）: `/issue-init 工程: SS-Plan` / `PG-Plan` / `PT-Plan`
- テンプレート: `specs/templates/plan.md`
- 計画ゲート（PR）・先行起票: `.claude/orchestrators/docs-to-pr.md`
- 新規スタック構築（SS-Plan Step 3.5 で未構築を検出した場合）: `.claude/skills/stack-init/SKILL.md`
- 次工程: SS-Plan→SS（`/detailed-design-gen`）／PG-Plan→PG（`/springer-scaffold`）／PT-Plan→PT（`/springer-unit-test-gen`・`feature/{案件キー}-pt-{機能ID}`）

## 作業指示
$ARGUMENTS
