---
name: batch-design-gen
description: batch.md（詳細設計WB）を入力にジョブネット一覧・ジョブフロー一覧を AI 生成し batch/docs/detail-design/ に出力する（工程4 SS・batch のみ）
---

# バッチ設計書生成スキル（工程4 SS・batch スタックのみ）

`specs/{案件キー}/detail-design/batch/{issue_id}_{機能ID}/batch.md`（バッチ詳細設計WB）を読み込み、
PSK 準拠のジョブネット一覧・ジョブフロー一覧を **`batch/docs/detail-design/`** 配下に AI 生成する。

> `/detailed-design-gen`（batch.md WB 生成）の後に実行する。`phase-{N}` 概念は廃止（スタック×機能ID）。

## 入力形式
```
/batch-design-gen
案件キー: inventory-2026-001
機能ID: J001            # バッチ機能の機能ID
SS issue-id: {issue_id}
```

---

## ドキュメント種別と生成ルール（出力先 = `batch/docs/detail-design/`）
| ファイル | 種別 | 初回（なし） | 2 回目以降（あり） |
|---|---|---|---|
| `ジョブネット一覧.md` | 一覧系（プロダクト共通・累積） | テンプレから新規生成 | 今回の新規ジョブネットセクションのみ追記 |
| `ジョブフロー一覧_{機能ID}.md` | 機能別（`_{機能ID}`） | 新規機能 → 新規作成 | 既存機能修正 → 上書き更新 |

---

## 実行手順

### Step 1: 入力チェック
- [ ] `specs/{案件キー}/detail-design/batch/{issue_id}_{機能ID}/batch.md` が存在するか確認
- [ ] `docs/base-design/機能概要_{機能ID}.md` を読み、バッチ全体の概要・スケジュールを取得
- [ ] `batch/docs/detail-design/` が存在するか確認（なければ `mkdir -p batch/docs/detail-design`）

### Step 2: 必要ドキュメントの読み込み
- `specs/{案件キー}/detail-design/batch/{issue_id}_{機能ID}/batch.md` — バッチ詳細設計WB
- `docs/base-design/機能概要_{機能ID}.md`・`シーケンス図_{機能ID}.md` — バッチ全体の概要・スケジュール
- `docs/base-design/機能一覧.md` — 機能ID・要件ID・ジョブIDの対応関係
- `.claude/rules/springer-batch.md` — バッチ規約

### Step 3: シナリオ判定
- `batch/docs/detail-design/ジョブネット一覧.md` 有無 → 初回（新規生成）/ 追加（差分追記）
- `batch/docs/detail-design/ジョブフロー一覧_{機能ID}.md` 有無 → 新規機能（新規作成）/ 既存機能修正（上書き）

### Step 4: 各設計書の生成
**ジョブネット一覧.md（一覧系）**
- 【初回】ジョブネット一覧表（ID `JN001`〜・名称・スケジュール・起動種別・概要）＋各ジョブネット詳細（概要・Mermaid 構成図・ジョブ構成表・スケジュール・前提条件）
- 【追加】今回の新規ジョブネットのセクションのみ追記（既存に触れない）／【修正】対象ジョブネット（JN ID）のセクションのみ更新

**ジョブフロー一覧_{機能ID}.md（機能別）**
- 【新規機能】ジョブフロー一覧表（ID `J001`〜・名称・所属ジョブネット・クラス名・概要）＋各ジョブ詳細（概要・Mermaid `flowchart TD`・処理ステップ表・入出力・Cursor 利用・エラーハンドリング）
- 【既存機能修正】ファイル全体を今回の設計で上書き更新

### Step 5: ファイル書き込み（`batch/docs/detail-design/`）
**注意:** Mermaid 図は正しい構文。不明値（cron 式・タイムアウト等）は `> ⚠️ TODO: 要記入`。バッチ規約（全件オンメモリ禁止・`System.exit(1)` 等）を反映。

### Step 5b: プログラム仕様書の生成（`batch/docs/detail-design/`・直接生成）

ジョブフロー設計が確定した後、バッチ向けプログラム仕様書を生成する:

- **`batch/docs/detail-design/プログラム仕様書_{機能ID}.md`**: batch.md と生成済みジョブフロー一覧から PSK60 準拠のバッチプログラム仕様書を生成。
  内容: Runner/Tasklet/Processor の FQCN・処理概要・Cursor 設定・冪等性設計・エラーハンドリング・リスタートポイント。
- **`batch/docs/detail-design/メッセージ一覧.md`**: バッチで使用するバリデーションエラー・業務例外・システム例外のメッセージ行を生成/差分追記（累積・他機能の記述を消さない）。
- テンプレ＝`docs/templates/50_詳細設計/プログラム仕様書.md`（バッチ向けセクション）・`メッセージ一覧.md`。

> ⚠️ **プログラム仕様書は本 SS 工程で生成する**。実装の指針となるドキュメントは詳細設計で担保されるべきであり、PG 工程の `/springer-scaffold` はコード生成のみに専念し、プログラム仕様書を再生成しない。

### Step 6: 完了報告
```
✅ バッチ設計書を生成しました（工程4 SS・batch）。

🆕 ジョブネット一覧.md: {初回/追加/修正} ／ ジョブフロー一覧_{機能ID}.md: {新規/修正}
📁 プログラム仕様書_{機能ID}.md（PR 対象・スタックリポ）
📁 batch/docs/detail-design/ （PR 対象・スタックリポ）

🎯 次のアクション:
   /detailed-design-review-batch 案件キー: {案件キー} 機能ID: {機能ID}
```

---

## 制約
- 🚫 `batch.md`・基本設計の内容を削除・変更しない
- ✅ 出力先は **`batch/docs/detail-design/`**（旧 `batch/docs/design/` ではない）
- ✅ 一覧系（ジョブネット一覧）は他ジョブネットの既存記述を消さず差分追記
- ✅ 機能別（ジョブフロー一覧_{機能ID}）は新規作成 or 上書き更新
- ✅ Mermaid 図は正しい構文・バッチ規約を反映
- 🚫 `phase-{N}` 概念は使わない（スタック×機能ID）

## 関連
- 前: `/detailed-design-gen`（batch.md WB 生成）
- 次: `/detailed-design-review-batch` → PR（`batch/docs/detail-design/`・スタックリポ）
- テンプレ: `docs/templates/50_詳細設計/ジョブネット一覧.md`・`ジョブフロー一覧.md`

## 作業指示
$ARGUMENTS
