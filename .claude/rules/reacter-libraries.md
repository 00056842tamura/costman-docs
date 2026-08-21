---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# ライブラリ部品（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

本ファイルは Reacter（blank-template 一般）で利用する各種ライブラリ部品の規約をまとめる。部品ごとに小見出しを設け、その中で 4 分類・条件付き事項・参考パターンを整理する。
帳票作成・CSV 解析・メッセージ管理・ツールチップ・ローカルファイル読み込みなど、大半は手順／API 解説（タイプ B）であり「参考・推奨実装パターン」に記載する。規範（禁止／必須等）は instructions.md 由来または明確な記述に限定する。

---

## タブパネル（react-tabs）

### ✅ 必須 (ALWAYS)

（該当なし）

### ✨ 推奨 (PREFER)

（該当なし）

### 条件付き事項（一覧）

- [参考] `react-tabs` は非制御モード／制御モードの 2 つを提供する（デフォルトは非制御モード）。タブクリック以外の操作をトリガにタブを切り替えるケースを考慮し、本ガイドでは制御モードでの使用を前提とする。
  - **条件成立時: 任意**
- [QLT-27] 別タブ選択時にフォーム入力済み内容を消去させたくない場合は、`forceRenderTabPanel` オプションを使用して非表示タブも裏で強制レンダリングする。ただし非表示タブの内容も DOM 上に残るため、タブ表示状態に合わせたファンクションキー等の有効/無効制御はライブラリ利用者が行う必要がある。
  - **条件成立時: ✅必須**
- [参考] `Tab` タグ内の `tabIndex` を数値型で記述すると開発環境上でエラー表示となることがあるが、動作は正常。エラー表示が気になる場合は `string` 型で記述してよい。
  - **条件成立時: 任意**

### 参考・推奨実装パターン

- 制御モードでは、選択中タブの index を `useState` で保持し、`Tabs` の `selectedIndex` に渡し、`onSelect` で更新する。
- タブの表示/非表示状態に応じてファンクションキー（`useFunctionKey`）の有効/無効を切り替える。
- `tabIndex` はフォーカス遷移させたい方向に合わせて設定する（非表示要素はフォーカスされない）。

---

## 文字列／数値フォーマット（react-string-format・numeral）

> 入力フォームの値ではなく、ラベル等の表示テキストをフォーマットする用途。

### ✅ 必須 (ALWAYS)

（該当なし）

### 参考・推奨実装パターン

- 文字列フォーマット（区切り文字挿入など）: `react-string-format` の `format('{0}-{1}-{2}', a, b, c)` を使用。第 1 引数に `{}` でスタイルを定義し、第 2 引数以降の値が `{}` に順番に代入される。
- 数値フォーマット: `numeral` を使用。
  - 3 桁ごとのカンマ: `numeral(1000000).format('0,0')` → `1,000,000`
  - 先頭 0 埋め: `numeral(100).format('000000')` → `000100`
  - 小数桁指定: `numeral(1.235).format('0.00')` → `1.24`（小数第三位は四捨五入）

---

## CSV 解析（PapaParse）

### ✅ 必須 (ALWAYS)

- [QLT-21] ✅ **必須 (ALWAYS)**: PapaParse の `config` で、区切り文字（`delimiter`）および改行コード（`newline`）を必ず設定すること。自動判定も可能だが、意図せぬ動作になる可能性があるため明示する。

### 条件付き事項（一覧）

- [参考] `config.header` に `false` を指定すると `data` は 2 次元配列、`true` を指定すると先頭行をヘッダーとしたオブジェクト配列で返却される。
  - **条件成立時: 任意**

### 参考・推奨実装パターン

- `import { parse } from 'papaparse'` し、`parse<T>(csvString, config)` で解析する。戻り値は `{ data, errors, meta }`（`ParseResult`）。
- CSV へ戻す場合は `unparse` を使用（`newline` 指定可）。

---

## 帳票作成（jsPDF・日本語フォント）

### 🚫 禁止 (NEVER)

（該当なし）

### ✅ 必須 (ALWAYS)

（該当なし。下記「条件付き事項」参照）

### 条件付き事項（一覧）

- [QLT-24] 帳票プレビューを子画面（`window.open`）で表示する際、ウィンドウ名は他のアプリケーションと重複しないよう、業務 ID を含むユニークな名前（例: `_mkk00_preview`）にすること。同一ウィンドウ名は表示済みウィンドウを上書きし、`_blank` は出力の度に新規ウィンドウが開きリソースを消費する。
  - **条件成立時: ✅必須**
  - （instructions.md §2.8「ウィンドウ表示」の規約と整合）
- [QLT-25] `jsPDF` は初期設定のままでは日本語フォントを指定できない。日本語を出力する場合は Base64 化した日本語フォント（例: M+ 2p regular、BIZ UDGothic）を `addFileToVFS` → `addFont` → `setFont` で読み込んで使用する。
  - **条件成立時: ✅必須**
- [参考] 子画面表示は Microsoft Edge のポップアップブロック対象となり得る。表示されない場合は該当 URL をポップアップブロック許可に追加する（運用・環境設定上の注意）。
  - **条件成立時: 任意**

### 参考・推奨実装パターン

- インスタンス生成: `new JsPdf('l', 'pt', 'a4', true)`（向き・単位・サイズ・圧縮）。
- 要素配置 API: `text()` / `line()` / `rect()` / `setFillColor()` / `setLineWidth()` / `setFontSize()` / `addImage()`（Base64 画像）など。
- 複数ページ: `addPage()` / `getNumberOfPages()` / `setPage()` でページ追加・全ページ処理（ヘッダー／フッター）を行う。
- テーブル: `jspdf-autotable` の `autoTable(doc, { head, body, foot, styles, columnStyles, ... })` を使用。`showHead: 'everyPage'`・`rowPageBreak: 'avoid'`・`didDrawCell` フック等が利用可。データ複数ページ時は改ページが自動。
- 出力・プレビュー: `output('bloburi')` で URL を取得し `window.open` で子画面表示。子画面を閉じた／親画面 `beforeunload` 時に `URL.revokeObjectURL` でリソースを解放し、登録した `addEventListener` は `removeEventListener` で削除する。
- バーコード（`jsbarcode`）・グラフ（`chart.js` + `react-chartjs-2`）は `canvas` で生成し画像として `addImage` で挿入できる。グラフは `animation: false` で画像取得中のアニメーションを防止する。
- フォント Base64 化: `certutil -f -encodehex <ttf> <b64> 0x40000001`。

---

## メッセージ管理（getMessage・メッセージ定義ファイル）

### ✅ 必須 (ALWAYS)

（該当なし）

### ✨ 推奨 (PREFER)

- [QLT-23] ✨ **推奨 (PREFER)**: メッセージをハードコーディングせず、メッセージ定義ファイルで管理すること。

### 条件付き事項（一覧）

- [参考] メッセージ定義は `/src/config/message.ts` にまとめて管理する。複数ファイルに分けることも可能（その場合はファイル名と変数名を異なる名前にし、`messageUtil.add` で 2 つ目以降を追加する）。
  - **条件成立時: 任意**

### 参考・推奨実装パターン

### [REF-QLT-03] メッセージ定義/初期化パターン

- 定義: `{ id, message }` の配列を `MessageType[]` として定義。`message` には `{0}`・`{1}` 等のプレースホルダーを埋め込める。
- 初期化: `/src/lib/messageInitializer.ts` で `new MessageUtil()` → `init({ resources: messages })` → `getMessage` を export（複数定義は `add` で追加）。
- 利用: `getMessage('E0001', 値1, 値2, ...)`。第 1 引数（必須）にメッセージ ID、第 2 引数以降（任意）にプレースホルダー置換値を index 順で指定。

### [REF-QLT-04] メッセージ ID 命名規約

> 工程4 SS `{スタック}/docs/detail-design/メッセージ一覧.md`（`docs/templates/50_詳細設計/メッセージ一覧.md`）で確定し、`/src/config/message.ts` へ反映する ID の命名規約。Springer 側の階層命名（`{アプリ名}.error.business.{名前}` 等、[springer-message-i18n.md](springer-message-i18n.md) R-12-01）は複数アプリが名前空間を共有する前提のため、単一 SPA で完結する Reacter では軽量な形式を採用する。

- 形式: `{カテゴリ記号}{4桁ゼロ埋め連番}`（例: `E0001`）。
- カテゴリ記号: `E` = エラー（入力チェック・業務エラー・システムエラーに相当する表示メッセージ）、`I` = 案内（確認・完了・出力成功等の情報メッセージ）。
- 連番はカテゴリ記号ごとに独立してユニークとし、機能追加で累積する（既存番号は変更しない）。

---

## ツールチップ（title 属性）

### 参考・推奨実装パターン

- HTML5 の `title` 属性でツールチップを実現する。要素（例: `<button title="...">`）にカーソルを重ねると指定文字列が表示される。

---

## ローカルファイル読み込み（FileReader・種類/サイズチェック・SJIS）

### ✅ 必須 (ALWAYS)

- [QLT-22] ✅ **必須 (ALWAYS)**: ローカルファイルの読み込みを行う場合、ファイルの種類やサイズをチェックすること。種類は MIME タイプやファイル拡張子でチェックし、サイズはプロジェクトの要件に合わせて上限を設定する。

### 条件付き事項（一覧）

- [QLT-26] `FileReader.readAsText` のデフォルト文字コードは UTF-8。SJIS 等の場合は第 2 引数で文字コードを指定して読み込む（指定しない場合は省略可で UTF-8）。
  - **条件成立時: ✅必須**

### 参考・推奨実装パターン

- 処理の流れ: 選択ボタン押下 → ファイル選択ダイアログ → ファイル読み込み＋内容バリデーション → axios でマルチパート送信。
- 実装: `<input type="file">` を CSS で非表示にし、表示用ボタンの `onClick` から `inputRef.current?.click()` で起動。`onChange` で `FileList` を受け取り、`new FileReader()` → `readAsText(file, 'SJIS')` → `onload` で `event.target?.result` を取得・検証する。
- 検証例: 拡張子チェック（`.csv`）、改行コード（`\r\n`）の有無、行数上限、各行の要素数・桁数・文字種など。
- 送信は `FormData` に内容を `append` して `axiosDefault.post` でアップロード（詳細は `reacter-server-communication.md` 参照）。

---
