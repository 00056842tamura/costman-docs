---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# ユニットテスト（Vitest + Testing Library）（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

（該当なし）

## ⚠ 非推奨 (AVOID)

（該当なし）

## ✅ 必須 (ALWAYS)

- [QLT-14] CSS モジュールを使用している場合、テストコードのセレクターの CSS クラス名の箇所を正規表現で記述することで、クラス名のハッシュ化に対応すること。
  （根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §3）

## ✨ 推奨 (PREFER)

- [QLT-15] 複雑なロジックを持つカスタムフックや、共通部品として複数箇所で利用されるカスタムフックを開発した場合は、`renderHook` を利用してカスタムフックを個別にテストするとよい。
  （根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/04.ReactHooksのテストの書き方.md`）

## 条件付き事項（一覧）

- [参考] テストファイルは `*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}` に一致する名前が Vitest によりテストとみなされる。一般的には `[テスト対象ファイル名].test.tsx(ts)` という命名規則を用いる。配置先（`src/test/` へのミラーリング）は `reacter-folder-structure.md` [STR-36] を参照。
  - **条件成立時: 任意（一般に .test.tsx を推奨）**
  （根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/01.基本的な使い方.md`）
- [参考] `beforeAll`/`afterAll` はテストケース実行前後に 1 回だけ実行したい処理に、`beforeEach`/`afterEach` は各テストケース実行前後に毎回実行したい処理に利用できる。
  - **条件成立時: 任意（用途別の選択）**
  （根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/04.ReactHooksのテストの書き方.md`）

## 参考・推奨実装パターン

### [参考] setupFiles の設定

- 共通セットアップ（`@testing-library/jest-dom/vitest` の import 等）は `src/test/setup.ts` に置き、`vite.config.ts` の `test.setupFiles` に `'./src/test/setup.ts'` を指定して読み込む。
- `src/test/` 配下のその他のパスは個別テストファイルのミラーリング配置用（`reacter-folder-structure.md` [STR-36]）であり、`setup.ts` はその配下に置く唯一の非ミラーファイルとなる。

### [参考] テストコードの基本構成（describe / it）

- Vitest の `describe` でテストグループを定義し、`it` でテストケースを定義する。
- Testing Library の `render` でコンポーネントを描画し、`screen.getByText`（テキストから検索）・`screen.getByRole`（role 属性から検索）等のクエリで対象要素を検索する。
- `fireEvent.click()` 等でイベントを発火させ、`expect(...).toBeInTheDocument()` 等のマッチャーでアサーションを行う。
- テストは given / when / then の構造で記述すると読みやすい。

（根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/01.基本的な使い方.md`）

### [参考] テスト実行

- `npm run test` でテストを実行する。デフォルトで watch モードで実行され、コード変更時に自動再実行される。`q` 入力で watch を終了する。
- 成功時は `passed`、失敗時は `Failed` のケースとエラーログが出力される。

（根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/01.基本的な使い方.md`）

### [参考] テストカバレッジ（C0 / C1）

- `npm run test:coverage` でカバレッジ取得モードでテストを実行し、表形式で出力される。
- カバレッジ表の見方:
    - `Stmts`: すべての実行可能命令のうちテストで実行された命令の割合（**C0**: 命令網羅率）
    - `Branch`: すべての条件分岐のうちテストで実行された分岐の割合（**C1**: 分岐網羅率）
    - `Funcs`: すべての関数のうちテストで実行された関数の割合
    - `Lines`: すべてのコード行のうちテストで実行された行の割合
    - `Uncovered Line`: 実行されていない行番号

（根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/02.テストカバレッジの取得.md`）

### [参考] React コンポーネントのテスト

- `render` でレンダリングしたコンポーネントに対し、主に以下の観点でテストを作成する。
    - 表示要素（タイトル・ボタン名など）の検証
    - Props・State の変化に応じたレンダリング結果の検証
    - ボタンクリックなどユーザ操作に応じた動作の検証

（根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/03.Reactコンポーネントのテストの書き方.md`）

### [REF-QLT-02] given/when/then構造・MSWモック

- Testing Library の `renderHook` で対象のカスタムフックを呼び出し、戻り値 `result` を取得する。`result.current` に現在の戻り値が格納されており、`expect` で検証する。
- カスタムフックが返す関数（例: `mutate`）は `result.current.mutate()` のように実行する。
- 非同期処理を待ってチェックする場合は `waitFor` を利用し、タイムアウトまで期待値が来るのを待機する。
- カスタムフックのテスト観点:
    - 引数で渡した値が利用されていることの検証
    - 取得する値の検証
    - 取得する関数の実行結果の検証

#### API 通信のモック化（MSW）

- API 通信を伴うフックは [Mock Service Worker](https://mswjs.io/)（MSW）で API リクエストをモック化する。
- `TEST_HANDLERS` 配列に必要な API 個数分のモック定義を設定する。`http.get()`/`http.post()` の第 1 引数にエンドポイント URL、第 2 引数にモック処理を記載し、`HttpResponse.json(responseData)` を return してモックレスポンスを返す。
- リクエストボディは `request.json<型>()`（非同期、`async`/`await` 必要）で取得する。
- `setupServer(...TEST_HANDLERS)` でモックサーバーを設定し、`beforeAll` で `server.listen()`、`afterAll` で `server.close()` を呼ぶ。

（根拠: `reacter-docs-main/07.テスト/02.Vitest＋TestingLibraryを利用した自動テスト/04.ReactHooksのテストの書き方.md`）
