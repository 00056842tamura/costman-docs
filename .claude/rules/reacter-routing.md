---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# ルーティング（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- （該当なし）

## ⚠ 非推奨 (AVOID)

- （該当なし）

## ✅ 必須 (ALWAYS)

- [STR-26] ✅ **必須 (ALWAYS)**: `window.open` でウィンドウを表示する場合、ウィンドウ名にアプリケーション名を入れるなど、他のアプリケーションと重複しないように工夫すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.8
- [STR-27] ✅ **必須 (ALWAYS)**: ルーティングは React Router（`react-router`）を利用すること。`BrowserRouter` の中に `Routes`、その中に `Route` を入れて定義する。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/04.ルーティング/README.md` 2-1
- [STR-28] ✅ **必須 (ALWAYS)**: ネスト構造のルーティングでは、親コンポーネントに `<Outlet />` を記載し、子ルートのコンポーネントが表示されるエリアを指定すること。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/04.ルーティング/README.md` 2-1 (7)

## ✨ 推奨 (PREFER)

- [STR-29] ✨ **推奨 (PREFER)**: ブラウザーの「戻る」を不可能にしたい（戻るボタン対策）場合は、`<Navigate replace />`・`<Link replace />`・`navigate(path, { replace: true })` のように `replace` を指定し、URL 履歴を残さないようにすること。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/04.ルーティング/README.md` 2-1 (6), 2-2, 2-4

## 条件付き事項（一覧）

- [参考] React18（共通部品 v1.x.x）を使用している場合、`react-router` のインポート元は `react-router-dom` に読み替える（React19 では `react-router`）。
  - **条件成立時: ✅必須（React18使用時）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/04.ルーティング/README.md` 1 Overview
- [参考] React18（共通部品 v1.x.x）を使用している場合、`navigate(...)` 呼び出しに `void` は不要（React19 では `void navigate(...)`）。
  - **条件成立時: 任意（その環境では付けない）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/04.ルーティング/README.md` 2-2
- [STR-30] アプリをサブパス配置する場合は `<BrowserRouter basename={ROUTER.BASE_NAME}>` を設定する（`ROUTER.BASE_NAME` は環境変数 `VITE_BASE_NAME` 由来。ローカルは通常空、本番はアプリのベースパス）。
  - **条件成立時: ✅必須（サブパス配置する場合）**

## 参考・推奨実装パターン
