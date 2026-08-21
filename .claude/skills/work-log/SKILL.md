---
name: work-log
description: 作業ログ（デイリー・issue 別）に手動メモを追記
---

# 作業ログ手動追記スキル

ファイル編集の自動ログ（`.claude/hooks/log_work.py` による）に加えて、
**実装者が判断したマイルストーン・決定事項・着手/完了の宣言** を手動で追記するスキルです。

## プロダクト作業時の注意

- 自動ログは Write/Edit 時に `docs/changelog/work-logs/{YYYY-MM-DD}.md` に追記されます
- ブランチが `feature/{案件キー}-{工程}[-{機能ID}]` 形式の場合、`specs/{案件キー}/{工程dir}/{issue_id}/work-log.md` にも併記されます（`log_work.py` の実挙動）
- 本スキルはそのログに **NOTE エントリ** を追加します

---

## 入力形式

```
/work-log {自由記述メッセージ}
```

### 例

```
/work-log 工程1 SA（要件定義）完了、工程2 UI に進む
```

```
/work-log ItemController.getOne の実装完了。レビュー観点: 404 ハンドリング
```

```
/work-log 既存コードから Lombok 全削除完了（issue-007 一段落）
```

---

## 実行手順

### Step 1: 現在情報の取得

- 現在日時（`YYYY-MM-DD HH:MM:SS`）
- 実装者名（`git config user.name` → `USER` env → `USERNAME` env の順）
- 現在のブランチ（`git branch --show-current`）

### Step 2: デイリーログへ追記

`docs/changelog/work-logs/{YYYY-MM-DD}.md` を以下の形式で追記:

```markdown
- {HH:MM:SS} [{user}@{branch}] **NOTE**: {メッセージ}
```

ファイルが存在しなければ、ヘッダ付きで新規作成します（自動ログと同じヘッダ）。

### Step 3: issue 別ログへの追記（該当ブランチの場合）

現在のブランチが `feature/{案件キー}-{工程}[-{機能ID}]` 形式にマッチする場合、
`specs/{案件キー}/{工程dir}/{issue_id}/work-log.md` にも以下の形式で追記:

```markdown
- {YYYY-MM-DD} {HH:MM:SS} [{user}] **NOTE**: {メッセージ}
```

工程 issue の作業ディレクトリが存在しない場合は警告して停止（誤入力の保護）。

### Step 4: 結果報告

追記した内容と書き込み先パスをユーザーに表示。

```
✅ 作業ログに NOTE を追記しました。

📝 docs/changelog/work-logs/2026-05-19.md
   - 14:35:00 [tanaka@feature/inventory-2026-001-sa] **NOTE**: 工程1 SA 完了

📝 specs/inventory-2026-001/requirements/{issue_id}/work-log.md（工程 issue 別）
   - 2026-05-19 14:35:00 [tanaka] **NOTE**: 工程1 SA 完了
```

---

## 推奨される追記タイミング

- ✨ 工程切り替え時（SA→UI→SS-Plan→SS→PG-Plan→PG→UT-Plan→UT→リリース前ゲート）
- ✨ 重要な意思決定をしたとき（採用案・棄却案）
- ✨ レビューで指摘を受け修正方針を決めたとき
- ✨ 既知の問題に遭遇しブロックされたとき
- ✨ デプロイ・リリース・ロールバック等の節目

ファイル単位の変更履歴は自動ログが担うので、本スキルは **「なぜ・何のため」** の記録に集中してください。

---

## 関連

- 自動ログフック: `.claude/hooks/log_work.py`
- ログ格納先: `docs/changelog/work-logs/`
- 工程 issue 別ログ: `specs/{案件キー}/{工程dir}/{issue_id}/work-log.md`

---

## 作業指示

$ARGUMENTS
