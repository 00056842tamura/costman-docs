---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# CSS の構成と役割（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- [CSS-01] 🚫 **禁止 (NEVER)**: CSS を記述するときに `!important` を使用しないこと。
  - 補足: `!important` を使うとその値が最優先になり、後で上書きできず統制が取れなくなるため基本的に使用しない。

## ⚠ 非推奨 (AVOID)

## ✅ 必須 (ALWAYS)

- （該当なし）

## ✨ 推奨 (PREFER)

- [CSS-03] ✨ **推奨 (PREFER)**: 一般の場合、画面ごとのスタイルは CSS モジュール、複数画面にまたがるスタイルはグローバル CSS で実装すること。
- [CSS-04] ✨ **推奨 (PREFER)**: 一般の場合、レスポンシブ対応を意識したレイアウト定義をすること（`flex`・`grid`・`margin` を使うなど）。

## 条件付き事項（一覧）

- [参考] `position: absolute` / `top` / `left` は、重ね合わせ（要素の重ね表示）をする場合は使用可。
  - **条件成立時: 任意（⚠非推奨の例外許容）**
- [参考] CSS Modules（`コンポーネント名.module.css`）は Vite の仕組みを利用するため、追加ライブラリのインストールは不要。
  - **条件成立時: 任意（既定挙動の説明）**

## 参考・推奨実装パターン

- **共通部品スタイルの上書き例**:
  ```css
  /* Feature1.module.css */
  :local(.myFeature) :global(.reacterMessageAreaErrorArea) {
    color: blue;
  }
  ```
