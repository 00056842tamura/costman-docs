---
name: usage-report
description: 案件（Milestone）の LLM 工数を工程別・手戻り別に集計し、サマリレポートを docs/changelog/ に生成する
---

# LLM 工数集計スキル

指定された案件（Milestone）の `specs/` 配下に蓄積された `llm-usage.jsonl` を収集・集計し、
工程別・手戻り別の工数サマリレポートを `docs/changelog/usage-{案件キー}.md` に生成する。

## 入力形式

```
/usage-report
案件キー: inventory-2026-001
```

---

## 実行手順

### Step 1: llm-usage.jsonl の収集

案件リポ（`specs/{案件キー}/`）＋各スタックリポ（案件リポ配下にチェックアウトされている `{スタック}/specs/{案件キー}/`）を横断的に走査して集計する。`{スタック}` は `bs`/`us-mpa`/`us-api`/`frontend`/`batch`（`CLAUDE.md` §3 のスタック配置ルール準拠）。

```bash
# Python3 スクリプトで直接集計（案件リポ＋チェックアウト済みスタックリポを横断走査）
python3 .claude/scripts/usage_report.py --case {案件キー} --specs-dir specs/
```

- チェックアウトされていないスタックリポ・ブランチがある場合は、次のいずれかで解決してから再実行する。
  - 該当スタックリポをローカルに `git clone`（既存 clone があれば `git fetch`）してから走査対象に含める
  - `git show feature/{案件キー}:{スタック}/specs/{案件キー}/.../llm-usage.jsonl` で統合ブランチの内容を直接取得し、ローカルの対応パスに保存してから走査する

### Step 2: 集計（4 レベル）

**Level 1: issue 単位**
各 issue ごとの exchanges 数 / input / output / total tokens / duration_sec

**Level 2: 工程単位**
同一工程（SA/UI/SS/PG/PT 等）の issue を集約

**Level 3: 手戻り単位**
`-sp-` を含むブランチのログを正準工程分と分離して集計

**Level 4: Milestone サマリ**
正準工程合計 / 手戻り合計 / 総計

### Step 3: サマリレポートの生成

`docs/changelog/usage-{案件キー}.md` に以下の形式で出力する:

```markdown
## LLM 工数サマリ（{案件キー}）

### 工程別集計
| 工程 | issue | exchanges | input | output | total | duration_sec |
|---|---|---|---|---|---|---|
| SA | #5 | 45 | 12,000 | 8,000 | 20,000 | 1,200 |
...

### 手戻り別集計
| 手戻りID | 原因概要（meta.md から） | exchanges | total | duration_sec |
|---|---|---|---|---|
| sp-001 | updateEmailVerified 設計ミス | 8 | 5,000 | 300 |

### Milestone 合計
| 区分 | exchanges | total_tokens | duration_sec |
|---|---|---|---|
| 正準工程 | N | X | Y |
| 手戻り | N | X | Y |
| **総計** | **N** | **X** | **Y** |
```

### Step 4: 完了報告

```
✅ LLM 工数サマリを生成しました。
📁 docs/changelog/usage-{案件キー}.md
```

---

## 制約

- ✅ `exchange_uuid` で重複排除してから集計する（area別工程で同一やり取りが複数 dir に記録される場合あり）
- 🚫 集計・レポート生成のみ行う（issues / PR の操作は行わない）
- ✅ `docs/changelog/` が無ければ `mkdir -p` する

## 関連

- 集計スクリプト: `.claude/scripts/usage_report.py`
- ログ記録フック: `.claude/hooks/log_llm_usage.py`
- ログ仕様: `docs/changelog/work-logs/llm-usage-guide.md`

## 作業指示
$ARGUMENTS
