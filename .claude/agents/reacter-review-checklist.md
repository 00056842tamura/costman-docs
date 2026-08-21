# Reacter レビュー観点台帳

`/reacter-code-review` のクラスタ別レビューエージェント（C1〜C6）が**起点として参照する単一チェックリスト**。
各観点は 1 行 = 1 チェック。詳細な規約文言・実装例が必要な場合のみ「出典」のルールファイル（`.claude/rules/reacter-{出典}.md`）を開く（全ルールの常時全読込は不要）。

**凡例**（制約レベルは `.claude/rules/rule-overview.md` に準拠）
- 制約レベル: `🚫`=禁止(NEVER) / `⚠`=非推奨(AVOID) / `✅`=必須(ALWAYS) / `✨`=推奨(PREFER) / `条件付き`=条件成立時のみ適用 / `参考`=参考実装パターン（**判定対象外**）
- 分類（指摘の重大度。制約レベルから機械決定）: `🚫`/`✅` → **MUST** ／ `⚠`/`✨` → **SHOULD** ／ 条件付き(任意)・参考・ルール非紐づけ → **MAY**（条件付きで条件成立時✅必須のものは **MUST(条件)** と表記）
- 判定: 各エージェントは**自クラスタの観点すべて**に `準拠 / 違反 / 非該当` のいずれかを必ず付与し、**未評価 0 件**を保証する

**この台帳の対象範囲**
- クラスタ C1〜C6（＝レビュー観点 b「ルール準拠」＋ c「エラーハンドリング漏れ」のうちルールに紐づく部分）。
- レビュー観点 a「設計書整合」・d「重複・可読性・保守性」は**横断レビュー（`reacter-review-crosscut`）が担当**し、本台帳の網羅ゲートの対象外。
- **単一オーナー原則**: 各観点はちょうど1クラスタが所有する。`reacter-coding-standards` / `reacter-folder-structure` は C5 単独所有。コード全域に現れる記法・型・命名・lint（ubiquitous）はドメインルールが再掲していても C5 所有（ドメインクラスタは判定せずリンクのみ）。
- **規範行（各クラスタの C-行）**は 🚫/⚠/✅/✨ と規範的トリガーを持つ条件付き事項を計上し、**未評価0件ゲートの対象**とする。
- **参考・推奨実装パターン由来の観点は付録「参考観点リスト（`REF-`）」に分離**する（規範性なし）。**未評価0件ゲートの対象外**で、クラスタエージェントは規範行を全件判定したうえで明確な逸脱があれば **MAY として任意報告**してよい（報告は義務ではない。本ファイル末尾の付録参照）。

---

## C1. 通信（`COM-`）

担当出典: `reacter-server-communication` / `reacter-cache-control`

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| COM-01 | サーバエラー発生時、サーバのエラーメッセージをそのまま画面表示しない | 🚫 | MUST | server-communication | サーバ通信時 |
| COM-02 | 操作ユーザに見せるべきでないデータを取得して画面側で非表示にしない（参照可能データのみ取得） | 🚫 | MUST | server-communication | サーバ通信時 |
| COM-03 | Axios タイムアウトをプロジェクト要件に合わせて見直す（`src/lib/axiosConfig.ts`） | ✅ | MUST | server-communication | 常時 |
| COM-04 | `axiosConfig.ts` の Axios インスタンスを使用（変更時は既存編集 or インスタンス追加） | ✨ | SHOULD | server-communication | サーバ通信時 |
| COM-05 | 一覧形式の取得データ件数に上限を設定 | ✨ | SHOULD | server-communication | 一覧表示時 |
| COM-06 | ハッシュ非付与コンテンツ（`index.html`・`/public`）はブラウザにキャッシュさせない | ✅ | MUST | cache-control | 常時 |
| COM-07 | HTTP で取得する静的ファイルのキャッシュ制御を行う | ✨ | SHOULD | cache-control / server-communication | 静的ファイル取得時 |
| COM-08 | 時間がかかる処理（サーバ通信・ファイルアップロード/ダウンロード等）はインジケーターを表示し処理完了前のユーザ操作を受け付けない | ✅ | MUST | server-communication | サーバ通信・ファイルIO時 |

---

## C2. セキュリティ・ビルド（`SEC-`）

担当出典: `reacter-security` / `reacter-app-config`

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| SEC-01 | `document.write` / `innerHTML` / `dangerouslySetInnerHTML` を使わない | 🚫 | MUST | security | 常時 |
| SEC-02 | `innerText` 等の直接 DOM 操作をしない | 🚫 | MUST | security | 常時 |
| SEC-03 | CSP のオリジンに「ワイルドカード（`*`）のみ」を使わない | 🚫 | MUST | security | CSP 設定時 |
| SEC-04 | `window.alert` を使わない | 🚫 | MUST | security | 常時 |
| SEC-05 | 動的値の表示は JSX データバインディング `{...}` で行う | ✅ | MUST | security | 常時 |
| SEC-06 | API キー・クライアント ID 等の機密情報は環境変数に記述（ハードコード禁止） | ✅ | MUST | security / app-config | 常時 |
| SEC-07 | `tabIndex` は `-1〜32766` の範囲 | ✅ | MUST | security | tabIndex 使用時 |
| SEC-08 | `z-index` は `0〜2147483633` の範囲 | ✅ | MUST | security | z-index 使用時 |
| SEC-09 | ローカル以外のビルドで SourceMap を生成しない（`build.sourcemap:false`・`vite.config.ts` を編集しない） | ✅ | MUST | security / app-config | ビルド設定時 |
| SEC-10 | 不要なライブラリを `dependencies` / `devDependencies` に書かない | 🚫 | MUST | app-config | package.json |
| SEC-11 | ライブラリバージョンに `*` / `^` / `~` を使わない | 🚫 | MUST | app-config | package.json |
| SEC-12 | ライブラリバージョンをプロジェクト内で統一・固定（package-lock 共有） | ✅ | MUST | app-config | package.json |
| SEC-13 | `npm install` でのライブラリ追加は開発者の承認を得る | ✅ | MUST | app-config | ライブラリ追加時 |
| SEC-14 | `package.json` の `name` をプロジェクト名に変更 | ✅ | MUST | app-config | package.json |
| SEC-15 | 本番ビルド（prod）で console と debugger を削除（`esbuild.drop`） | ✅ | MUST | app-config | 本番ビルド設定時 |
| SEC-16 | `index.html` の `<title>` を記述 | ✅ | MUST | app-config | index.html |
| SEC-17 | 環境変数のプロパティ名は `VITE_` で始める | ✅ | MUST | app-config | 環境変数定義時 |
| SEC-18 | env のプロパティを `src/vite-env.d.ts` の `ImportMetaEnv` に追記 | ✅ | MUST | app-config | 環境変数定義時 |
| SEC-19 | CSP: 外部 API 呼び出し時は `connect-src` に `'self' <ドメイン>` を設定（ワイルドカード単独不可） | 条件付き | MUST(条件) | security | 外部 API 呼出時 |
| SEC-20 | CSP: Blob URL 使用時は `img-src`/`connect-src` に `'self' blob:` を設定 | 条件付き | MUST(条件) | security | Blob URL 使用時 |
| SEC-21 | CSP: インラインスタイル使用時は `style-src` に `'self' 'unsafe-inline'` を設定 | 条件付き | MUST(条件) | security | インラインスタイル使用時 |
| SEC-22 | CSP: ローカル環境（development モード）のみ `script-src` に `'self' 'nonce-development-mode-only'` を設定 | 条件付き | MUST(条件) | security | development モード時 |
| SEC-23 | CSP: Azure AD 認証使用時は `connect-src` に `login.microsoftonline.com`、SSO 時は `frame-ancestors`/`frame-src` にも設定 | 条件付き | MUST(条件) | security | Azure AD/SSO 使用時 |
| SEC-24 | favicon を表示する場合は `public/blank.svg` を差し替え `index.html` の `href` を修正 | 条件付き | MUST(条件) | app-config | favicon 表示時 |

---

## C3. スタイル（`CSS-`）

担当出典: `reacter-css`

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| CSS-01 | CSS で `!important` を使わない | 🚫 | MUST | css | 常時 |
| CSS-02 | `position:absolute` / `top` / `left` での絶対値位置固定を極力避ける（重ね合わせ時は可） | ⚠ | SHOULD | css | 常時（重ね合わせ時は非該当） |
| CSS-03 | 画面ごとは CSS Modules、複数画面共通はグローバル CSS で実装 | ✨ | SHOULD | css | 常時 |
| CSS-04 | レスポンシブ対応を意識したレイアウト（flex/grid/margin） | ✨ | SHOULD | css | 常時 |

---

## C4. フォーム・バリデーション（`FRM-`）

担当出典: `reacter-form-validation`
※ submit の `then()` 禁止は ubiquitous 記法のため **STR-04 が所有**（本クラスタでは判定しない）。型/スキーマの配置規約は **STR-22/STR-23 が所有**。

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| FRM-01 | 入力値管理は React Hook Form を使用 | ✅ | MUST | form-validation | フォーム実装時 |
| FRM-02 | バリデーションは React Hook Form + Zod を使用 | ✅ | MUST | form-validation | バリデーション時 |
| FRM-03 | `string` 必須チェックは `.min(1)` を併用（空文字を弾く） | ✅ | MUST | form-validation | 必須チェック時 |
| FRM-04 | `number`/`Date` は `setValueAs` または `coerce`/`preprocess` で変換 | ✅ | MUST | form-validation | number/Date 項目時 |
| FRM-05 | `onSubmit` は `void` を返す（`void handleSubmit(...)(e)`） | ✅ | MUST | form-validation | フォーム submit 時 |
| FRM-06 | 複数送信先/一部項目は `type=button`+`trigger()`、全項目は `type=submit` | ✅ | MUST | form-validation | ボタン押下処理時 |
| FRM-07 | `trigger()` は Promise を返すため後続処理は `async/await` で待機 | ✅ | MUST | form-validation | trigger 使用時 |
| FRM-08 | 型変換は原則 `setValueAs` を利用（`preprocess` でなく） | ✨ | SHOULD | form-validation | 型変換時 |
| FRM-09 | 全エラーメッセージ共通変更は `ZodErrorMap`（個別はスキーマで上書き） | ✨ | SHOULD | form-validation | 共通メッセージ変更時 |
| FRM-10 | `refine()` 複数項目チェックは別/複合プロパティのパスに紐づける | ✨ | SHOULD | form-validation | 複数項目チェック時 |
| FRM-11 | modal/message-box 表示時は `shouldFocusError:false` | ✨ | SHOULD | form-validation | modal/message-box 表示時 |
| FRM-12 | ネスト構造フォームでは `register` 引数をドット連結（例: `register('address.city')`） | 条件付き | MUST(条件) | form-validation | ネスト構造フォーム時 |
| FRM-13 | 画面内にフォームが複数ある場合は `useForm` を複数定義し関数名を区別する | 条件付き | MUST(条件) | form-validation | 複数フォーム時 |

---

## C5. 構造・規約・性能・ルーティング（`STR-`）

担当出典: `reacter-coding-standards` / `reacter-folder-structure` / `reacter-routing` / `reacter-performance`
※ コード全域の記法・型・命名・lint（ubiquitous）と構造・配置を**単独所有**する。

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| STR-01 | ファイル全体に対する ESLint 無効化をしない | 🚫 | MUST | coding-standards | 常時 |
| STR-02 | `if` 内の処理を同一行に書かない（改行する） | 🚫 | MUST | coding-standards | 常時 |
| STR-03 | `React.FC` を使わない | 🚫 | MUST | coding-standards | 常時 |
| STR-04 | プロミスチェーン `then()` を使わない（`async/await`） | 🚫 | MUST | coding-standards | 非同期処理時 |
| STR-05 | ESLint/Prettier を条件緩和方向に変更しない | ⚠ | SHOULD | coding-standards | 設定変更時 |
| STR-06 | `React.strictMode` を使わない | ⚠ | SHOULD | coding-standards | 常時 |
| STR-07 | props バケツリレーを 4 階層以上行わない（3 階層まで） | ⚠ | SHOULD | coding-standards | props 受け渡し時 |
| STR-08 | `any` 型を使わない（やむを得ない場合のみ可） | ⚠ | SHOULD | coding-standards | 常時 |
| STR-09 | 関数はアロー関数で記述（不可の場合のみ function） | ✅ | MUST | coding-standards | 常時 |
| STR-10 | 非同期待機は原則 `async/await` | ✅ | MUST | coding-standards | 非同期処理時 |
| STR-11 | すべての関数に TSDoc コメント（無名インラインコールバックは除く） | ✅ | MUST | coding-standards | 常時 |
| STR-12 | ガイドの命名規約を満たす | ✅ | MUST | coding-standards / folder-structure | 常時 |
| STR-13 | フォルダー構成がガイド（本プロジェクト実構成）を満たす | ✨ | SHOULD | coding-standards / folder-structure | 常時 |
| STR-14 | React コンポーネント名は PascalCase | ✅ | MUST | folder-structure | 常時 |
| STR-15 | コンポーネントファイル名は PascalCase でコンポーネント名と一致 | ✅ | MUST | folder-structure | 常時 |
| STR-16 | カスタムフック名は `useXxx` の camelCase | ✅ | MUST | folder-structure | フック定義時 |
| STR-17 | フックファイル名は `useXxx.ts` でフック名と一致 | ✅ | MUST | folder-structure | フック定義時 |
| STR-18 | Type 名は PascalCase | ✅ | MUST | folder-structure | 型定義時 |
| STR-19 | CSS クラス名は camelCase | ✅ | MUST | folder-structure | CSS 定義時 |
| STR-20 | CSS Modules ファイル名は `コンポーネント名.module.css` | ✅ | MUST | folder-structure | CSS Modules 時 |
| STR-21 | env 変数名は大文字 SNAKE_CASE | ✅ | MUST | folder-structure | 環境変数定義時 |
| STR-22 | 型定義は `types/types.ts`（`xxxTypes.ts` 可。本PJは `features/{機能}/types/`・`shared/types/`） | ✅ | MUST | folder-structure | 型定義時 |
| STR-23 | スキーマは `types/schema.ts`（`xxxSchema.ts` 可） | ✅ | MUST | folder-structure | スキーマ定義時 |
| STR-24 | import 順序 `builtin→external→internal(@/)→parent→sibling`（グループ間に空行） | ✅ | MUST | coding-standards | 常時 |
| STR-25 | Airbnb 命名（説明的命名・変数/関数 camelCase・クラス PascalCase・頭字語の大小統一） | ✨ | SHOULD | folder-structure | 常時 |
| STR-26 | `window.open` のウィンドウ名は他アプリと重複しない工夫（アプリ名等を含める） | ✅ | MUST | routing | window.open 使用時 |
| STR-27 | ルーティングは react-router（`BrowserRouter`>`Routes`>`Route`） | ✅ | MUST | routing | ルーティング時 |
| STR-28 | ネスト構造のルーティングは親に `<Outlet />` を記載 | ✅ | MUST | routing | ネストルーティング時 |
| STR-29 | 戻る対策は `replace`（`<Navigate replace />`/`navigate(path,{replace:true})`） | ✨ | SHOULD | routing | 戻る抑止時 |
| STR-30 | サブパス配置時は `<BrowserRouter basename={ROUTER.BASE_NAME}>` を設定 | 条件付き | MUST(条件) | routing | サブパス配置時 |
| STR-31 | 効果が見込めないメモ化はしない（効果のあるケースのみ） | ✨ | SHOULD | performance | メモ化時 |
| STR-32 | コンポーネント外で定義可能な関数は外で定義（不要な useCallback を避ける） | ✨ | SHOULD | performance | 関数定義時 |
| STR-33 | `memo` した子へ props で渡すコールバックは `useCallback` でメモ化 | 条件付き | MUST(条件) | performance | コールバックを props で渡す時 |
| STR-34 | コード分割（`lazy`）対象モジュールは default export | 条件付き | MUST(条件) | performance | コード分割時 |
| STR-35 | `id` 属性は全画面で重複しない命名にする（画面 ID 等をプレフィックスに付与） | ✅ | MUST | folder-structure | id 属性使用時 |
| STR-36 | Vitest テストファイルは `src/test/` 配下にソースと同じディレクトリ構造をミラーリングして配置（`src/test/` 直下は共通ファイルのみ・直置き不可） | ✅ | MUST | folder-structure | テストファイル作成時 |

---

## C6. 状態・エラー・テスト・共通部品（`QLT-`）

担当出典: `reacter-state-management` / `reacter-error-handling` / `reacter-testing-vitest` / `reacter-testing-playwright` / `reacter-libraries`

| ID | 観点 | 制約レベル | 分類 | 出典 | 適用条件 |
|---|---|---|---|---|---|
| QLT-01 | `addEventListener` は不要時に `removeEventListener` で必ず削除 | ✅ | MUST | state-management | イベントリスナ登録時 |
| QLT-02 | `useContext` の `Provider` で囲む範囲を必要最小限に限定 | ✨ | SHOULD | state-management | useContext 使用時 |
| QLT-03 | オブジェクト型・配列型の定数末尾に `as const` | ✨ | SHOULD | state-management | 定数定義時 |
| QLT-04 | 状態管理は `useState` 基本、階層が深い場合に `useContext` | ✨ | SHOULD | state-management | 状態管理時 |
| QLT-05 | `createContext` の初期値に `null` を指定 | ✨ | SHOULD | state-management | createContext 時 |
| QLT-06 | `error.message` をエラー画面にそのまま表示しない | ⚠ | SHOULD | error-handling | エラー表示時 |
| QLT-07 | Error Boundary で画面表示エラー（レンダー/ライフサイクル）を捕捉 | ✅ | MUST | error-handling | 常時 |
| QLT-08 | エラー画面コンポーネントをプロジェクト要件に合わせカスタマイズ | ✅ | MUST | error-handling | 常時 |
| QLT-09 | 関数コンポーネントの Error Boundary は `react-error-boundary` を使用 | ✅ | MUST | error-handling | Error Boundary 利用時 |
| QLT-10 | 捕捉対象を `<ErrorBoundary FallbackComponent={...}>` で囲む（全体は最外側） | ✅ | MUST | error-handling | Error Boundary 利用時 |
| QLT-11 | イベント/非同期エラーは `try-catch` で個別ハンドリング | ✅ | MUST | error-handling | イベント/非同期処理時 |
| QLT-12 | `ErrorPage.tsx`（テンプレートのエラー画面）を必ずカスタマイズ | ✅ | MUST | error-handling | テンプレート利用時 |
| QLT-13 | エラー画面はシンプルでエラーが発生しえない作りにする | ✨ | SHOULD | error-handling | エラー画面実装時 |
| QLT-14 | CSS Modules 使用時、テストのセレクタのクラス名を正規表現で記述（ハッシュ対応） | ✅ | MUST | testing-vitest | CSS Modules 使用時 |
| QLT-15 | 複雑/共通カスタムフックは `renderHook` で個別テスト | ✨ | SHOULD | testing-vitest | カスタムフック開発時 |
| QLT-16 | `setTimeout` での待機合わせは最終手段（DOM 状態待ち `waitFor` を優先） | ⚠ | SHOULD | testing-playwright | E2E ルーティング直後 |
| QLT-17 | セレクタの CSS Modules クラス名はワイルドカード（`[class^="..."]`）で可変部を省略 | ✅ | MUST | testing-playwright | CSS Modules セレクタ時 |
| QLT-18 | Playwright テストファイル名は `任意文字列.spec.ts` | ✅ | MUST | testing-playwright | E2E テスト時 |
| QLT-19 | セレクタ定数ファイル名の先頭文字は小文字 | ✅ | MUST | testing-playwright | セレクタ定数作成時 |
| QLT-20 | セレクタは `locator`+定数化（画面ごとの定数ファイル・パスカル命名・`as const`） | ✨ | SHOULD | testing-playwright | セレクタ定義時 |
| QLT-21 | PapaParse の `delimiter`・`newline` を必ず明示設定 | ✅ | MUST | libraries | CSV 解析時 |
| QLT-22 | ローカルファイル読込時にファイル種類・サイズをチェック | ✅ | MUST | libraries | ローカルファイル読込時 |
| QLT-23 | メッセージをハードコーディングせずメッセージ定義ファイルで管理 | ✨ | SHOULD | libraries | メッセージ使用時 |
| QLT-24 | 帳票プレビュー子画面（`window.open`）のウィンドウ名を業務 ID 含むユニーク名に | 条件付き | MUST(条件) | libraries | 帳票プレビュー時 |
| QLT-25 | jsPDF で日本語出力時は Base64 日本語フォントを読み込む | 条件付き | MUST(条件) | libraries | jsPDF 日本語出力時 |
| QLT-26 | `FileReader.readAsText` で SJIS 等は第 2 引数に文字コードを指定 | 条件付き | MUST(条件) | libraries | SJIS 等読込時 |
| QLT-27 | `react-tabs` `forceRenderTabPanel` 使用時は F キー等の有効/無効制御を利用者が行う | 条件付き | MUST(条件) | libraries | forceRenderTabPanel 使用時 |
| QLT-28 | sessionStorage 操作時に `sessionStorage.clear()` で全体クリアをしない（`removeItem` を使う） | ⚠ | SHOULD | state-management | sessionStorage 使用時 |
| QLT-29 | React18 使用時は Provider を `<コンテキスト.Provider value={...}>` 形式で記述 | 条件付き | MUST(条件) | state-management | React18 使用時 |
| QLT-30 | テスト間に依存がある場合は `fullyParallel: false` / `workers: 1` を設定 | 条件付き | MUST(条件) | testing-playwright | テスト間依存がある場合 |
| QLT-31 | 拡張機能使用時は `chromium.launchPersistentContext(USER_DATA_DIR)` で指定 | 条件付き | MUST(条件) | testing-playwright | 拡張機能使用時 |
| QLT-32 | 拡張機能有効時はポップアップ表示後に `page.reload()` を挿入 | 条件付き | MUST(条件) | testing-playwright | 拡張機能有効時 |
| QLT-33 | `Locator.clear()` でクリアできない場合は `Control+a` → `Delete` の処理を使う | 条件付き | MUST(条件) | testing-playwright | テキスト入力クリア時 |

---

## 使い方（各クラスタエージェント共通）

1. 本台帳の**自クラスタ行のみ**を担当範囲とする（CMN は無い。共通コーディング規約は C5 が単独所有）。
2. 対象ファイル（`frontend/`）を読み、性質（通信/フォーム/AG-Grid/CSV/帳票/セッション/一時ファイル 等の使用有無）を判定し、各観点の「適用条件」に照らして該当/非該当を決める。
3. 詳細な規約文言・実装例が必要な観点、または違反が疑われる観点のみ「出典」のルールファイル（`.claude/rules/reacter-{出典}.md`）を開いて確認する（全ルール常時全読込は不要）。
3-2. ルールファイルの各箇条書き行頭には `[ID]` が付与されている。**自クラスタのプレフィックス（C1=COM-/C2=SEC-/C3=CSS-/C4=FRM-/C5=STR-/C6=QLT-）と一致しない ID が付いた行は他クラスタ所有**のためスキップする（判定しない）。`[参考]` 付きの行は規範性なし（判定対象外）のためスキップする。
4. 自クラスタの**規範行（C-行）すべて**に `準拠 / 違反 / 非該当` を付与し、**未評価 0 件**を保証する。違反は `ファイル(行番号)` を全件列挙し、各指摘に `制約レベル` と `分類`（MUST/SHOULD/MAY）を付与する。「等」「複数箇所」「XX以上」等の曖昧表現は禁止。
5. （任意）規範行を全件判定したうえで、付録の自クラスタ参考観点（`REF-`）に**明確な逸脱**があれば **MAY** として任意報告してよい（報告は義務ではなく、未評価0件ゲートの対象外。逸脱が無ければ言及不要）。

---

## 付録: 参考観点リスト（判定対象外・任意 MAY）

> 規範性のない「参考・推奨実装パターン」由来の観点。**未評価0件ゲートの対象外**。クラスタエージェントは規範行（C-行）を全件判定したうえで、以下に明確な逸脱があれば **MAY** として任意報告してよい（報告義務はなく、抜けても違反扱いにしない）。一覧は代表例であり**網羅性は保証しない**。

### C1 通信（`REF-COM`）
- REF-COM-01 GET/POST で `catch` を書き `axios.isAxiosError` で response/request/その他に分岐しているか
- REF-COM-02 ファイルダウンロードで `URL.revokeObjectURL` による後始末をしているか
- REF-COM-03 用途別に分けるべき通信先で Axios インスタンスを使い分けているか

### C2 セキュリティ・ビルド（`REF-SEC`）
- REF-SEC-01 CSP に `frame-ancestors 'none'`（クリックジャッキング対策）を設定しているか
- REF-SEC-02 独自モード/環境変数ファイルがモード対応表どおり命名・分離されているか

### C3 スタイル（`REF-CSS`）
- REF-CSS-01 グローバル CSS で初期化し画面個別は class セレクタ上書き、の基本パターンに沿っているか
- REF-CSS-02 子/子孫セレクタの多用で崩れやすくなっていないか

### C4 フォーム（`REF-FRM`）
- REF-FRM-01 `z.infer` でスキーマから型を生成しているか
- REF-FRM-02 複数フォーム時に `register` 取り出し名を意味ある名前で区別しているか

### C5 構造・規約・性能・ルーティング（`REF-STR`）
- REF-STR-01 TSDoc が `@param 名 - 説明` / `@returns` 形式（型をコメントに書かない）か
- REF-STR-02 パスパラメータ/クエリ/state 受け渡しが推奨 API（`useParams`/`useSearchParams`/`useLocation`）か
- REF-STR-03 不要な `useMemo`/`memo`/`useCallback`（効果のないメモ化）がないか

### C6 状態・エラー・テスト・共通部品（`REF-QLT`）
- REF-QLT-01 Context を専用ファイルに「型+createContext+Provider」でまとめているか
- REF-QLT-02 テストが given/when/then 構造で、API 通信フックは MSW でモックしているか
- REF-QLT-03 メッセージ定義/初期化（`messageInitializer` の `getMessage`）パターンに沿っているか
- REF-QLT-04 メッセージ ID が命名規約（`{カテゴリ記号}{4桁ゼロ埋め連番}`。例: `E0001`）に沿っているか
