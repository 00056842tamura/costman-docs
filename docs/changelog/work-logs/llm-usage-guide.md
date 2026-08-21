# LLM 使用量（工数計上）ログ 説明資料

> 対象読者: 本テンプレートで開発を行う開発者・工数を集計する管理者
> 目的: 各工程（チケット=issue）での AI とのやり取りを「工数」として自動記録する仕組みの使い方を説明する。
---

## 1. これは何か

各工程（チケットベース）で AI（Claude Code）とやり取りするたびに、以下を **issue 単位で自動記録**します。

- 使用した LLM のモデル
- トークン数（入力 / 出力 / キャッシュ）
- 作業時間（壁時計）

記録は **1 やり取り（あなたの指示 → AI の 1 応答）ごとに 1 行**増えていきます。操作は不要で、フックが自動で書き込みます。

## 2. どこに記録されるか

工程 issue の作業ディレクトリ配下に `llm-usage.jsonl` が作られます。**記録先リポジトリは工程によって異なります**（3 リポ構成・案件リポ／スタックリポ）。

- **SA / UI / SS-Plan（案件リポ側）**:
  ```
  specs/{案件キー}/{工程}/{issue_id}/
  ├── meta.md            … 工程issue の状態（ここには工数を書きません）
  ├── work-log.md        … ファイル編集ログ
  └── llm-usage.jsonl    … ★工数計上ログ（1 行 = 1 やり取り）
  ```
- **SS 以降（PG-Plan / PG / PT-Plan / PT・スタックリポ側）**: 上記と同じ構成が、対象スタックリポ内の `{スタック}/specs/{案件キー}/{工程}/{issue_id}[_{機能ID}]/` に置かれます（機能ID単位の工程は `{issue_id}_{機能ID}` ディレクトリ）。

- どの issue に記録されるかは **作業ブランチ** (`feature/{案件キー}-{工程}[-{機能ID}]`。機能ID は `{カテゴリ}-{3桁連番}`) から自動判定されます。
- ブランチが新ワークフロー形式でない場合は issue 単位の記録は行われません（誤記録防止）。

## 3. 記録される項目（1 行の JSON）

| 項目 | 意味 |
|---|---|
| `ts` / `date` | 記録時刻 |
| `source` | `main`＝メイン会話 / `subagent`＝サブエージェント（レビュー・テスト生成等） |
| `model` | 使用した LLM モデル ID |
| `input_tokens` / `output_tokens` | 入出力トークン（1 やり取り内の全 API 呼び出しを合算） |
| `cache_read_input_tokens` / `cache_creation_input_tokens` | プロンプトキャッシュの内訳 |
| `total_tokens` | 上記 4 種の合計 |
| `duration_sec` | 作業時間（秒・壁時計） |
| `exchange_uuid` | やり取りの一意キー（集計時の重複排除用） |
| `branch` / `user` / `session_id` | 文脈情報 |

### 記録例
```json
{"ts":"2026-06-19T14:32:10","date":"2026-06-19","source":"main","model":"claude-opus-4-8","input_tokens":2200,"output_tokens":130,"cache_read_input_tokens":1100,"cache_creation_input_tokens":0,"total_tokens":3430,"duration_sec":20.0,"exchange_uuid":"a2","branch":"feature/inventory-2026-001-sa","user":"yamada","session_id":"abc123"}
{"ts":"2026-06-19T14:35:02","date":"2026-06-19","source":"subagent","model":"claude-opus-4-8","input_tokens":8000,"output_tokens":600,"cache_read_input_tokens":0,"cache_creation_input_tokens":0,"total_tokens":8600,"duration_sec":42.0,"exchange_uuid":"d7","branch":"feature/inventory-2026-001-sa","user":"yamada","session_id":"abc123"}
```

## 4. 仕組み（自動記録の流れ）

1. AI が 1 応答を終えると **`Stop` フック**（サブエージェントは **`SubagentStop` フック**）が発火する。
2. 会話ログ（トランスクリプト）から直前のあなたの発言以降の AI 応答を読み、`usage`・`model`・時刻を集計する。サブエージェントの場合は当該サブエージェント自身の孤立したトランスクリプトを読む（ADR-010）。
3. 作業ブランチ（または `issue-init` が着手時に書き込むスコープ確定マーカーがあればそれを優先）から記録先 issue を解決する（`log_work.py` と同じロジック）。
4. `llm-usage.jsonl` に 1 行追記する。

フックの登録は `.claude/settings.json`（`Stop` / `SubagentStop`）、実体は `.claude/hooks/log_llm_usage.py` です。調査用の診断ログ（既定は無効）については同ファイルの先頭コメント・`docs/decisions/ADR-010-llm-usage-hook-transcript-misattribution.md` を参照してください。

## 5. 集計のしかた（/usage-report スキル）

`/usage-report` スキルで案件単位の工程別・手戻り別サマリを自動生成します（jq 不要）。

```
/usage-report
案件キー: inventory-2026-001
```

内部では `.claude/scripts/usage_report.py`（Python3）が以下を実行します:
1. `specs/{案件キー}/` 配下の全 `llm-usage.jsonl` を収集
2. `exchange_uuid` で重複排除
3. ブランチ名から工程・手戻りID を判定して集計
4. `docs/changelog/usage-{案件キー}.md` にサマリレポートを出力

```bash
# 直接 Python スクリプトで実行する場合
python3 .claude/scripts/usage_report.py --case inventory-2026-001 --out docs/changelog/usage-inventory-2026-001.md
```

> `exchange_uuid` で重複排除するのは、area別工程（SS/PG/PT）で同じやり取りが複数の機能IDディレクトリに記録され得るためです（SA/UI では単一ディレクトリなので影響しません）。

## 6. 注意・制約

- **作業時間は壁時計**（ツール実行時間込み）です。純粋な推論時間ではありません（工数の近似値として扱ってください）。
- **サブエージェント分は別行（`source: "subagent"`）で計上**します。メイン分とは別トランスクリプトのためトークンは重複せず、合算しても二重計上になりません。
- トランスクリプトの形式はバージョン依存があるため、取得できない場合はその行をスキップします（記録が欠けることはあっても、開発作業や会話を妨げることはありません）。
- 記録には Python 3.8 以上が PATH に必要です。

## 7. 関連
- 実装: `.claude/hooks/log_llm_usage.py`・`.claude/settings.json`
- 記録漏れ・過小集計の根本原因調査と対応: `docs/decisions/ADR-010-llm-usage-hook-transcript-misattribution.md`
