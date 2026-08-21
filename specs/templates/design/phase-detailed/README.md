# 詳細設計WBテンプレート（工程4 SS 用）

工程4 SS（詳細設計）で `/detailed-design-gen` が参照する PSK 準拠の**層別設計WB**テンプレート群。
WB（作業設計）は **specs（push・PR対象）**、形式設計書（外部IF定義書等）は
`/detailed-design-gen` が **`{スタック}/docs/detail-design/` に直接生成**する。テーブル定義書は UI 工程 `docs/base-design/` が正本のため、本工程では生成しない（読み込みのみ）。

本テンプレート群は工程4 SS（詳細設計）専用の PSK 準拠設計WBであり、プログラム仕様書レベルの詳細度を持つ（工程2 UI の基本設計は `/basic-design-gen` が `docs/base-design/` へ直接生成するため、コピー用テンプレートは別途持たない）。

## 配置先（新構造・スタック×機能ID）
```
{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/
  ├── controller.md     ... Springer Controller（bs / us-api / us-mpa）
  ├── service.md        ... Springer Service
  ├── repository.md     ... Springer Repository / Mapper
  ├── frontend.md       ... Reacter フロントエンド機能（frontend）
  └── batch.md          ... バッチ処理（batch）
```
- 旧 `specs/issues/issue-{n}/design/phase-{N}/` は廃止（`phase-` 概念なし・area=スタック×機能ID）。
- `/detailed-design-gen` が自動生成・配置する（スタックリポで実行）。WB は **specs（push・PR対象）**。

## PSKとの対応
| このテンプレート | PSK設計書 | 対象スタック |
|---|---|---|
| `controller.md` | `50_詳細設計/プログラム仕様書.md`（Controller担当部分） | bs / us-api / us-mpa |
| `service.md` | `50_詳細設計/プログラム仕様書.md`（Service担当部分） | bs / us-api / us-mpa |
| `repository.md` | `50_詳細設計/プログラム仕様書.md`（Repository担当部分）＋`docs/base-design/テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（UI工程が正本・テーブル単位・読み込みのみ） | bs / us-api / us-mpa |
| `frontend.md` | `30_要件定義/画面レイアウト.md` ＋ `50_詳細設計/プログラム仕様書.md` | frontend |
| `batch.md` | `50_詳細設計/ジョブフロー一覧.md` ＋ `50_詳細設計/プログラム仕様書.md` | batch |

## 形式設計書（成果物）との関係
- WB（本テンプレ）は specs の**作業設計**。**形式設計書**（`外部IF定義書` 等）は
  `/detailed-design-gen` が WB と同時に **`{スタック}/docs/detail-design/` へ直接生成**する（直接編集モデル）。`テーブル定義書_{テーブルID}_{テーブル名（論理）}` は UI 工程 `docs/base-design/` が正本のため、本工程では生成しない（読み込みのみ）。
- batch は `/batch-design-gen` が `batch/docs/detail-design/` に ジョブネット一覧・ジョブフロー一覧_{機能ID} を生成する。
- 旧「Step12 `/documentation` で `{スタック}/docs/design/` に反映」は廃止（直接編集モデル）。
- `{スタック}/docs/detail-design/` 配下に Phase別・issue別ディレクトリを作らない／機能IDなしのファイルを作らない。
