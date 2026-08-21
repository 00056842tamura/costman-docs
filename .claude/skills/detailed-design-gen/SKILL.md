---
name: detailed-design-gen
description: 基本設計とSS-Plan棚卸しを入力に、詳細設計WB（specs）とPSK50形式設計書（外部IF定義書・プログラム仕様書・メッセージ一覧）を {スタック}/docs/detail-design/ に直接生成する（工程4 SS・スタック×機能ID）。テーブル定義書はUI工程が正本のため読み込みのみ。非機能共通設計（docs/base-design/非機能共通設計.md）はスタック単位の共通設計書.mdとして初版生成・差分更新する
---

# 詳細設計生成スキル（工程4 SS）

基本設計（`docs/base-design/`）と SS-Plan の棚卸し（`plan.md`）を入力に、**スタック×機能ID 単位**で
①層別の**詳細設計WB**（specs・push・PR 対象）と ②PSK50 **形式設計書**（外部IF定義書・プログラム仕様書・メッセージ一覧）を
`{スタック}/docs/detail-design/` に**直接生成**する（直接編集モデル）。テーブル定義書は UI 工程 `docs/base-design/` が正本のため本工程では読み込むのみ（生成・変更しない）。

> SS issue は SS-Plan で先行起票済み。本スキルはスタックリポで対象 `スタック×機能ID` の詳細設計を作る。
> `phase-{N}` 概念は廃止（area = スタック×機能ID）。

## 入力形式
```
/detailed-design-gen
案件キー: inventory-2026-001
機能ID: bs-001
スタック: bs              # bs / us-api / us-mpa / frontend / batch
SS issue-id: {issue_id}
```

---

## 実行手順

### Step 1: 入力チェック
- [ ] `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/`（`/issue-init 工程: SS` 済み）が存在するか確認
- [ ] 基本設計 `docs/base-design/機能概要_{機能ID}.md`・`Web_API_IF定義書_{機能ID}.md` が存在するか確認
- [ ] SS-Plan の `plan.md`（対象 sub-issue・作成設計書）が案件リポ `specs/{案件キー}/detail-design-plan/{issue_id}/plan.md` から直接読めるか確認
- [ ] 既存 WB / 設計書がある場合は上書き確認をユーザーに求める

### Step 2: 必要ドキュメントの読み込み
**必須:**
- `docs/base-design/機能概要_{機能ID}.md`・`Web_API_IF定義書_{機能ID}.md`・`シーケンス図_{機能ID}.md`・`ロバストネス図_{要件ID}.md` — API/責務/フロー/オブジェクト分析
- `docs/base-design/テーブル一覧.md`＋当該機能が参照するテーブルの `テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（bs/batch のみ） — **UI工程で確定済みのテーブル定義（テーブル単位）。本工程では読み込み専用（追記・変更しない）。**変更が必要な場合は手戻りとしてUI issueへ戻す
- `docs/base-design/実装対象クラス一覧.md`・`クラス図.md` — 対象クラス・ドメインモデル
- `docs/requirements/要件定義_{要件ID}.md`（当該機能IDの分解元要件IDは `docs/base-design/機能一覧.md` で確認） — 業務ルール・バリデーション・非機能
- `docs/base-design/非機能共通設計.md` — UI工程確定済みの非機能共通方針（backend系スタック〔bs/us-api/us-mpa/batch〕は `## バックエンド非機能事項`、frontendは `## フロントエンド非機能事項` を参照）。本工程では読み込み専用（変更しない）
- SS-Plan `plan.md` — 作成設計書（案件リポ `specs/{案件キー}/detail-design-plan/{issue_id}/plan.md` を直接読む）
- 対象スタックの `CLAUDE.md` — ベースパッケージ・Springer バージョン

> **生成後（SS ゲート）**: WB は `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` に配置し、SS 工程の PR に含めてスタックリポへ push する（添付不要）。PG は SS PR マージ後の `feature/{案件キー}` から分岐するため WB を直接読める。

**存在する場合（既存プロダクト）:**
- `docs/requirements/コード定義書.md` — コード値
- `.claude/rules/springer-*.md`（対象スタック種別）・`.claude/rules/reacter-*.md`（frontend）

### Step 3: 生成対象の決定（スタック種別で出し分け）
| スタック | WB（specs） | 形式設計書（`{スタック}/docs/detail-design/`・直接生成） |
|---|---|---|
| bs | controller/service/repository.md | **プログラム仕様書_{機能ID}.md**・（外部連携あれば）外部IF定義書.md |
| us-api / us-mpa | controller/service/repository.md | **プログラム仕様書_{機能ID}.md**・（外部連携あれば）外部IF定義書.md |
| frontend | frontend.md | **コンポーネント仕様書_{機能ID}.md**（WB を昇格・コンポーネント構成・Props・State・API 接続・Zod スキーマ）＋**画面アクション遷移図_{機能ID}.md**（同 WB のアクション定義・画面遷移を可視化） |
| batch | batch.md | **プログラム仕様書_{機能ID}.md** ＋ ジョブ設計は **`/batch-design-gen`** |

> テーブル一覧.md・テーブル定義書_{テーブルID}_{テーブル名（論理）}.md（bs/batch）は **UI工程 `docs/base-design/` が正本**（テーブル単位。機能ID単位ではない）。本工程は Step2 で読み込むのみで生成・変更しない。
> 上記のほか、全backend系スタック（bs/us-api/us-mpa/batch）・frontendは **`共通設計書.md`**（スタック単位・単一・累積）も生成対象になる（Step 3.5 参照。機能ID単位ではなく当該スタックで最初に着手するSS issueが初版生成する）。

### Step 3.5: 共通設計書の生成/差分更新判定（スタック単位・最初のSS issue判定）
`docs/base-design/非機能共通設計.md` の内容を、当該スタックの `{スタック}/docs/detail-design/共通設計書.md` へ反映する。ファイル存在確認と更新履歴Ver比較の2段階で、初版生成か差分更新かを判定する。

1. **初版生成の判定（ファイル存在確認ベース）**: `{スタック}/docs/detail-design/共通設計書.md` が存在しない場合、当該スタックで最初に着手するSS issueとみなし、初版を生成する。
   - `docs/base-design/非機能共通設計.md` の該当セクション（backend系スタック〔bs/us-api/us-mpa/batch〕は `## バックエンド非機能事項`、frontendは `## フロントエンド非機能事項`）の全項目を、対応するテンプレート（`docs/templates/50_詳細設計/共通設計書_backend.md` または `共通設計書_frontend.md`）へ転記する。
   - 生成した `共通設計書.md` のメタ情報「反映済み上流Ver」に、転記元 `非機能共通設計.md` の「更新履歴」表のうち**自スタック区分（バックエンド or フロントエンド）に該当する行**の最大Verを記録する（他区分の行は無視する）。
2. **差分更新の判定（Ver比較ベース）**: `{スタック}/docs/detail-design/共通設計書.md` が既に存在する場合、その「反映済み上流Ver」と、`docs/base-design/非機能共通設計.md`「更新履歴」表のうち自スタック区分に該当する行の最大Verを比較する。
   - **一致する場合**: 新規決定なし。本ステップでは何もしない（完了報告に「共通設計書.md: 変更なし」と明記する）。
   - **上流のVerが新しい場合**: 差分（新しいVer行に対応する決定事項）のみ該当見出しへ追記し、「反映済み上流Ver」を最新Verへ更新する（既存の記載は変更しない）。
3. batch スタックはSecurityConfig等の一部項目が該当しない場合がある（HTTPエンドポイントを持たないジョブ等）。該当しない項目は項目自体を削除せず「該当なし（理由）」と明記する。
4. `docs/base-design/非機能共通設計.md` に自スタック区分の見出し自体が存在しない場合（UI工程でまだ非機能共通設計が確定していない）、本ステップは実施せず完了報告に「共通設計書.md: 上流未確定のため未生成」と明記する。

### Step 4: 詳細設計WB の生成（specs）
`specs/templates/design/phase-detailed/` の各テンプレをベースに、基本設計・要件・plan を反映して
`{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/{controller,service,repository,frontend,batch}.md` を生成。

> **discussion-log.md の「## Phase 1」「## Phase 2」セクションは、対象の意思決定が確定した直後（本Stepでの成果物ファイルへの反映と同時）に、実際の決定内容で記入すること。** テンプレートのプレースホルダー（`<!-- ... -->`）のまま残さない。壁打ちで確定した「確認した不明点と決定事項」「影響パッケージの確認」（Phase 1）・「採用した設計の選択肢」（Phase 2）を、成果物WBへの反映と合わせて discussion-log.md 本体へも構造的に記入する。サブエージェントへ本工程を委任する場合は、委任プロンプトに確定済みの設計決定一式に加え、この記入手順自体を明示的に含める。

- **controller.md**: `Web_API_IF定義書` の各エンドポイントへ認証認可・単体/関連項目チェック仕様を展開
- **service.md**: 要件・シーケンスから業務ルール／トランザクション設定（bs: `@Transactional` / us: なし）
- **repository.md**: `docs/base-design/テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（UI工程で確定済み・正本・テーブル単位）を参照して対象テーブル・完全なカラム定義を使用。データアクセス仕様（インデックス・想定最大件数）
- **frontend.md**: US-API 接続仕様・Zod バリデーション・reacter 規約・**アクション定義**（画面操作＝ボタン押下・行選択等ごとに、①トリガーとなるイベント ②対象コンポーネント ③ハンドラー関数名 ④処理内容のシーケンス〔入力検証 → API 接続仕様のエンドポイント呼出 → 状態管理の state 更新 → 画面遷移表の遷移先への遷移〕 ⑤事前条件・確認ダイアログの有無を洗い出す。新規登録・更新・削除等の破壊的操作は確認ダイアログの要否を必ず確認する。`docs/base-design/画面状態遷移図_{機能ID}.md` の状態遷移契機〔UI工程で確定済み〕を出発点に、各契機に対応する具体的な処理シーケンスへ詳細化する）
- **batch.md**: ジョブフロー・Cursor 利用・冪等性・エラーハンドリング・バッチ規約

### Step 5: PSK50 形式設計書の生成（`{スタック}/docs/detail-design/` へ直接・拡張）
`docs/templates/50_詳細設計/` をベースに、WB と基本設計から **形式設計書を直接生成**する:

- **`{スタック}/docs/detail-design/外部IF定義書.md`**（外部連携がある場合）: UI で繰り延べた外部 IF をここで作成。リクエスト/レスポンス電文・タイムアウト・リトライ。累積（今回 IF セクションのみ追記）。
- **`{スタック}/docs/detail-design/プログラム仕様書_{機能ID}.md`**（backend: bs/us-api/us-mpa）: WB（controller/service/repository.md）から PSK50 準拠のクラス設計仕様を生成（FQCN・処理フロー・例外・バリデーション仕様）。テンプレ: `docs/templates/50_詳細設計/プログラム仕様書.md`。
  - ⚠️ **版管理はメソッド/クラス単位では行わない**。プログラム仕様書の版管理は文書全体を単位とし、文書末尾の「## 更新履歴」テーブル（Ver/更新日/更新者/更新内容）のみで一元管理する。プログラム単位のメタ情報テーブルに「バージョン」欄を追加・復元しない。
- **`frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md`**（frontend）: WB（frontend.md）の全セクションを昇格させてコンポーネント仕様書を生成する。テンプレ: `docs/templates/50_詳細設計/コンポーネント仕様書.md`。
  - 基本情報（機能名・Feature dir・機能ID・画面名・コンポーネント名・ルートパス・US-API エンドポイント）
  - コンポーネント構成・コンポーネント仕様（Props・State・処理）
  - API 接続仕様（HTTP・エンドポイント・リクエスト/レスポンス型・エラー処理）
  - 入力バリデーション・Zod スキーマ（フィールド・型・ルール・エラーメッセージ）
  - **画面遷移**（操作・遷移先・条件）
  - **アクション定義**（画面操作＝ボタン押下・行選択等ごとに、トリガーイベント・対象コンポーネント・ハンドラー関数名・処理シーケンス〔入力検証 → API 接続仕様の呼出 → 状態管理の state 更新 → 画面遷移表の遷移先への遷移〕・事前条件／確認ダイアログの有無を定義する。「画面遷移」表〔遷移先に着目〕とは役割が異なり、操作のきっかけから処理完結までのシーケンスに着目する。API 接続仕様・入力バリデーション・画面遷移・状態管理の各表の記載と整合させる）
  - **状態管理**（管理方法・React Query / React Hook Form / useState の使い分け）
  - **共通型定義**（TypeScript 型・API 接続仕様との整合性）
- **`frontend/docs/detail-design/画面アクション遷移図_{機能ID}.md`**（frontend）: 同じ WB（frontend.md）の「画面遷移」「アクション定義」の内容を Mermaid `flowchart` で可視化する。テンプレ: `docs/templates/50_詳細設計/画面アクション遷移図.md`。
  - ノード＝「画面名（状態）」（画面表示の変化を `[ ]` で注記）、エッジ＝アクション（トリガー）。他画面への遷移・確認ダイアログも同じ図に含める。
  - コンポーネント仕様書の「画面遷移」「アクション定義」表を**置き換えない**（両方生成する・補完資料）。「アクション×状態 対応表」の記載は両表と一致させる。
- **`batch/docs/detail-design/プログラム仕様書_{機能ID}.md`**（batch）: `/batch-design-gen` 実行後に batch.md と WB からバッチプログラム仕様書を生成（Runner/Tasklet/Processor の処理仕様・Cursor 設定・冪等性設計）。
- **`{スタック}/docs/detail-design/メッセージ一覧.md`**（全スタック: bs/us-api/us-mpa/batch/frontend）: バリデーションエラー・業務例外・システム例外（frontend はエラー／案内メッセージ）のメッセージ行を生成/差分追記（累積・他機能の記述を消さない）。UI で繰り延べた分をここで確定。メッセージ ID 命名規約・コードへの反映方法はスタックにより異なる（backend: `.claude/rules/springer-message-i18n.md`＋`messages.properties`／frontend: `.claude/rules/reacter-libraries.md`＋`/src/config/message.ts`）。テンプレ: `docs/templates/50_詳細設計/メッセージ一覧.md`。
- **`{スタック}/docs/detail-design/共通設計書.md`**（全スタック: bs/us-api/us-mpa/batch/frontend）: Step 3.5 で判定した初版生成/差分更新の内容をここで書き込む（backend系スタックは `docs/templates/50_詳細設計/共通設計書_backend.md`、frontendは `共通設計書_frontend.md` をテンプレとする）。

> batch のジョブネット/ジョブフローは **`/batch-design-gen`** が `batch/docs/detail-design/` に生成する（本スキルでは作らない）。
> テスト計画書（UT 用）は工程7 UT-Plan の成果物（本工程では作らない）。
> ⚠️ **プログラム仕様書は本 SS 工程で生成する**。実装の指針となるドキュメントは詳細設計で担保されるべきであり、PG 工程の `/springer-scaffold`・`/reacter-code-gen` はコード生成のみに専念し、プログラム仕様書を再生成しない。

### Step 6: ファイル書き込み
- WB → `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/`（specs・スタックリポ・push・PR 対象）
- 形式設計書 → `{スタック}/docs/detail-design/`（スタックリポ・PR 対象）。**`共通設計書.md` も本ディレクトリに書き込む**（Step 3.5 の判定結果に基づき新規作成または差分追記）
- ディレクトリが無ければ `mkdir -p`。不明値は `> ⚠️ TODO: 要確認`。

### Step 7: 完了報告
```
✅ 詳細設計を生成しました（工程4 SS・{スタック}×{機能ID}）。

📁 WB（specs・push・PR 対象）: {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/
   {生成した WB の一覧}
📁 形式設計書（{スタック}/docs/detail-design/・PR 対象）:
   外部IF定義書.md（該当時）
   プログラム仕様書_{機能ID}.md（backend/batch）/ コンポーネント仕様書_{機能ID}.md・画面アクション遷移図_{機能ID}.md（frontend）
   メッセージ一覧.md（全スタック・累積追記）
   共通設計書.md: {初版生成 / 差分追記 / 変更なし / 上流未確定のため未生成}
   ※テーブル一覧.md・テーブル定義書_{テーブルID}_{テーブル名}.md は UI 工程 docs/base-design/ が正本（テーブル単位。本工程は読み込みのみ）

🎯 次のアクション:
   - batch を含む場合: /batch-design-gen 案件キー: {案件キー} 機能ID: {機能ID}
   - レビュー: /detailed-design-review 案件キー: {案件キー} 機能ID: {機能ID} スタック: {スタック}
```

---

## 制約
- 🚫 `docs/base-design/`（基本設計）の内容を削除・変更しない
- ✅ WB は `{スタック}/specs/`（push・PR 対象）／形式設計書は `{スタック}/docs/detail-design/`（PR 対象）に分離する
- ✅ 単位は **スタック×機能ID**（`phase-{N}` 概念は使わない）。ただし `共通設計書.md` はスタック単位（機能IDに紐付かない）で例外的に扱う
- ✅ `specs/templates/design/phase-detailed/`・`docs/templates/50_詳細設計/` の構造に従う
- ✅ 「業務ルール」「データアクセス仕様」など PSK 固有セクションを省略しない
- ✅ 外部IF定義書は累積（他機能・他issue の既存記述を消さない）
- 🚫 テーブル一覧.md・テーブル定義書_{テーブルID}_{テーブル名}.md を本工程で生成・変更しない（正本は `docs/base-design/`・UI工程・テーブル単位。実装時の変更は手戻りとしてUI issueへ戻す）
- 🚫 `{スタック}/docs/detail-design/` 配下に Phase別・issue別ディレクトリを作らない／機能IDなしファイルを作らない
- 🚫 `docs/base-design/非機能共通設計.md` の内容を削除・変更しない（UI工程が正本・本工程は読み込み専用）
- ✅ `{スタック}/docs/detail-design/共通設計書.md` は当該スタックで最初に着手するSS issueが初版生成し、以降のSS issueは上流 `非機能共通設計.md` の更新履歴（自スタック区分）のVer比較により新規決定がある場合のみ差分追記する
- ✅ `共通設計書.md` の「反映済み上流Ver」欄は必ず更新する（次のSS issueの差分判定に使われるため）

## 関連
- 前工程: SS-Plan（`/issue-plan`）→ `/issue-init 工程: SS`（先行起票分の着手）
- batch ジョブ設計: `/batch-design-gen`
- 次: `/detailed-design-review` → PR（`{スタック}/docs/detail-design/`・スタックリポ）→ 工程6 PG
- テンプレ: `specs/templates/design/phase-detailed/`・`docs/templates/50_詳細設計/`

## 作業指示
$ARGUMENTS
