---
name: basic-design-review
description: 基本設計書群（docs/base-design/）を要件定義（docs/requirements/）との整合性・フレームワーク規約（RestErrorInfo等）との構造的一致の観点でAIレビューする（工程2 UI・要件ID×機能ID）
---

# 基本設計AIレビュースキル（工程2 UI）

`docs/base-design/` の **基本設計書群**（`機能概要_{機能ID}.md`・`Web_API_IF定義書_{機能ID}.md`・`シーケンス図_{機能ID}.md`・`テーブル定義書_{機能ID}.md`・`ロバストネス図_{要件ID}.md`・`実装対象クラス一覧.md`・`テストシナリオ.md`・`クラス図.md`・`画面状態遷移図_{機能ID}.md` 等）を、`docs/requirements/` の **要件定義**（正本）を基準に以下の4観点でレビューする:
1. 要件定義との整合性（構造的一致）
2. 用語・表記の統一性（CPC-2）
3. PSK40 設計書の網羅性
4. フレームワーク規約との構造的一致（RestErrorInfo等・要件定義とは無関係のフレームワーク仕様準拠確認）

> **対象工程: UI（工程2）**。入力は要件ID単位（1 UI issue で複数機能IDが採番されるため、要件ID単位でレビュー範囲を確定する）。
> **レビューレポートは specs（push・PR対象）**。

## 入力形式
```
/basic-design-review
案件キー: inventory-2026-001
対象要件ID: R001
UI issue-id: {issue_id}
```

---

## 実行手順

### Step 1: 対象ファイルの確認
- [ ] `docs/base-design/機能一覧.md` を確認し、対象要件ID（`R###`）から分解された全機能ID（`{カテゴリ}-{3桁連番}`）を特定する
- [ ] 特定した機能IDごとに `docs/base-design/機能概要_{機能ID}.md` 等の基本設計成果物が存在するか確認する

### Step 2: 基準ドキュメントの読み込み
- `docs/requirements/要件定義_{要件ID}.md` — 要求一覧・受け入れ条件・非機能要件（整合性確認の基準）
- `docs/requirements/要件一覧.md` — 要件ID の確認
- `docs/glossary/business-terms.md`・`docs/glossary/system-terms.md` — 業務用語・システム用語辞書（CPC-2 の基準）

### Step 3: 3観点でレビューを実施

#### 観点A: 要件定義との整合性（構造的一致）
| チェック項目 | 確認方法 |
|---|---|
| 受け入れ条件が `docs/base-design/テストシナリオ.md` の「受け入れ条件カバレッジ」表に全件反映されているか | 要件定義_{要件ID}.md の受け入れ条件とテストシナリオ.mdを照合 |
| 非機能要件が API 設計・DB スキーマに反映されているか | 要件定義_{要件ID}.md の非機能要件と Web_API_IF定義書_{機能ID}.md・テーブル定義書_{機能ID}.md を照合 |
| 要件ID→機能IDの分解が `docs/base-design/機能一覧.md` に漏れなく記録されているか | 機能一覧.md の要件ID列と分解結果を照合 |
| 要求一覧の各要求が機能概要・Web_API_IF定義書のいずれかに反映されているか | 要件定義_{要件ID}.md の要求一覧と機能概要_{機能ID}.md 等を照合 |

#### 観点B: 用語・表記の統一性（CPC-2）
`.claude/rules/cross-process-consistency.md` の判定基準を適用する。
| チェック項目 | 確認方法 |
|---|---|
| 固有名詞・項目名・業務用語（`docs/glossary/business-terms.md` 記載語を含む）が要件定義と完全一致しているか | 要件定義_{要件ID}.md・glossary の用語と基本設計成果物の記述を照合（完全一致） |
| 処理概要・業務ルールの説明的記述が要件定義と意味的に一致しているか | 要件定義_{要件ID}.md の目的・背景・要求一覧の記述と機能概要_{機能ID}.md・シーケンス図_{機能ID}.md の記述を照合（意味的一致） |

#### 観点C: PSK40設計書の網羅性
`basic-design-gen/SKILL.md` の「生成対象（UI 成果物に限定・新ツリー準拠）」表を基準に、当該機能IDに適用条件が合致する成果物が全て生成されているか確認する。
| チェック項目 | 確認先 |
|---|---|
| `機能概要_{機能ID}.md`（常に生成）が存在するか | `docs/base-design/` |
| `Web_API_IF定義書_{機能ID}.md`（API を持つ機能）が存在するか | `docs/base-design/` |
| `シーケンス図_{機能ID}.md`（常に生成）が存在するか | `docs/base-design/` |
| `テーブル定義書_{機能ID}.md`（bs/batch・DBスキーマ変更あり）が存在するか | `docs/base-design/` |
| `ロバストネス図_{要件ID}.md`（常に生成）が存在するか | `docs/base-design/` |
| `画面状態遷移図_{機能ID}.md`（frontend）が存在するか | `docs/base-design/` |
| `実装対象クラス一覧.md`・`テストシナリオ.md`・`クラス図.md`（累積）に当該機能ID分が反映されているか | `docs/base-design/` |
| `Web_API_IF一覧表.md` に当該機能の API 行が追記されているか | `docs/base-design/` |

#### 観点D: フレームワーク規約との構造的一致（RestErrorInfo）
`Web_API_IF定義書_{機能ID}.md` の400/409等の標準エラー電文が、Springerフレームワークの固定構造（要件定義とは無関係の、フレームワーク仕様そのものへの準拠確認）と完全一致しているかを確認する。
| チェック項目 | 確認方法 |
|---|---|
| トップレベル（`errorCode`/`message`/`detail`）のキー名・大文字小文字（camelCase）が一致しているか | `.claude/rules/springer-exception.md`「API エラーレスポンス構造（RestErrorInfo）」の定義と1文字ずつ照合 |
| `detail[]` 内部要素（`Type`/`Field`/`ValidationMessage`）のキー名・**大文字小文字（PascalCase）**が一致しているか（`type`/`field`/`validationMessage`等の小文字camelCaseへの書き換えがないか） | 同上（表記の非対称性に特に注意） |
| `Field` がnull許容として記載され、グローバルエラー（`Type=GLOBAL`）の説明を含むか | 同上 |

### Step 4: 結果の出力（レポートは specs・push・PR対象）
```
## 基本設計レビュー結果（工程2 UI・要件ID{要件ID}×機能ID{{カテゴリ}-###...}）

### 観点A: 要件定義との整合性不一致（違反 N 件）
- ...

### 観点B: 用語・表記の統一性不一致（CPC-2・違反 N 件）
- ...

### 観点C: PSK40設計書の網羅性不足（違反 N 件）
- ...

### 観点D: フレームワーク規約との構造的不一致（RestErrorInfo・違反 N 件）
- ...

## 優先修正箇所
## 総評（PR 提出可否）
```
レポートは `specs/{案件キー}/base-design/{issue_id}/review-report.md`（specs・push・PR対象）。

### Step 5: ADR-UI-{n} の起票（強制ゲート）

レビュー結果に関わらず、本レビューで確認した設計上の判断事項ごとに **ADR-UI-{n}** を必ず起票する。`issue-to-design.md` Step9 で既に起票済みの ADR とは別枠（レビューで新たに判明した決定事項のみ追加起票する）。

- **finding が 0 件の場合**: `ADR-UI-{n}-design.md`（{n} は既存 ADR の続きから連番。finding なし・要件定義との整合性確認済み・特記事項なし）を 1 件作成する
- **finding がある場合**: 対処方針・採用したアプローチごとに 1 件ずつ起票する（finding を修正した場合も決定根拠を残す）

```
命名規則: ADR-UI-{n}-design.md（{n} = 既存 ADR の続きから連番）
格納先: specs/{案件キー}/base-design/{issue_id}/adr/
内容: 判断事項のタイトル・選択した設計・選択しなかった代替案・採用理由
```

### Step 6: 修正案の提示と手戻り連携
重大な違反は修正案を提示し、確認後にファイルを修正する。修正後は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を適用する（手順1 インラインフィックス判定 〜 手順5 大規模手戻りエスカレーション）。要件定義側の修正が必要な場合は手戻りID採番（手順3）＋SA工程への修正PRを促す（手順4 の要件定義影響確認に相当）。

---

## 制約
- 🚫 コードの生成・修正はしない（設計書のレビューのみ）
- 🚫 **`docs/base-design/` の修正に生成スキル（`/basic-design-gen`）を再実行せず流用しない**（修正は本スキルの Step 6 内で完結させる。設計判断に関わる修正が必要な場合は `issue-to-design.md` の手順に従い ADR 追加起票後に修正する）
- ✅ 違反箇所は具体的な箇所（セクション名・項目名・機能ID等）を明示する
- ✅ レビューレポートは specs（push・PR対象）
- ✅ 「警告なし」の場合も全観点を確認したことを明示する

## 関連
- 前: `/basic-design-gen`
- 次: `issue-to-design.md` Step11（人間確認）→ PR（`docs/base-design/`・案件リポ）→ 人間レビュー → 工程3 SS-Plan

## 作業指示
$ARGUMENTS
