# 作業ログ (`docs/changelog/work-logs/`)

このディレクトリには、実装者の作業活動が **日別ファイルで自動記録** されます。

## 構成

```
docs/changelog/work-logs/
├── README.md                  ← このファイル
├── 2026-05-19.md              ← 2026-05-19 の作業ログ（自動生成）
├── 2026-05-20.md              ← 2026-05-20 の作業ログ（自動生成）
└── ...                        （以降、日付ごとに追加）
```

## 記録の仕組み

### 自動記録（フック）

`.claude/hooks/log_work.py` が Claude Code の **Write / Edit ツール実行のたび** に
今日の日付のファイルに 1 行追記します。

エントリ例:
```markdown
- 14:32:15 [tanaka@feature/inventory-2026-001-pg-bs-001] `bs/src/main/java/jp/co/example/{product}/bs/item/controller/ItemController.java`
```

各エントリの構成:
- 時刻（HH:MM:SS）
- 実装者名 (`git config user.name` から取得)
- ブランチ名 (`git branch --show-current` から取得・`feature/{案件キー}-{工程}[-{機能ID}]` 形式。機能ID は `{カテゴリ}-{3桁連番}`)
- 操作対象ファイル（プロダクトルートからの相対パス）

### 手動記録（スキル）

実装者の意思決定やマイルストーンは `/work-log` スキルで追記:

```
/work-log 工程1 SA（要件定義）完了、工程2 UI に進む
```

エントリ例:
```markdown
- 14:35:00 [tanaka@feature/inventory-2026-001-sa] **NOTE**: 工程1 SA（要件定義）完了、工程2 UI に進む
```

詳細は `.claude/skills/work-log/SKILL.md` を参照。

## 利用シーン

- **日次レビュー**: 今日何が変わったかを `cat docs/changelog/work-logs/$(date +%Y-%m-%d).md`
- **障害対応**: 「障害が起きた日に何を編集したか」を即座に追跡
- **進捗報告**: 週次レポートのインプットとして集計
- **コードレビュー**: 「PR にこのファイルが含まれている経緯」を確認
- **オンボーディング**: 新人が先輩の作業履歴を辿って学習

## 工程issue 別ログとの関係

ブランチ名が新ワークフロー形式 `feature/{案件キー}-{工程}[-{機能ID}]`（例: `feature/inventory-2026-001-sa`・`feature/inventory-2026-001-pg-bs-001`）の場合、
工程issue スコープの `work-log.md` にも同じ内容が併記されます。**記録先リポジトリは工程によって異なります**（案件リポ／スタックリポ、3 リポ構成）。

記録先の解決ロジック（`.claude/hooks/log_work.py`）:
- **案件リポ側・全体ゲート工程（sa / ui / ssplan）**: 案件リポの `specs/{案件キー}/meta.md` の「issue-id 対応表」から該当工程の issue-id を取得し、`specs/{案件キー}/{工程dir}/{issue_id}/work-log.md` に記録
- **スタックリポ側・スタック単位の全体ゲート工程（pgplan / ptplan）**: 対象スタックリポ（`{スタック}/`）内の `specs/{案件キー}/meta.md` から issue-id を取得し、`{スタック}/specs/{案件キー}/{工程dir}/{issue_id}/work-log.md` に記録
- **スタックリポ側・機能ID単位の area別工程（ss / pg / pt）**: ブランチの機能ID（`{カテゴリ}-{3桁連番}`）に一致する `{スタック}/specs/{案件キー}/{工程dir}/{issue_id}_{機能ID}/work-log.md` を全走査して記録
- **解決不能時**: 該当リポの `specs/{案件キー}/work-log.md` にフォールバック

工程dir 名の対応: sa→requirements / ui→base-design / ssplan→detail-design-plan / ss→detail-design / pgplan→implementation-plan / pg→implementation / ptplan→unit-test-plan / pt→unit-test。

| 観点 | デイリーログ | 工程issue 別ログ |
|---|---|---|
| パス | `docs/changelog/work-logs/{date}.md` | 案件リポ: `specs/{案件キー}/{工程dir}/.../work-log.md` ／ スタックリポ: `{スタック}/specs/{案件キー}/{工程dir}/.../work-log.md` |
| 範囲 | その日の全プロダクト・全工程 | 該当工程issue のみ・複数日にまたがる |
| 用途 | 日次レビュー・全体俯瞰 | 工程issue 単位のトレーサビリティ |

specs も通常の git 管理対象・PR マージ対象のため、work-log.md も各リポ（SA/UI/SS-Plan は案件リポ、SS 以降はスタックリポ）のコミット履歴として残ります。

## 除外対象

以下は **記録されません**（無限ループ・ノイズ防止）:

- `docs/changelog/work-logs/` 配下のファイル（自分自身）
- 任意の `work-log.md`（issue 別ログを含む）

## ログのコミット運用

- ✅ 推奨: 通常の PR と一緒にコミット（プロジェクト履歴として保存）
- ⚠️ 注意: 同日に複数人が同じファイルに追記する場合、まれにマージコンフリクト発生
  - その場合は追記行を両方残せば OK（順序は時刻順）

## 月次アーカイブ（自動）

`.claude/hooks/log_work.py` が、Write/Edit のたびに **前月以前のデイリーログを自動で月次アーカイブ** します。

### アーカイブ先

```
docs/changelog/work-logs/
├── README.md
├── 2026-05-19.md              ← 当月のログはここに残る
├── 2026-05-20.md
├── ...
└── archive/
    ├── 2026-03/               ← 月単位でグルーピング
    │   ├── 2026-03-01.md
    │   ├── 2026-03-02.md
    │   └── ...
    └── 2026-04/
        ├── 2026-04-01.md
        └── ...
```

### 動作

- 当月（`YYYY-MM` が今月と一致）のログは **そのまま** `work-logs/` 直下に残ります
- 前月以前（`YYYY-MM` が今月より過去）のログは `archive/{YYYY-MM}/` に **自動移動** されます
- 冪等処理: すでにアーカイブ済みのファイルは再度動かしません（重複名の場合はスキップ）
- 月の切り替わり初日に最初の Write/Edit が走ったタイミングで前月分が一括移動されます

### 過去ログの参照

```bash
# 2026 年 3 月分のログを横断確認
$ cat docs/changelog/work-logs/archive/2026-03/*.md

# 特定日（過去）のログ
$ cat docs/changelog/work-logs/archive/2026-03/2026-03-15.md
```

### 手動アーカイブが必要なケース

通常は自動で動きますが、以下の場合は手動で移動が必要です:
- 長期間 Claude Code を使わず、新しい月になっても自動アーカイブが走らなかった
- 過去のログを `archive/` の構造に揃えたい初回セットアップ時

```bash
# Unix/Linux/macOS
$ cd docs/changelog/work-logs/
$ for f in 2026-03-*.md; do
    mkdir -p archive/2026-03/
    mv "$f" archive/2026-03/
  done

# Windows PowerShell
PS> Set-Location docs/changelog/work-logs/
PS> Get-ChildItem 2026-03-*.md | ForEach-Object {
      New-Item -ItemType Directory -Force -Path archive/2026-03 | Out-Null
      Move-Item $_.FullName archive/2026-03/
    }
```

### より古いアーカイブの整理（任意）

数年分蓄積したら以下のいずれかで二次整理を検討:
- 年単位でディレクトリを集約: `archive/{YYYY}/` 配下にまとめる
- 月次集約ファイルに圧縮: `archive/{YYYY-MM}-summary.md` を生成して個別ファイルを削除
- 監査要件に応じた長期保管庫へ移送

整理計画は `docs/changelog/PENDING_CLEANUP.md` のローテーション項目を参照。

## トラブルシューティング

### Q. ログが記録されない

**確認事項**:
- Python が PATH に通っているか（`python3 --version` または `python --version`）
- `.claude/settings.json` の hooks に `log_work.py` が登録されているか
- フックが Write/Edit 実行時に動いているか（試しに何かファイルを編集してみる）

### Q. ブランチ名が `(no-branch)` や `(unknown)` になる

**原因**: Git リポジトリでない、または `git` コマンドが PATH にない、もしくは detached HEAD。
**対応**: `git status` で状態を確認、または `git checkout {branch}` で明示ブランチに移る。

### Q. 実装者名が `(unknown)` になる

**原因**: `git config user.name` 未設定で、`USER` / `USERNAME` 環境変数も無効。
**対応**:
```bash
$ git config --global user.name "あなたの名前"
```
