---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# コーディング規約（ESLint/Prettier・TSDoc・実装）（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- [STR-01] 🚫 **禁止 (NEVER)**: ファイル全体に対して ESLint を無効化しないこと。
- [STR-02] 🚫 **禁止 (NEVER)**: `if` 内の処理を同一行に記述しないこと（改行すること）。例: `if (status === 0) value = "aaa";` は不可。
- [STR-03] 🚫 **禁止 (NEVER)**: `React.FC` を使用しないこと（使用しない傾向になってきているため）。
- [STR-04] 🚫 **禁止 (NEVER)**: プロミスチェーン `then()` の利用は禁止（非同期処理を待機して実行する場合は `async/await` を使用すること）。

## ⚠ 非推奨 (AVOID)

- [STR-05] ⚠ **非推奨 (AVOID)**: ESLint・Prettier の設定を、条件を緩くする方向に変更しない（条件を厳しくする変更は可）。
  - 代替: 緩和が必要な場合は個別に検討し、原則は既定ルールを維持する。
- [STR-06] ⚠ **非推奨 (AVOID)**: `React.strictMode` を使用しない（2 回レンダリングで開発者がバグと誤認する可能性があるため）。
  - 代替: StrictMode を外す、または影響を理解した上で限定的に使用する。
- [STR-07] ⚠ **非推奨 (AVOID)**: props バケツリレー（Props Drilling）を 4 階層以上行わない（3 階層まで推奨）。
  - 代替: `useContext` 等の状態共有手段を用いる。
- [STR-08] ⚠ **非推奨 (AVOID)**: TypeScript の型として `any` を使用しない（使用せざるを得ない場合は使用しても良い）。
  - 代替: 具体的な型・ジェネリクス・`unknown` を用いる。ESLint では `@typescript-eslint/no-explicit-any` を `WARN` に設定。

## ✅ 必須 (ALWAYS)

- [STR-09] ✅ **必須 (ALWAYS)**: 関数はアロー関数で記述すること（アロー関数で記述できない場合は function での記述を可とする）。
- [STR-10] ✅ **必須 (ALWAYS)**: 非同期処理を待機して実行する場合は、原則として `async/await` を使用すること（可読性・エラーハンドリングの観点）。
- [STR-11] ✅ **必須 (ALWAYS)**: TSDoc の記述方法で、すべての関数に説明（コメント）を記載すること（無名インラインコールバック関数は必須ではない）。

## ✨ 推奨 (PREFER)

## 条件付き事項（一覧）

- [参考] ESLint 設定は条件を厳しくする方向の変更は可。
  - **条件成立時: 任意（許容）**

## 参考・推奨実装パターン

- [REF-STR-01] **TSDoc の書き方**: TypeScript では型情報をコメントに明記しない。`@param パラメータ名 - 説明`（名前と説明の間にハイフン）、`@returns 説明` を記載する。
  ```ts
  /**
   * 2つの数値の平均値を返します。
   * @param x - 1つ目の数値
   * @param y - 2つ目の数値
   * @returns xとyの平均値
   */
  export const getAverage = (x: number, y: number): number => (x + y) / 2.0;
  ```
- [参考] **props のプロパティ別コメント**: props の `@param` ではプロパティ名ごとの説明を書けないため、Type 定義内のインラインコメント（`/** コメント */`）で記載する（TypeDoc 出力には反映されないが VSCode 補完で参照できる）。
- [参考] **ESLint 設定（要点）**: Airbnb ルールをベースに TypeScript 向けルールセット・型情報利用ルールセット・Prettier 競合回避ルールセットを有効化。主なプロジェクト独自設定:
  - `eqeqeq: error`（厳密等価を強制）、`no-nested-ternary: error`、`no-else-return: error`、`no-irregular-whitespace: error`（全角スペース等禁止）、`no-shadow: error`、`no-use-before-define: error`。
  - `no-alert: error`（`window.alert` 禁止）。
  - `react/destructuring-assignment: error`（分割代入強制）、`react/button-has-type: error`、`react/no-array-index-key: error`、`react/jsx-no-constructed-context-values: error`。
  - `consistent-return`・`no-bitwise`・`react-hooks/exhaustive-deps`・各種 `@typescript-eslint/no-unsafe-*`・`@typescript-eslint/no-explicit-any` は `WARN`。
  - `no-console` は無効（※ ただし本番ビルドでは esbuild で console を削除する。`reacter-app-config.md` 参照）。
- [参考] **VSCode**: 保存時 Prettier 整形＋ESLint 自動修正（`source.fixAll.eslint`）を有効化する（`reacter-app-config.md` 参照）。
