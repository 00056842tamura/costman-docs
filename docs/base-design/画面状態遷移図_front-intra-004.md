# 画面状態遷移図

> 工程2 UI（基本設計）の正式成果物。`docs/base-design/画面状態遷移図_{機能ID}.md` に生成する（**1機能ID = 1画面**・frontend のみ）。
> 画面（コンポーネント）内部の state 設計の軸になる。`コンポーネント仕様書_{機能ID}.md` の State 定義と対応させる。

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | Claude (basic-design移行) |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | Claude (basic-design移行) |

## 基本情報

| 項目 | 内容 |
|---|---|
| 機能ID | front-intra-004 |
| 画面名 | 工番別収支データ参照画面 |
| コンポーネント名 | `KobanbetsuShushi`（`features/kobanbetsuShushi/components/KobanbetsuShushi.tsx`。ダイアログ `KobanSearchDialog` を内包） |

> 状態は `KobanbetsuShushi.tsx` の `useState`（`kobanNm` / `nendoList` / `sortingYojitsuFlg` / `sortingColumn` / `sortingErrorMessage` / `sortOrder` / `registerDateList` / `sokuhoFlg` / `shushiData` / `historyShushiData` / `totalData` / `pinedColumnWidth` / `isShowDialog` 等）および `KobanSearchDialog.tsx` の `useState`（`kobanData`）・ローディングコンテキスト（`useLoading`）から抽出した、画面全体としての実効的な状態遷移を表す。

## 状態遷移図

```mermaid
stateDiagram-v2
    [*] --> Idle : 画面表示（年度リスト初期化・nendoList設定）

    %% --- 収支検索 ---
    Idle --> Loading : 「収支検索」ボタン押下（単体項目チェックOK）
    Displayed --> Loading : 「収支検索」ボタン押下（再検索・単体項目チェックOK）
    Sorted --> Loading : 「収支検索」ボタン押下（再検索・単体項目チェックOK）
    SearchError --> Loading : 「収支検索」ボタン押下（再検索・単体項目チェックOK）
    DialogResult --> Loading : ダイアログで工番選択（行クリック／ダブルクリック。ダイアログは閉じる）

    Loading --> Displayed : 200 OK（工番名・登録日時・収支一覧・合計表・速報値フラグを反映）
    Loading --> SearchError : 400/401/404/409（axiosErrorHandlingでトースト表示）

    %% --- 並び替え（通信なし） ---
    Displayed --> Sorted : 「並び替え」ボタン押下（関連項目チェックOK）
    Sorted --> Sorted : 「並び替え」ボタン押下（条件変更・再並び替え）
    Displayed --> SortError : 「並び替え」ボタン押下（検索結果なし／ソート列未選択／予実未選択エラー）
    Sorted --> SortError : 「並び替え」ボタン押下（同上エラー）
    SortError --> Displayed : 「並び替え」ボタン押下（正しい条件で実行）
    SortError --> Sorted : 「並び替え」ボタン押下（正しい条件で再実行）

    %% --- 工番検索ダイアログ ---
    Idle --> DialogOpen : 「工番検索」ボタン押下（isShowDialog=true）
    Displayed --> DialogOpen : 「工番検索」ボタン押下
    Sorted --> DialogOpen : 「工番検索」ボタン押下
    SearchError --> DialogOpen : 「工番検索」ボタン押下

    DialogOpen --> DialogOpen : ダイアログ「検索」ボタン押下（単体項目チェックNG、または関連項目チェックNG＝3項目未入力・通信なし）
    DialogOpen --> DialogSearching : ダイアログ「検索」ボタン押下（単体項目チェックOK・関連項目チェックOK）
    DialogSearching --> DialogResult : 200 OK（工番一覧反映・0件時は空表示）
    DialogSearching --> DialogOpen : 400/401/404/409（axiosErrorHandlingでトースト表示・ダイアログ内に留まる）

    DialogOpen --> Idle : ダイアログを閉じる（Idleから起動していた場合。検索条件・結果は変化なし）
    DialogOpen --> Displayed : ダイアログを閉じる（Displayedから起動していた場合）
    DialogOpen --> Sorted : ダイアログを閉じる（Sortedから起動していた場合）
    DialogOpen --> SearchError : ダイアログを閉じる（SearchErrorから起動していた場合）
    DialogResult --> Idle : ダイアログを閉じる（Idleから起動していた場合。工番選択せず×で閉じた場合）
    DialogResult --> Displayed : ダイアログを閉じる（Displayedから起動していた場合。工番選択せず×で閉じた場合）
    DialogResult --> Sorted : ダイアログを閉じる（Sortedから起動していた場合。工番選択せず×で閉じた場合）
    DialogResult --> SearchError : ダイアログを閉じる（SearchErrorから起動していた場合。工番選択せず×で閉じた場合）

    %% --- 画面終了 ---
    Idle --> [*] : 「メニューへ」ボタン押下
    Displayed --> [*] : 「メニューへ」ボタン押下
    Sorted --> [*] : 「メニューへ」ボタン押下
    SearchError --> [*] : 「メニューへ」ボタン押下
```

## 状態一覧

| 状態 | 概要 | 画面表示 |
|---|---|---|
| Idle | 初期表示・検索条件入力待ち（`shushiData`/`totalData` 未検索状態、`isShowDialog=false`） | 検索条件フォーム表示。収支一覧・合計表は空。登録日時表は初期値（`----/--/-- --:--:--`。「プロ管（計画値）」は未登録扱いで「プロ管取込」リンク表示） |
| Loading | 収支検索API（bs-005）呼出中（`useLoading` の `runWithLoading` 実行中）。呼出前に `clearResultData` で検索結果をクリア済み | ローディング表示（画面全体を覆う） |
| Displayed | 収支検索成功・検索結果表示中（`shushiData`/`historyShushiData`/`totalData`/`sokuhoFlg`/`registerDateList`/`kobanNm` を反映済み） | 収支一覧グリッド・合計表グリッド・工番名・登録日時（5件）を表示。速報値の月に `(*)` 表示 |
| SearchError | 収支検索失敗（400/401/404/409） | 検索結果は空のまま（`clearResultData` 実行済み）。トースト通知でエラーメッセージ表示。401時は3秒後に自動ログアウト |
| Sorted | 並び替え適用済み（`shushiData` を社員単位でグループ化しソート。合計表・`historyShushiData` は対象外） | 収支一覧グリッドの行順が並び替え条件（予実／列／昇順・降順）に従って変化。合計表は変化なし |
| SortError | 並び替え条件エラー（`sortingErrorMessage` 設定） | 並び替え条件欄下にエラーメッセージ表示（検索結果なし／ソート列未選択／予実未選択で収支列を選択、のいずれか） |
| DialogOpen | 工番検索ダイアログ表示中・検索前または検索エラー時（`isShowDialog=true`、`kobanData` は初期状態またはクリア済み） | ダイアログ（工番コード親番・子番・工番名の入力欄、工番一覧は空） |
| DialogSearching | ダイアログ内で工番検索API（bs-004）呼出中 | ダイアログ内ローディング表示 |
| DialogResult | 工番検索成功・工番一覧表示中（`kobanData` に検索結果を反映。0件時は空一覧） | ダイアログ内に工番一覧グリッドを表示（0件時はグリッドに行なし） |

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | Claude (basic-design移行) | 初版 |
