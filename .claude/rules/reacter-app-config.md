---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# アプリ設定（package.json / Vite / index.html / 環境変数 / テンプレート）（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- [SEC-10] 🚫 **禁止 (NEVER)**: `package.json` の `dependencies` / `devDependencies` に不要なライブラリを記述しない（必要なライブラリのみ記述すること）。
- [SEC-11] 🚫 **禁止 (NEVER)**: `package.json` のライブラリバージョンに `*` / `^` / `~` を使用しない（バージョンをプロジェクト内で統一・固定するため）。
- [SEC-09] 🚫 **禁止 (NEVER)**: `vite.config.ts` の `sourcemap` を編集しない（`true` にしない。本番環境のエラー解析は検証環境で実施する方針のため `false` のまま）。
  - 関連: SourceMap 無効化のセキュリティ上の理由は `reacter-security.md`（✅必須）を参照。本ファイルはビルド設定としての扱いを記載する。

## ⚠ 非推奨 (AVOID)

- （該当なし）

## ✅ 必須 (ALWAYS)

- [SEC-12] ✅ **必須 (ALWAYS)**: ライブラリのバージョンはプロジェクト内で統一すること（`*`/`^`/`~` を使わない、または `package-lock.json` を共有して固定する）。
- [SEC-13] ✅ **必須 (ALWAYS)**: `npm install` でライブラリを追加する場合は、開発者の承認を得ること。
- [SEC-14] ✅ **必須 (ALWAYS)**: `package.json` の `name` をプロジェクト名等に変更すること。
- [SEC-15] ✅ **必須 (ALWAYS)**: 本番ビルド（`prod`）では `esbuild` において console と debugger を削除すること。
- [SEC-16] ✅ **必須 (ALWAYS)**: `index.html` の `<title>` に、ブラウザのタイトルバー／タブに表示されるタイトルを記述すること。
- [SEC-17] ✅ **必須 (ALWAYS)**: 環境変数のプロパティ名は必ず `VITE_` で始めること。
- [SEC-18] ✅ **必須 (ALWAYS)**: 環境変数ファイルに定義したプロパティは、型定義ファイル `/src/vite-env.d.ts` の `ImportMetaEnv` に追記すること（`as` 不要化・入力補完のため）。
- [SEC-06] ✅ **必須 (ALWAYS)**: API キーやクライアント ID などの機密情報は環境変数に記述すること（コード内へのハードコーディングは不可）。

## ✨ 推奨 (PREFER)

- （該当なし）

## 条件付き事項（一覧）

- [参考] リバースプロキシ設定（`vite.config.ts` の `proxy`）はデフォルトで US アプリ転送設定が記載されている。必要に応じて変更してよい（パス先頭の正規表現・`target`・`changeOrigin`・`rewrite`）。モードによる `target` 切替も条件分岐で可能。
  - **条件成立時: 任意（選択・設定変更）**
- [参考] ローカル環境の CSP ヘッダー出力設定（`vite.config.ts` の `server.headers` / `html.cspNonce`）はデフォルトで記載されている。必要に応じて修正してよい。
  - **条件成立時: 任意（選択・設定変更）**
- [参考] 環境変数を使って `index.html` 内のタイトルや CSP を書き換えることが可能（`html-transform` プラグインで `%=...%` 囲み箇所を環境変数に置換）。
  - **条件成立時: 任意（能力の提示）**
  - **実装例**:

    **① `index.html` — `%=変数名%` プレースホルダーを埋め込む**

    ```html
    <!doctype html>
    <html lang="ja">
      <head>
        <meta charset="UTF-8" />
        <!-- タイトルを環境変数で切り替え -->
        <title>%=VITE_APP_TITLE%</title>
        <!-- CSP を環境変数で切り替え -->
        <meta http-equiv="Content-Security-Policy" content="%=VITE_CSP%" />
      </head>
      <body>
        <div id="root"></div>
        <script type="module" src="/src/main.tsx"></script>
      </body>
    </html>
    ```

    **② `vite.config.ts` — `html-transform` プラグインを定義して置換処理を実装する**

    ```typescript
    import react from '@vitejs/plugin-react';
    import { defineConfig, loadEnv, Plugin } from 'vite';

    /** %=VITE_XXX% プレースホルダーを環境変数に置換するプラグイン */
    function htmlTransformPlugin(env: Record<string, string>): Plugin {
      return {
        name: 'html-transform',
        transformIndexHtml(html: string): string {
          return html.replace(/%=([^%]+)%/g, (_, key: string) => env[key] ?? '');
        },
      };
    }

    export default defineConfig(({ mode }) => {
      // 第3引数に '' を渡すと VITE_ プレフィックスなしの変数も取得できる
      const env = loadEnv(mode, process.cwd(), '');
      return {
        plugins: [react(), htmlTransformPlugin(env)],
        // ...（既存設定）
      };
    });
    ```

    **③ 環境変数ファイル — モードごとに値を定義する**

    `.env.development`（ローカル開発用）:
    ```
    VITE_APP_TITLE={product}（開発）
    VITE_CSP=default-src 'self'; connect-src 'self' http://localhost:8080; script-src 'self' 'nonce-{nonce}';
    ```

    `.env.production`（本番用）:
    ```
    VITE_APP_TITLE={product}
    VITE_CSP=default-src 'self'; connect-src 'self' https://api.example.com; script-src 'self';
    ```

    **④ 型定義 `/src/vite-env.d.ts` に追記する**（✅必須ルールに対応）

    ```typescript
    interface ImportMetaEnv {
      readonly VITE_APP_TITLE: string;
      readonly VITE_CSP: string;
      // ... 既存エントリ
    }
    ```

    > **注意**: `loadEnv` の第3引数を `''` にすると `VITE_` プレフィックスなし変数も取得できる。
    > CSP に nonce を使う場合は `vite.config.ts` の `server.headers` / `html.cspNonce` と組み合わせて設定すること（ローカル CSP 設定の条件付き事項を参照）。
- [参考] テンプレートのモードに加え、独自モードを追加してよい（環境変数ファイル作成＋ `package.json` に `build:[モード名]` スクリプト追記）。
  - **条件成立時: 任意（任意拡張）**
- [SEC-24] favicon を表示する場合は `public/blank.svg` を用意した svg に入れ替え、`index.html` の `href="/blank.svg"` を修正してよい（デフォルトは空 favicon で非表示）。
  - **条件成立時: ✅必須（faviconを表示する場合）**
- [参考] デフォルトと異なる設定の Axios インスタンスが必要な場合は `axiosConfig.ts` に用途別インスタンスを作成してよい。
  - **条件成立時: 任意（選択）**
