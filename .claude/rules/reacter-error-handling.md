---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# エラーハンドリング（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- （該当なし）

## ⚠ 非推奨 (AVOID)

- [QLT-06] `error.message`（エラーオブジェクトのメッセージ）をエラー画面にそのまま表示しないこと。
  - 代替: メッセージは画面表示用にラップ・抽象化し、内部エラーの詳細を露出させない。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 2-1 (1)

## ✅ 必須 (ALWAYS)

- [QLT-07] エラーバウンダリーを利用し、画面表示エラー（コンポーネントのレンダー中、または useEffect 等ライフサイクルメソッド実行中のエラー）を捕捉すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.9
- [QLT-08] エラーバウンダリー用のエラー画面（エラー画面コンポーネント）を、プロジェクトの要件に合わせてカスタマイズすること。
  - ⚠ パス表記の要確認: 原典間で表記が揺れている（instructions.md §2.9 は `src/components/error`、プログラミングガイドは `src/components/Error/ErrorPage.tsx`）。大文字小文字を含む正確なパスは、利用する `reacter-blank-template` の実体に合わせて確認すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.9 ／ `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 2-1
- [QLT-09] 関数コンポーネントで Error Boundary を利用する場合は `react-error-boundary` ライブラリを使用すること（React 標準の Error Boundary はクラスコンポーネントにのみ対応するため）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 3.1
- [QLT-10] エラーをキャッチしたいコンポーネント全体を `<ErrorBoundary FallbackComponent={エラー画面} >` で囲むこと。アプリケーション全体のエラーハンドラとするには一番外側で囲むこと。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 2-1
- [QLT-11] イベントハンドラー内・非同期処理内で発生するエラーは Error Boundary では自動捕捉されないため、`try-catch` 等で個別にハンドリングすること（エラーダイアログ表示・エラーメッセージ表示・エラーページ遷移など）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 2-2

## ✨ 推奨 (PREFER)

- [QLT-13] エラー画面のコンポーネントは、極力シンプルでエラーが発生しえない画面にすること（エラー画面のレンダー中にエラーが発生するとアプリがクラッシュするため）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/05.エラー処理/README.md` 2-1 注意

## 条件付き事項（一覧）

## 参考・推奨実装パターン

- エラー画面コンポーネントは `react-error-boundary` の `FallbackProps` 型から `error` を受け取る（エラーメッセージは `error.message`、ただし画面への直接表示は非推奨）。
- 非同期処理（`useEffect` 内の async 関数など）や `onClick` ハンドラ内では `try-catch` でエラーを捕捉し、`showBoundary(error)` を呼び出してエラー画面を表示する。
