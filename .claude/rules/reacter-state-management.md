---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# 状態管理（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- （該当なし）

## ⚠ 非推奨 (AVOID)

- [QLT-28] セッションストレージをクリアするときに `sessionStorage.clear()` でストレージ全体をクリアしないこと（ライブラリが保存したデータまで削除されてしまうため）。
  - 代替: `sessionStorage.removeItem("key")` のようにキーを指定して削除すること。

## ✅ 必須 (ALWAYS)

- [QLT-01] `addEventListener` でイベントリスナーを登録した場合、不要になるとき（例: 他画面への遷移時など）に `removeEventListener` で必ず削除すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.12

## ✨ 推奨 (PREFER)

- [QLT-02] `useContext` を使う場合、`Provider` で囲むコンポーネントを必要最小限に限定すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.12
- [QLT-03] オブジェクト型・配列型の定数は、末尾に `as const` を付けることで内部要素を変更不可にすること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.12
- [QLT-04] 状態管理は `useState` を基本とし、コンポーネント階層が深く props による受け渡しが煩雑になる場合に `useContext` を利用すること。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/02.状態管理/01.useContextの使い方.md` 1-1
- [QLT-05] `createContext` の初期値には `null` を指定すること（初期値が利用されるのは Provider 配下にないコンポーネントから利用した＝誤用のケースであるため）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/02.状態管理/01.useContextの使い方.md` 2-1 (1)

## 条件付き事項（一覧）

- [QLT-29] React18（共通部品 v1.x.x）を使用している場合、Provider は `<コンテキスト.Provider value={...}>` の形式にする必要がある（React19 では `<コンテキスト value={...}>` が可）。
  - **条件成立時: ✅必須（React18使用時）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/02.状態管理/01.useContextの使い方.md` 2-1 (2)

## 参考・推奨実装パターン

### [REF-QLT-01] Contextを専用ファイルにまとめる

- Context は専用ファイル（例: `/src/providers/LangStore.tsx`）に、共有する状態の型・`createContext`・Provider コンポーネントをまとめて定義する。
