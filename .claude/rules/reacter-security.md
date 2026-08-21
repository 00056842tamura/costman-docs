---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# セキュリティ（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- [SEC-01] 🚫 **禁止 (NEVER)**: 外部からの入力値を使用して HTML の動的な生成を行わないこと。以下の関数・プロパティの使用は禁止。
    - `document.write`
    - `innerHTML`
    - `dangerouslySetInnerHTML`（`innerHTML` の React での代替）
- [SEC-02] 🚫 **禁止 (NEVER)**: 直接 DOM 操作をしないこと。以下は使用禁止。
    - `innerText`
- [SEC-03] 🚫 **禁止 (NEVER)**: Content Security Policy（CSP）のディレクティブの値で指定するオリジンに「ワイルドカード（`*`）のみ」を使用しないこと（全オリジンを許可してしまうため）。
    - ❌ NG: `connect-src *` / `connect-src 'self' *` / `connect-src 'self' www.example.com *`
    - ⭕ OK: `connect-src 'self' *.example.com` / `connect-src 'self' www.example.com`
- [COM-02] 🚫 **禁止 (NEVER)**: 操作ユーザに見せるべきでないデータを取得し、画面側で非表示にしないこと（参照可能なデータのみ取得すること）。
- [COM-01] 🚫 **禁止 (NEVER)**: サーバ通信時にサーバエラーが発生した場合、サーバからのエラーメッセージをそのまま画面に表示しないこと。
- [SEC-04] 🚫 **禁止 (NEVER)**: `window.alert` を使用しないこと。

## ⚠ 非推奨 (AVOID)

- （該当なし）

## ✅ 必須 (ALWAYS)

- [SEC-05] ✅ **必須 (ALWAYS)**: 動的な値の表示は、React のデフォルトの XSS 保護（JSX データバインディング構文 `{...}`）を用いて行うこと。
- [SEC-06] ✅ **必須 (ALWAYS)**: API キーやクライアント ID などの機密情報は、環境変数に記述すること（コード内へのハードコーディングは不可）。
- [SEC-07] ✅ **必須 (ALWAYS)**: `tabIndex` は `-1〜32766` の範囲で設定すること。
- [SEC-08] ✅ **必須 (ALWAYS)**: `z-index` は `0〜2147483633` の範囲で設定すること。
- [SEC-09] ✅ **必須 (ALWAYS)**: ローカル開発環境以外のビルドでは、SourceMap（`.js.map`）を生成しないよう Vite の設定（`build.sourcemap: false`）を行うこと（圧縮前の TypeScript が開発者ツールで閲覧可能になりセキュリティリスクになるため）。
    - 関連: ビルド設定（`sourcemap` を `true` にしない）としての扱いは `reacter-app-config.md`（🚫）を参照。同一設定をセキュリティ観点で必須化したもの。

## ✨ 推奨 (PREFER)

- （該当なし）

## 条件付き事項（一覧）

- [SEC-21] CSP の `style-src` に `'unsafe-inline'` を許可するのは、インラインスタイルを使用する共通部品やサードパーティコンポーネントを使用する場合。使用しない場合は `'self' 'nonce-development-mode-only'`（ローカル環境）でよく、本番では不要。
  - **条件成立時: ✅必須（インラインスタイルを使用する場合）**
- [SEC-22] CSP の `script-src` は、Vite が埋め込むインラインスクリプトを許可するため、ローカル環境（`development` モード）のみ `'self' 'nonce-development-mode-only'` を設定する。
  - **条件成立時: ✅必須（該当環境では設定する）**
- [SEC-20] Blob URL を使用した画像表示が必要な場合は `img-src` に `'self' blob:` を、Blob URL の API 呼び出しが必要な場合は `connect-src` に `'self' blob:` を設定できる。
  - **条件成立時: ✅必須（Blob URLを使用する場合）**
- [SEC-19] 外部 API を呼び出す場合は `connect-src` に `'self' <ドメイン名>`（例: `api.example.com`）を設定できる。
  - **条件成立時: ✅必須（外部APIを呼び出す場合）**
- [SEC-23] Azure AD 認証を使用する場合は `connect-src` に `'self' login.microsoftonline.com` を設定できる。SSO ありの場合はさらに `frame-ancestors` と `frame-src` に `'self' login.microsoftonline.com` を設定する。
  - **条件成立時: ✅必須（Azure AD/SSOを使用する場合）**

## 参考・推奨実装パターン

- CSP は HTTP の `Content-Security-Policy` レスポンスヘッダー、または `<meta http-equiv="Content-Security-Policy" content="...">` で有効化する。`default-src`・`style-src`・`script-src`・`img-src`・`connect-src`・`frame-ancestors` などのディレクティブでアプリの振る舞いに応じたポリシーを設定する。標準的な設定例: `default-src 'self'; style-src 'self' 'unsafe-inline'; frame-ancestors 'none'`。
- CSP の `frame-ancestors 'none'` はクリックジャッキング対策として用いる。
- 絶対パス URL を生成・許可する際は、許可するオリジンを限定する（ワイルドカード単独を避ける）。詳細は MDN「コンテンツセキュリティポリシー (CSP)」を参照。
