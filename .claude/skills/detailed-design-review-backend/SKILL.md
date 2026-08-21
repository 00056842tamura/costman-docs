---
name: detailed-design-review-backend
description: 詳細設計WB・形式設計書（外部IF定義書/プログラム仕様書）をSpringer規約・基本設計（テーブル定義書含む）との整合性観点でAIレビューする（工程4 SS・スタック×機能ID）。対象スタック: bs / us-api / us-mpa
---

# 詳細設計AIレビュースキル・バックエンド版（工程4 SS）

`{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` の **詳細設計WB** と
`{スタック}/docs/detail-design/` の **形式設計書**（外部IF定義書・プログラム仕様書。テーブル定義書は `docs/base-design/` が正本のため基準ドキュメントとして参照）を以下の3観点でレビューする:
1. Springer 規約への準拠
2. 基本設計（`docs/base-design/`）との整合性
3. PSK 設計書の網羅性

> **対象スタック: bs | us-api | us-mpa**（Web 系 Springer スタック）
> **レビューレポートは specs（push・PR対象）**。`phase-{N}` 概念は廃止（スタック×機能ID）。

## 入力形式
```
/detailed-design-review-backend
案件キー: inventory-2026-001
機能ID: bs-001
スタック: bs
SS issue-id: {issue_id}
```

---

## 実行手順

### Step 1: 対象ファイルの確認
- [ ] `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/` の WB（controller/service/repository.md 等）が存在するか確認
- [ ] `{スタック}/docs/detail-design/` の形式設計書（外部IF定義書 等）が存在するか確認

### Step 2: 基準ドキュメントの読み込み
- `docs/base-design/機能概要_{機能ID}.md`・`Web_API_IF定義書_{機能ID}.md`・`シーケンス図_{機能ID}.md` — 整合性確認の基準
- `docs/base-design/テーブル一覧.md`＋当該機能が参照するテーブルの `テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（UI工程が正本・テーブル単位。1機能IDが複数テーブルを参照する場合は該当する全テーブル定義書を読む） — DB スキーマ整合性確認の基準
- WB（`{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/`）＋ 形式設計書（`{スタック}/docs/detail-design/`）
- `{スタック}/docs/detail-design/共通設計書.md`（存在する場合） — 非機能共通方針（認証方式全体・SecurityConfig方針・パスワードハッシュ・セッション・CSP・例外ハンドリング共通方針等）の整合性確認の基準
- `.claude/rules/springer-*.md`（対象スタック種別）

### Step 3: 3観点でレビューを実施

#### 観点 A: Springer 規約準拠
| チェック項目 | 確認先 |
|---|---|
| DI はコンストラクターインジェクションか | service.md / repository.md |
| トランザクション設定はアプリ種別に合っているか（bs: あり / us: なし） | service.md |
| throws 節がインターフェースに記述されていないか | service.md |
| 例外は `ApplicationException` / `SystemException` を継承するか | service.md / repository.md |
| データアクセス例外は Repository のみで catch されているか | repository.md |
| SELECT 1件は null チェック → `ResourceNotFoundException` を throw するか | repository.md |

#### 観点 B: 基本設計（`docs/base-design/`）との整合性
| チェック項目 | 確認方法 |
|---|---|
| API パス・HTTP メソッドが `Web_API_IF定義書_{機能ID}` と一致するか | controller.md と照合 |
| 認証・認可要件が controller.md に反映されているか | Web_API_IF定義書 と照合 |
| 実装対象クラス（plan.md / 実装対象クラス一覧）が全て定義されているか | WB と照合 |
| DB スキーマが repository.md・`docs/base-design/`の**テーブル定義書_{テーブルID}_{テーブル名}**（UI工程が正本・テーブル単位）と一致するか | 基本設計と照合 |
| 用語・項目名・エンティティ名等の表記が基本設計と一致するか（CPC-2） | `.claude/rules/cross-process-consistency.md` の判定基準に従い、固有名詞・項目名・列名等は完全一致、説明的記述は意味的一致で確認 |
| 実装対象クラス（SecurityConfig等）・controller.md/service.mdの認証認可・例外ハンドリング記述が `共通設計書.md` の非機能方針と矛盾しないか（存在する場合） | `共通設計書.md` と照合（CPC-2の完全一致/意味的一致の判定基準に従う） |

#### 観点 C: PSK 設計書の網羅性
| チェック項目 | 確認先 |
|---|---|
| FQCN・対応機能ID が記載されているか | 全 WB |
| 単体項目チェック仕様が全バリデーション項目を網羅しているか | controller.md |
| 業務ルールに抜けがないか | service.md |
| データアクセス仕様（想定最大件数・インデックス）が記載されているか | repository.md |
| **外部IF定義書**（電文・タイムアウト・リトライ）が網羅的か（外部連携あり時） | `{スタック}/docs/detail-design/` |

### Step 4: 結果の出力（レポートは specs・push・PR対象）
```
## 詳細設計レビュー結果（工程4 SS・{スタック}×{機能ID}）

### WB
- controller.md（違反 N 件）: [規約違反]… [整合性不一致]… [網羅性不足]…
- service.md / repository.md …

### 形式設計書（{スタック}/docs/detail-design/）
- 外部IF定義書（N 件）… / プログラム仕様書（N 件）…

## 優先修正箇所
## 総評（PR 提出可否）
```
レポートは `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/review-report.md`（specs・push・PR対象）。

### Step 5: ADR-SS-{n} の起票（強制ゲート）

レビュー結果に関わらず、本レビューで確認した設計上の判断事項ごとに **ADR-SS-{n}** を必ず起票する。

- **finding が 0 件の場合**: `ADR-SS-001-design.md`（finding なし・規約準拠確認済み・特記事項なし）を 1 件作成する
- **finding がある場合**: 対処方針・採用したアプローチごとに 1 件ずつ起票する（finding を修正した場合も決定根拠を残す）

```
命名規則: ADR-SS-{n}-design.md（{n} = 001, 002, ... 連番）
格納先: {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/adr/
内容: 判断事項のタイトル・選択した設計・選択しなかった代替案・採用理由
```

### Step 6: 修正案の提示と手戻り連携
重大な違反は修正案を提示し、確認後にファイルを修正する。修正後は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を適用する（手順1 インラインフィックス判定 〜 手順5 大規模手戻りエスカレーション。既存の「上位設計書確認」の内容は手順4 に相当し、そのまま包含される）。手戻りID採番（手順3）を経由しない簡易修正は、手順1のインラインフィックス判定に該当する場合のみ許容する。

---

## 制約
- 🚫 コードの生成・修正はしない（設計書のレビューのみ）
- 🚫 **WB 修正に `/springer-review-fix` を流用しない**（PG 工程向けスキルのため。WB 修正は本スキルの Step 6 内で完結させる）
- ✅ WB（specs）と形式設計書（`{スタック}/docs/detail-design/`）の両方を観点に含める
- ✅ 違反箇所は具体的な箇所（セクション名・メソッド名・テーブル名等）を明示する
- ✅ レビューレポートは specs（push・PR対象）。`phase-{N}` 概念は使わない
- ✅ 「警告なし」の場合も全観点を確認したことを明示する

## 関連
- 前: `/detailed-design-gen`
- 次: PR（`{スタック}/docs/detail-design/`・スタックリポ）→ 人間レビュー → 工程6 PG

## 作業指示
$ARGUMENTS
