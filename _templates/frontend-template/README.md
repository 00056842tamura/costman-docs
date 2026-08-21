# frontend-template（Reacter 受入用ひな形）

`frontend/` スタック（Reacter SPA フロント）を新規作成する際の受け皿です。
Springer 系（`bs-template/` 等）と同じ構造（`_received/` サブディレクトリ）を持ちます。

## 資材の受け皿 `_received/`

標準チームから配布された Reacter ワイヤーフレーム（`reacter-blank-template`。GitLab 管理・zip 配布）を
展開したものを、このディレクトリ配下の `_received/` に配置してください。

```
_templates/frontend-template/
├── README.md       … 本ファイル（型のひな形。追跡対象）
└── _received/       … 【受領資材の展開先。.gitignore 対象・リモートにpushしない】
    └── （reacter-blank-template を展開した内容をここに配置）
```

## 配置後の手順

1. `reacter-blank-template` の zip を入手する（GitLab 管理。入手経路は標準チームに確認する）。
2. 展開した内容を `_templates/frontend-template/_received/` にコピーする。
3. `/stack-init` を実行する（`スタック種別: frontend`）。
   - `_received/` の資材存在確認 → `frontend/` への反映（コピー） → `npm install && npm run build` によるビルド確認、まで一括で行う。

## 注意事項

- `_received/` に配置した資材そのものはリモートにpushしない（常に標準チームの最新版・確定版を都度受領する運用のため）。
- 本 README（`_received/` 以外）は通常のひな形ファイルとして追跡・管理する。改善要望がある場合は標準チームに相談すること（直接編集しない）。
