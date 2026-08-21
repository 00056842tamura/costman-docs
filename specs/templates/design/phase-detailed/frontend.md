# {機能名} フロントエンド詳細設計（Reacter）

> 工程4 SS 詳細設計WB — {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/frontend.md に配置（specs・push・PR対象）
> **1 機能ID = 1 画面**（`docs/base-design/機能一覧.md` の分解単位に合わせる）。

## 基本情報

| 項目 | 値 |
|-----|---|
| 機能ID | {機能一覧.md の機能ID、例: front-inter-001} |
| 要件ID | {要件一覧.md の要件ID、例: R001} |
| 画面名 | {画面名} |
| コンポーネント名 | {コンポーネント名}（例: XxxListPage） |
| ルートパス | {ルートパス}（例: /xxx） |
| Feature ディレクトリ | `src/features/{機能名}/` |
| 対応 US-API エンドポイント | `{HTTP メソッド} /api/v1/{リソース}` |

---

## コンポーネント構成

```
XxxListPage
├── XxxSearchForm          ... 検索条件フォーム（React Hook Form + Zod）
│   ├── InputEx            ... 入力コンポーネント
│   └── SubmitButton       ... 検索ボタン
├── MessageArea            ... エラー・情報メッセージ表示
└── XxxListGrid            ... 一覧グリッド（AG-Grid）
```

## コンポーネント仕様

| コンポーネント名 | Props | 主な State | 主な処理 |
|---|---|---|---|
| `XxxListPage` | なし | `loading`, `error`, `items` | データ取得・画面制御 |
| `XxxSearchForm` | `onSubmit: (values: XxxSearchParams) => void` | フォーム値（React Hook Form） | 検索条件入力 |
| `XxxListGrid` | `items: XxxItem[]` | なし | 一覧表示 |

## API 接続仕様

| 操作 | HTTP | エンドポイント | リクエスト型 | レスポンス型 | エラー処理 |
|---|---|---|---|---|---|
| 一覧取得 | GET | `/api/v1/xxx` | `XxxSearchParams` | `XxxItem[]` | 400: バリデーションエラー表示 / 500: MessageArea にエラー表示 |
| 登録 | POST | `/api/v1/xxx` | `XxxCreateRequest` | `XxxItem` | 409: 重複エラー表示 |

## 入力バリデーション（Zod スキーマ）

| フィールド名 | 型 | バリデーションルール | エラーメッセージ |
|---|---|---|---|
| <!-- name --> | `string` | <!-- 必須・最大 50 文字 --> | <!-- 「名称を入力してください」 / 「名称は 50 文字以内で入力してください」 --> |
| <!-- code --> | `string` | <!-- 必須・半角英数字 5 桁 --> | <!-- 「コードを入力してください」 / 「コードは半角英数字 5 桁で入力してください」 --> |

## 画面遷移

| 操作 | 遷移先 | 条件 |
|---|---|---|
| 行クリック | `/xxx/:id`（詳細画面・別機能ID） | — |
| 新規登録ボタン | `/xxx/create`（登録画面・別機能ID） | — |
| 登録完了 | `/xxx`（一覧・別機能ID） | 登録成功時 |

## アクション定義

> 画面操作（ボタン押下・行選択等）ごとに、**トリガーから処理完結までの一連のシーケンス**を定義する。
> 「画面遷移」表とは役割が異なる：画面遷移表は**遷移先**に着目し、本表は**操作のきっかけから処理完結までの処理シーケンス**（検証・API 呼出・状態更新・画面遷移の順序）に着目する。
> 各行の「処理内容」は API 接続仕様・入力バリデーション・画面遷移・状態管理の各表と整合させること（ハンドラーが呼ぶエンドポイント・遷移先・更新する state はそれぞれの表の記載と一致させる）。

| 操作（トリガー） | 対象コンポーネント | ハンドラー関数名 | 処理内容（シーケンス） | 事前条件／確認ダイアログ |
|---|---|---|---|---|
| 検索ボタン押下 | `XxxSearchForm` | `handleSearchSubmit` | ① Zod スキーマで入力検証 → ② API 接続仕様「一覧取得」を呼出 → ③ `items`/`error` state を更新 → ④ 画面遷移なし | 事前条件: なし／確認ダイアログ: なし |
| 新規登録ボタン押下 | `XxxCreateForm` | `handleCreateSubmit` | ① Zod スキーマで入力検証 → ② 確認ダイアログ表示 → ③ API 接続仕様「登録」を呼出 → ④ `loading`/`error` state を更新 → ⑤ 成功時「画面遷移」表の遷移先へ遷移 | 事前条件: フォーム入力済み／確認ダイアログ: あり（<!-- 「登録してよろしいですか？」等の確認文言 --> ） |
| <!-- 削除ボタン押下等、破壊的操作を追加 --> | <!-- 対象コンポーネント --> | <!-- handleXxxClick --> | <!-- ①確認ダイアログ表示 → ②API呼出 → ③state更新（一覧再取得等）→ ④画面遷移の有無 --> | 事前条件: <!-- 行選択済み等 -->／確認ダイアログ: <!-- あり・なしと文言 --> |

## 状態管理

| データ | 管理方法 | 説明 |
|---|---|---|
| 一覧データ | React Query (`useQuery`) | サーバー状態として管理 |
| フォーム値 | React Hook Form | フォーム状態として管理 |
| 画面表示状態 | `useState` | ローカル UI 状態 |

---

## 共通型定義

```typescript
// src/features/{機能名}/types/xxx.ts
type XxxItem = {
  id: string;
  name: string;
  // ...
};

type XxxSearchParams = {
  name?: string;
  // ...
};

type XxxCreateRequest = {
  name: string;
  // ...
};
```
