---
name: detailed-design-review-batch
description: バッチ詳細設計WB（batch.md）・形式設計書（ジョブネット一覧/ジョブフロー一覧）をSpringer batch規約・基本設計との整合性観点でAIレビューする（工程4 SS・batch×機能ID）
---

# 詳細設計AIレビュースキル・バッチ版（工程4 SS）

`batch/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/batch.md` と
`batch/docs/detail-design/` の **形式設計書**（ジョブネット一覧・ジョブフロー一覧）を以下の3観点でレビューする:
1. Springer バッチ規約への準拠
2. 基本設計（`docs/base-design/`）との整合性
3. PSK 設計書の網羅性

> **レビューレポートは specs（push・PR対象）**。`スタック: batch` パラメータは不要（固定）。

## 入力形式
```
/detailed-design-review-batch
案件キー: inventory-2026-001
機能ID: J001
SS issue-id: {issue_id}
```

---

## 実行手順

### Step 1: 対象ファイルの確認
- [ ] `batch/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/batch.md` が存在するか確認
- [ ] `batch/docs/detail-design/ジョブネット一覧.md` が存在するか確認
- [ ] `batch/docs/detail-design/ジョブフロー一覧_{機能ID}.md` が存在するか確認

### Step 2: 基準ドキュメントの読み込み
- `docs/base-design/機能概要_{機能ID}.md`・`シーケンス図_{機能ID}.md` — 整合性確認の基準（常時）
- `.claude/rules/springer-batch.md` — バッチ設計規約
- `.claude/rules/springer-service-transaction.md` — トランザクション制御規約
- `.claude/rules/springer-exception.md` — 例外ハンドリング規約
- ※ `springer-di-bean.md` は不要（CommandLineRunner 実装のため）

### Step 3: 3観点でレビューを実施

#### 観点 A: Springer バッチ規約準拠
（確認先: `batch.md` / `ジョブネット一覧.md` / `ジョブフロー一覧_{機能ID}.md`）

| # | チェック項目 | 確認先 |
|---|---|---|
| A-1 | `@ConditionalOnProperty(value="exec")` 付与 | `batch.md` |
| A-2 | `CommandLineRunner` 実装 | `batch.md` |
| A-3 | `ExitCodeExceptionMapper` 実装・終了コード定義 | `batch.md` |
| A-4 | 正常終了コード `0` | `batch.md` |
| A-5 | OS スケジューラ禁止（ジョブ管理ソフト使用） | `ジョブネット一覧.md` |
| A-6 | リラン単位がジョブ単位 | `ジョブフロー一覧_{機能ID}.md` |
| A-7 | ジョブ実行タイムアウト設定 | `ジョブネット一覧.md` or `batch.md` |
| A-8 | トランザクション境界が業務要件に対して適切 | `batch.md` |
| A-9 | 競合リソース時はスケジュール上で排他 | `ジョブネット一覧.md` |
| A-10 | シェルスクリプトが Java 終了コードをジョブ管理ソフト向けに変換 | `batch.md`（戻り値設計欄） |

#### 観点 B: 基本設計（`docs/base-design/`）との整合性

| # | チェック項目 | 確認方法 |
|---|---|---|
| B-1 | ジョブネット構成・スケジュールが `機能概要_{機能ID}.md` と一致 | `ジョブネット一覧.md` と照合 |
| B-2 | ジョブフロー（処理ステップ）が `シーケンス図_{機能ID}.md` と一致 | `ジョブフロー一覧_{機能ID}.md` と照合 |
| B-3 | 入出力ファイル・外部連携が基本設計から漏れていない | `ジョブフロー一覧_{機能ID}.md` の入出力欄と照合 |
| B-4 | 起動種別（定期/イベント/手動）が基本設計と一致 | `ジョブネット一覧.md` と照合 |
| B-5 | 用語・項目名・エンティティ名等の表記が基本設計と一致するか（CPC-2） | `.claude/rules/cross-process-consistency.md` の判定基準に従い、固有名詞・項目名・列名等は完全一致、説明的記述は意味的一致で確認 |

#### 観点 C: PSK 設計書の網羅性

| # | チェック項目 | 確認先 |
|---|---|---|
| C-1 | ジョブネット一覧に ID（`JN001`〜）・名称・スケジュール・起動種別・概要 | `ジョブネット一覧.md` |
| C-2 | ジョブフロー一覧に ID（`J001`〜）・名称・所属JN・クラス名・概要 | `ジョブフロー一覧_{機能ID}.md` |
| C-3 | 各ジョブ詳細に Mermaid フロー図 | `ジョブフロー一覧_{機能ID}.md` |
| C-4 | 入出力（ファイル名・形式・件数上限） | `ジョブフロー一覧_{機能ID}.md` |
| C-5 | エラーハンドリング（終了コード・リラン方法） | `ジョブフロー一覧_{機能ID}.md` |
| C-6 | `batch.md` に FQCN・対応機能ID・処理概要 | `batch.md` |
| C-7 | `batch.md` に Cursor（DB カーソル）利用有無 | `batch.md` |

### Step 4: 結果の出力（レポートは specs・push・PR対象）
```
## 詳細設計レビュー結果（工程4 SS・batch×{機能ID}）

### 観点A: Springer バッチ規約違反
- A-1 ...: ...（違反なし / 違反 N 件）
...

### 観点B: 基本設計との整合性不一致
- B-1 ...: ...

### 観点C: PSK 設計書の網羅性不足
- C-1 ...: ...

## 優先修正箇所
## 総評（PR 提出可否）
```
レポートは `batch/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/review-report.md`（specs・push・PR対象）。

### Step 5: ADR-SS-{n} の起票（強制ゲート）

レビュー結果に関わらず、本レビューで確認した設計上の判断事項ごとに **ADR-SS-{n}** を必ず起票する。

- **finding が 0 件の場合**: `ADR-SS-001-design.md`（finding なし・Springer バッチ規約準拠確認済み）を 1 件作成する
- **finding がある場合**: 対処方針・採用したアプローチごとに 1 件ずつ起票する

```
命名規則: ADR-SS-{n}-design.md（{n} = 001, 002, ... 連番）
格納先: batch/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/adr/
内容: 判断事項のタイトル・選択した設計・選択しなかった代替案・採用理由
```

### Step 6: 修正案の提示と手戻り連携
重大な違反は修正案を提示し、確認後にファイルを修正する。修正後は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を適用する（手順1 インラインフィックス判定 〜 手順5 大規模手戻りエスカレーション。既存の「上位設計書確認」の内容は手順4 に相当し、そのまま包含される）。手戻りID採番（手順3）を経由しない簡易修正は、手順1のインラインフィックス判定に該当する場合のみ許容する。

---

## 制約
- 🚫 コードの生成・修正はしない（設計書のレビューのみ）
- 🚫 **WB 修正に `/springer-review-fix` を流用しない**（PG 工程向けスキルのため。WB 修正は本スキルの Step 6 内で完結させる）
- ✅ 違反箇所は具体的な箇所（セクション名・ジョブID 等）を明示する
- ✅ レビューレポートは specs（push・PR対象）
- ✅ 「警告なし」の場合も全観点確認を明示する

## 関連
- 前: `/detailed-design-gen`（batch.md WB 生成）＋ `/batch-design-gen`（ジョブネット一覧・ジョブフロー一覧 生成）
- 次: PR（`batch/docs/detail-design/`・スタックリポ）→ 人間レビュー → 工程6 PG

## 作業指示
$ARGUMENTS
