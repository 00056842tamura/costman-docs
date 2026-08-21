---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# サーバ通信（Axios）（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

Reacter での HTTP 通信は `axios` を使用する。本ファイルは Axios 共通インスタンスの利用・タイムアウト・GET/POST・ファイルダウンロード/アップロード・一覧件数上限・静的ファイルのキャッシュ制御（キャッシュバスター）・エラーハンドリングに関する規約をまとめる。

## 🚫 禁止 (NEVER)

- [COM-01] 🚫 **禁止 (NEVER)**: サーバ通信時にサーバエラーが発生した場合、サーバからのエラーメッセージをそのまま画面に表示しない。
- [COM-02] 🚫 **禁止 (NEVER)**: 操作ユーザに見せるべきでないデータを取得し、画面側で非表示にすること（参照可能なデータのみ取得すること）。

## ⚠ 非推奨 (AVOID)

（該当なし）

## ✅ 必須 (ALWAYS)

- [COM-03] ✅ **必須 (ALWAYS)**: Axios のタイムアウト時間をプロジェクトの要件に合わせて見直すこと。設定ファイルは `src/lib/axiosConfig.ts`。
- [COM-08] ✅ **必須 (ALWAYS)**: 時間がかかる処理（サーバ通信・ファイルアップロード/ダウンロード等）を行う際は、処理中にインジケーターを表示してユーザに処理中であることを伝え、処理完了前のユーザ操作を受け付けないようにすること。

## ✨ 推奨 (PREFER)

- [COM-04] ✨ **推奨 (PREFER)**: `src/lib/axiosConfig.ts` に定義された Axios インスタンスを使用して通信を行うこと。通信設定を変更したい場合は、既存のインスタンスを編集するか、インスタンスを追加すること。
- [COM-05] ✨ **推奨 (PREFER)**: 一覧形式の画面表示を行う際に、サーバから取得するデータ件数に上限を設定すること。

## 条件付き事項（一覧）

- [参考] `axios.create` での共通設定（`baseURL`・`timeout` 等）は 1 例であり、プロジェクトの要件に応じて設定可能な項目を追加・変更できる。設定可能項目は axios 公式（リクエスト設定／デフォルト値）を参照。
  - **条件成立時: 任意（設定の選択）**
- [参考] 用途が異なる通信先がある場合は、複数の Axios インスタンス（例: `axiosInstance` / `axiosInstanceResist`）を定義してよい。
  - **条件成立時: 任意（手段の選択）**
- [参考] 300 系（リダイレクト）は、デフォルトでは自動的に処理される。
  - **条件成立時: 任意（既定挙動の説明）**

## 参考・推奨実装パターン

> 以下は手順・API 解説（強制度なし）。

### [REF-COM-01] GET の基本形

- `axiosInstance.get(url, { params })` を `async/await` で呼び出す。
- 正常系（2XX・3XX）は `response.data` を取得して処理する。
- 異常系（4XX・5XX・通信不到達・タイムアウト）は `catch` 句で `axios.isAxiosError(error)` により Axios エラーか判定し、`response`／`request`／その他に応じて分岐する。`catch` を記載しないと呼び元にエラーが通知されない。

### [参考] POST の基本形

- JSON 送信: `axiosInstance.post(url, オブジェクト)`。header 未指定時は `Content-Type: application/json` が設定される。
- フォーム送信（`application/x-www-form-urlencoded`）: `URLSearchParams` にパラメータを `append` して `post` する。

### [参考] ファイルアップロード（multipart/form-data）

- `FormData` を生成し `form.append('my_file', file)` でファイルを追加し、`post(url, form)` で送信する。
- ファイル選択は `<input type="file">` の `onChange` で `FileList` を受け取り、`useRef` 等で保持する。

### [REF-COM-02] ファイルダウンロード（API レスポンスをブラウザ保存）

- リクエストコンフィグで `responseType: "arraybuffer"` を指定する（Axios インスタンス側で指定済みなら不要）。
- `response.data` から `new Blob([data], { type: MIMEタイプ })` を生成し、`URL.createObjectURL(blob)` で URL を生成する。
- ファイル名はレスポンスヘッダー `content-disposition` から正規表現で抽出する（取得できない場合はデフォルト名）。
- 一時的な `<a>` タグを `document.createElement('a')` で生成 → `body` に追加 → `download`／`href` を設定 → `click()` → `remove()` でダウンロードを発火・後始末する。
- 後始末として `URL.revokeObjectURL(url)` で生成した URL を解放する（メモリリーク防止）。

### [参考] CSV ダウンロード（文字コード変換あり）

- `papaparse` の `parse`／`unparse` で CSV 文字列と 2 次元配列を相互変換する（行の加工が可能）。
- `encoding-japanese` の `Encoding.convert(..., { to: 'SJIS', from: 'UNICODE', type: 'arraybuffer' })` で SJIS に変換し、`Uint8Array` → `Blob({ type: 'text/csv; charset=Shift_JIS;' })` を生成してダウンロードする。

### [参考] 静的ファイルのダウンロード（キャッシュバスター）

- 公開済み静的ファイルは `axiosDefault.get(url)` で取得する。
- ブラウザがキャッシュを利用しないよう、URL にキャッシュバスター（例: `?${new Date().getTime()}`）を付与する。
