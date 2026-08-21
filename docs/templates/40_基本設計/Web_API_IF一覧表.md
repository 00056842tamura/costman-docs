# Web API I/F 一覧表

| 項目 | 内容 |
|---|---|
| プロダクト名 | <!-- プロダクト名 --> |
| 作成日 | <!-- YYYY/MM/DD --> |
| 作成者 | <!-- 氏名 --> |
| 最終更新日 | <!-- YYYY/MM/DD --> |
| 最終更新者 | <!-- 氏名 --> |

## API 一覧

> **機能ID**（`{カテゴリ}-{3桁連番}`・カテゴリは `bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter`・`docs/base-design/機能一覧.md` で採番）と **API ID**（本表で維持する既存識別子）は 1:1 対応。

| No. | 機能ID | API ID | API名 | Method | Path | API説明 |
|---|---|---|---|---|---|---|
| 1 | <!-- bs-001 --> | API0001 | <!-- API名 --> | GET | <!-- /resources --> | <!-- 説明 --> |
| 2 | <!-- us-inter-001 --> | API0002 | <!-- API名 --> | GET | <!-- /resources/{id} --> | <!-- 説明 --> |
| 3 | <!-- us-intra-001 --> | API0003 | <!-- API名 --> | POST | <!-- /resources --> | <!-- 説明 --> |
| 4 | <!-- bs-002 --> | API0004 | <!-- API名 --> | PUT | <!-- /resources/{id} --> | <!-- 説明 --> |
| 5 | <!-- batch-001 --> | API0005 | <!-- API名 --> | DELETE | <!-- /resources/{id} --> | <!-- 説明 --> |

### Method 定義

| Method | 用途 |
|---|---|
| GET | リソースの取得 |
| POST | リソースの新規作成 |
| PUT | リソースの更新（全件置換） |
| PATCH | リソースの部分更新 |
| DELETE | リソースの削除 |

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | <!-- YYYY/MM/DD --> | <!-- 氏名 --> | 初版 |
