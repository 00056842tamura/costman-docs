---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# E2E テスト（Playwright）（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

（該当なし）

## ⚠ 非推奨 (AVOID)

- [QLT-16] `setTimeout()`（およびそれに類する待機）でのタイミング合わせは最終手段とすること。その時間待機しても確実に期待した状態になっている保証がないため、ルーティング直後のテストでは避けるか、DOM 状態待ち（`waitFor`）を優先する。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）

## ✅ 必須 (ALWAYS)

- [QLT-17] セレクター文字列に CSS モジュールで定義したクラス名が含まれる場合、ワイルドカード指定（`[class^="..."]` 等）で可変部分（`_xxxxx_xx`）を省略する加工を行うこと。加工しないと CSS モジュール変更時にテストが失敗する。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）
- [QLT-18] Playwright のテストファイル名は `任意文字列 + .spec.ts` の形式にすること（Playwright の仕様）。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- [QLT-19] セレクター定数ファイルのファイル名の先頭文字は小文字にすること（大文字始まりのファイル名はコンポーネントを表す命名規則のため）。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）

## ✨ 推奨 (PREFER)

- [QLT-20] 項目の指定にはセレクター（`page.locator(セレクター)`）を使用するとよい。常に同じ `locator()` で指定でき、引数のセレクター文字列を定数化して共通利用できるため、画面修正時も定数の修正でリカバリーできる。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）
- セレクター定数ファイルは画面ごとに用意するとよい。ファイル名にも「画面ID」を含めると分かりやすい（形式: `画面ID.selector.ts`、例: `login.selector.ts`）。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）
- セレクター定数はオブジェクト形式で作成し、IDE のコードアシストを活かせる適切なサイズにするとよい（1 オブジェクトにプロパティが大量だと候補が選びにくい）。各オブジェクトには末尾に `as const` を付ける。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）
- セレクター定数のオブジェクト名・プロパティ名は全て大文字のパスカル記法とし、オブジェクト名は「セレクターであること」「どの画面か」「要素分類」（必要なら処理モード）が分かる名前にするとよい（例: `SELECTOR_LOGIN_TEXT`・`SELECTOR_FRMXXX0101_FORM_ADD`）。プロパティ名はどの項目／テキストのセレクターか分かる名前にする（例: `INPUT_ADDRESS`・`BUTTON_SEARCH`・`GRID_EMPROYEE_NAME_3`）。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）
- テストファイルを順番に実行する場合は、ファイル名が辞書的に昇順に並ぶようにするとよい（例: `01.Login.spec.ts`・`02.Menu.spec.ts`）。テストシナリオ単位ならシナリオ番号、画面単位なら画面 ID を含めるとよい。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- `test.step()` でテストケース内の処理を意味のある単位にまとめるとよい（コードとレポートの視認性向上）。ネスト可能。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- コードジェネレーターで生成したコードに余計な操作が混じっている場合は、適宜手動でメンテナンスを加えるとよい。
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/01.基本編/README.md`）

## 条件付き事項（一覧）

- [参考] `test()` の単位は、途中でテスト失敗したら同一 `test()` 内の以降の操作・検査を続けても意味がない単位（＝他の `test()` が失敗しても実施可能なテストケース単位）にする。一連の流れで一つでも失敗したら継続できないシナリオテストでは、ファイル内に `test()` は 1 度だけになる。
  - **条件成立時: 任意（設計指針）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- [参考] テストファイルの単位はテストシナリオ単位（一般的な E2E）または画面単位（単体テストの自動化）が考えられる。
  - **条件成立時: 任意（方針の選択）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- [QLT-30] `playwright.config.ts` で、複数 `test()` の同時実行は `fullyParallel`（`true`=同時／`false`=上から 1 つずつ）で設定する。あるテスト結果が次のテストの前提条件になる場合は `false` とし、`workers: 1` を指定する。
  - **条件成立時: ✅必須（テスト間に依存がある場合は false / workers:1）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/01.環境構築.md`）
- [参考] `reporter` は HTML 形式等を選べる。`open: 'on-failure'` は失敗時のみ、`open: 'on-always'` は成否に関わらずレポートを表示。
  - **条件成立時: 任意（任意設定）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/01.環境構築.md`）
- [参考] `webServer.reuseExistingServer` は、`true` で指定 URL が生きていれば起動中アプリをそのまま使用、`false` なら生きているとテストをしない。
  - **条件成立時: 任意（設定値の説明）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/01.環境構築.md`）
- [QLT-31] 拡張機能を使用する場合は `chromium.launchPersistentContext(USER_DATA_DIR)` でユーザープロファイルを指定し、使用しない場合は `chromium.launchPersistentContext("")` でプロファイル指定なしで起動できる。
  - **条件成立時: ✅必須（拡張機能を使用する場合は指定）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/01.基本編/README.md`）
- [QLT-32] 拡張機能有効時、ポップアップウィンドウを開いた際はリロードを挟まないと拡張機能の読み込みができないため、ポップアップ表示後に `page.reload()` を挿入する。
  - **条件成立時: ✅必須（拡張機能有効時）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/01.基本編/README.md`）
- [参考] `test.beforeAll`/`afterAll`/`beforeEach`/`afterEach` は処理が必要でない場合は定義不要。`test.step()` も不要なら定義不要。
  - **条件成立時: 任意**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`）
- [QLT-33] `Locator.clear()` で `<InputEx>` にフォーマット指定をした場合などはクリアできないことがあり、その場合は `Control+a` → `Delete` のクリア処理を関数化して使用する。
  - **条件成立時: ✅必須（clearできない場合の対処）**
  （根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/05.APIリファレンス.md`）

## 参考・推奨実装パターン

### [参考] フォルダー構成

- `e2e` 配下に `common`（共通関数）・`consts`（定数）・`files/input/{expect,upload}`・`files/output/{download,screenshot}` を配置する。テストによる出力ファイル（`/e2e/files/output/` 等）は `.gitignore` で git 管理対象外にする。

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/01.環境構築.md`）

### [参考] テストファイルの構成

- `test.beforeAll`/`afterAll`（全体前後処理 1 回）、`test.beforeEach`/`afterEach`（各テスト前後処理）、`test()`（テストケース、複数可）、`test.step()`（ステップ分割、ネスト可）で構成する。
- コードジェネレーター（`npm run e2e <ファイル名>`）で操作を記録してテストコードを自動生成できる。`await browser.close()` を `await page.pause()` に置き換えて一時停止させ、Record でブラウザ操作を記録し、生成形式を「Test Runner」にしてコピーする。最後にアサーションを手動追加する。

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/01.基本編/README.md`、`.../02.実践編/04.テストファイルの作成.md`）

### [参考] セレクター文字列の CSS モジュール加工

- CSS モジュールのクラス名はビルド後に `_クラス名_xxxxx_xx` に変化し、CSS 変更で末尾が変わる。書式:
    - `[class^="文字列"]`: 前方一致　/　`[class$="文字列"]`: 後方一致　/　`[class*="文字列"]`: 中間一致
    - クラス名先頭の `.` は削除する。複数クラス指定はクラス名の間にスペース 1 文字（`[class^="クラス名１ クラス名２"]`）。
- 例: `._selectBoxArea_1p4zc_33` → `[class^="_selectBoxArea_"]`

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/03.セレクター定数ファイルの作成.md`）

### [参考] 画面の操作・検査 API（要点）

- 操作: `click()`（左/`{button:'right'}`右/`dblclick()`）、`fill()`（1 回 onChange）、`pressSequentially()`（1 文字ずつ onKeyDown/onChange）、`selectOption(値)`、`check()`/`uncheck()`、`press('キー')`、`focus()`、`clear()`。
- 検査: `expect(Locator).期待値` 形式。`toHaveValue()`・`toHaveText()`・`toBeEmpty()`・`toBeChecked()`/`not.toBeChecked()`・`toBeEnabled()`/`toBeDisabled()`・`toBeFocused()`・`toHaveClass()`・`toBeVisible()`/`toBeHidden()`・`toHaveAttribute()`・`toEqual()`・`toHaveURL()`。
- 文字入力では IME の切り替えや変換入力（「k」「a」「Enter」で「か」）はできない。

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/05.APIリファレンス.md`、`.../01.基本編/README.md`）

### [参考] SPA / ルーティング直後の待機

- SPA は DOM が動的に書き換わるため、ページロード完了待ちの仕組みが使えない。画面遷移（ルーティング）が終わっても初期処理（`useEffect()`）やキーバインディングは終わっていないため、それを前提としたテストは失敗する。
- ルーティング直後の操作・検査は DOM が期待状態になるのを待つ。`Locator.waitFor({ state: 'attached' })`（DOM 上にできるのを待つ）・`waitFor({ state: 'dettached' })`（DOM 上から消えるのを待つ）を使用する（`Page.waitForTimeout()` は Deprecated）。

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`、`.../05.APIリファレンス.md`）

### [参考] window.open で開くアプリ（ポップアップ）の扱い

- Biz 再構築では画面をポップアップ表示して使用する。テロップ画面を表示 → リンク/ボタンクリックでポップアップ Window にログイン画面を表示 → ポップアップの `Page` オブジェクトをグローバル変数（`targetPage`）に保持 → 操作開始、という手順。
- 共通関数では `page.waitForEvent('popup')` でポップアップを待ち受け、リンクをクリックして表示された `Page` を受け取り、`targetPage.reload()`（Reacter-Link 読み込みのため）してから返却する。`beforeEach` で開き `afterEach` で `close()` する。
- **メッセージボックス**（`alert`/`confirm`/`prompt` 相当）は Reacter では `reacter-message-box` を使うため特殊操作不要。**ダイアログ**も `reacter-modal` を使うため特殊操作不要。
- **帳票プレビュー**は別 Window を `window.open` → `window.close` → `window.open` の手順で表示するため、ポップアップイベントが 2 回発生する。2 回目のイベント（`targetPage.on('popup', ...)` でカウント）で `resolve()` を呼んで完了を待つ。PDF ビューアは Playwright で操作できないため、確認できるのはプレビュー画面が表示されたかどうかまで。
- **ファイルダウンロード**: `waitForEvent('download')` → 操作 → `download.saveAs(...)`。検査は期待値ファイルと MD5 ハッシュ比較。
- **ファイル選択**: `waitForEvent('filechooser')` 経由、または `setInputFiles(ファイルパス)`。
- **`<img>` 画像保存**: `waitForResponse()` でレスポンスを待ち受け `fs.writeFileSync` で保存。
- **スクリーンショット**: `Page.screenshot({ path })`。

（根拠: `reacter-docs-main/07.テスト/03.Playwrightを利用したE2Eテスト/02.実践編/04.テストファイルの作成.md`、`.../05.APIリファレンス.md`）
