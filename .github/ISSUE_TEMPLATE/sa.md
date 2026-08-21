---
name: "[SA] 要件定義"
about: SA 要件定義工程（type:phase・案件リポ）。起票時に「工程:sa」ラベルと Milestone を設定してください。
title: "[SA] "
labels: ["type:phase", "repo:case", "工程:sa"]
---

## メタ
- 案件キー: <!-- {案件キー}（= Milestone） -->
- 工程: SA 要件定義（type:phase・案件リポ）
- 作業ブランチ: `feature/{案件キー}-sa`
- リポ: 案件リポ
- 担当:

---

## この工程で担保すること
- 開発スコープ（何を作るか）が関係者間で合意されている
- 要件一覧（R###）が確定し、後続工程（機能ID分解の起点）のすべての起点となる識別子が揃っている
- 業務要件・制約・非機能要件が記録されており、設計・実装の判断根拠として参照できる

## 対応概要
業務要件を整理し、要件一覧（R### 採番）を確定する。
壁打ちを通じて要件を深掘りし、ADR-SA-{n} に判断根拠を記録する。
最終的に要件定義書群（`docs/requirements/`）を生成して工程ゲート PR を出す。
**機能ID（`{カテゴリ}-{3桁連番}`・スタック×API/画面/ジョブ単位）は本工程では採番しない（UI 工程 `/basic-design-gen` が要件IDを分解して採番する）。**

## インプット
- 業務要求・ヒアリング資料（担当者から入手）
- （2案件目以降）既存 `docs/requirements/要件一覧.md`（R### 重複確認のため参照）

## 対応フロー
1. `/issue-init 工程: SA 案件キー: {案件キー}` を実行（工程ブランチ・作業ディレクトリ生成）
2. `issue-to-requirement` に従い Claude と壁打ちして要件を深掘りする（初回案件のみ Step 1.5 でDB構成・フレームワークバージョンを確定）
3. R### を採番し `docs/requirements/要件一覧.md` に記録する
4. 判断事項を `ADR-SA-{n}`（連番）として記録する
5. `/requirement-doc-gen` を実行して要件定義書群を生成する
6. `/docs-to-pr` で工程ゲート PR を作成する

## アウトプット
| 資料名 | 格納場所 |
|---|---|
| 要件一覧.md（R### 採番台帳） | `docs/requirements/` |
| 業務概要.md | `docs/requirements/` |
| システム全体図.md | `docs/requirements/` |
| システム化業務フロー.md | `docs/requirements/` |
| コード定義書.md | `docs/requirements/` |
| 画面レイアウト.md | `docs/requirements/` |
| ユースケース図.md | `docs/requirements/` |
| 画面遷移図.md | `docs/requirements/` |
| 帳票レイアウト.md（該当時のみ） | `docs/requirements/` |
| 要件定義_{要件ID}.md（要件ごと） | `docs/requirements/` |
| ADR-SA-{n}（判断根拠） | `specs/{案件キー}/requirements/{issue_id}/` |

## レビュー対象
- `docs/requirements/要件一覧.md`：R### の網羅性・優先度・スコープ外の明示
- `docs/requirements/業務概要.md`：業務フロー・前提条件の正確性
- `docs/requirements/ユースケース図.md`・`画面遷移図.md`：業務要求レベルの遷移・関連の正確性
- `docs/requirements/コード定義書.md`・`画面レイアウト.md`：定義済みコード値・画面項目の網羅性
- `docs/requirements/要件定義_{要件ID}.md`（要件ごと）：受け入れ条件・制約・非機能要件の正確性・曖昧さの排除

## 完了ゲート
- [ ] 要件一覧.md に R### が採番されている
- [ ] 工程ゲート PR（`feature/{案件キー}-sa` → `feature/{案件キー}`）が作成されている
- [ ] ADR-SA-{n} が本PRに含まれている（push 済み）
- [ ] discussion-log/ADRの未解決事項・未確定事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（Step 0）
- [ ] レビュアーの承認を得ている
