---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 15 ファイル取り扱い・二重送信防止・ページネーション・Ajax

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 備考: ファイルアップ/ダウンロード（104/105）は本文を持たず Spring Boot プログラミングガイドへの参照のみ。R-15-01〜03 の具体手順は [05](springer-controller.md)/[09](springer-api-client.md) 側の実装に従う。

## 🚫 禁止 (NEVER)

### R-15-01 アップロードファイルの全量オンメモリ保持をしない
アップロードされたファイルデータ全量のオンメモリ保持は禁止（`OutOfMemoryError` 回避）。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-15-02 MultipartFile から取得した InputStream は使用後に必ず close する
`MultipartFile.getInputStream()` で取得した `InputStream` は使用後に必ず `close` する（残留防止）。

### R-15-03 一時ファイルはレスポンス後に必ず削除する
一時ファイルはレスポンス後に必ず削除する（`finally` で `Files.deleteIfExists`、または拡張部品 `TemporaryFileService` に登録）。

### R-15-05 @TokenCheck は同一リクエスト内で一箇所のみに付与する
同一リクエスト内に複数 `@TokenCheck` があると「先勝ち」で処理されるため、原則として一箇所のみに付与する。

### R-15-06 データ量が多い一覧表示はページネーションを使う
多数データの全件表示はメモリ枯渇（`OutOfMemoryError`）・ネットワーク負荷・レスポンス遅延を招くため、ページ分割して表示する。
- 条件: プロジェクトごとに検索結果・画面表示の上限数を設定する。

## ✨ 推奨 (PREFER)

### R-15-07 @TokenCheck は更新系ハンドラーメソッドに付与する
二重送信チェックのマーカー `@TokenCheck` は更新系処理を行うハンドラーメソッドに付与する（`GET`/`HEAD`/`TRACE`/`OPTIONS` にマッピングされたメソッドではチェックがスキップされる）。

### R-15-08 トークン受け渡しは Thymeleaf の th:action で自動付与する
フォームのトランザクショントークン付与は Thymeleaf の `th:action` で自動化する（`_csrf` と `_double` が自動付与）。

## 条件付き事項

### R-15-C1 @TokenCheck の属性でリダイレクト先・エラーメッセージ・コンテキストパスを制御する
`value`（リダイレクトパス、初期値 `/error`）、`errorCode`（メッセージコード）、`useContextPath`（`DEFAULT`/`USED`/`NOT_USED`）を指定できる。

### R-15-C2 Ajax 使用時はトークンチェックではなく Submit ボタン無効化で防止する
Ajax はトランザクショントークンの受け渡しが困難なため、二重送信防止は JavaScript による Submit ボタン無効化で行う。

### R-15-C3 ページ検索方式は逐次検索／全件検索から選択する
逐次検索（表示ごとにページ単位検索）と全件検索（一括検索し US 側でページ編集）のメリット/デメリット（検索数抑制可否・表示頻度・更新頻度・データ量・メモリ）を踏まえて選択する。
- 逐次検索: `Pageable`/`PageImpl`/`Page`（spring-data-commons）、Mapper は LIMIT/OFFSET。
- 全件検索: `PagedListHolder`、結果をセッションに格納し再利用可。

### R-15-C4 Ajax のサーバー側は構成に応じて実装方法を選ぶ
`@RestController` 化できる場合は BS-Controller の方法に従う。通常の `@Controller` 内で JSON 応答する場合は `@ResponseBody` を付与する。

### R-15-C5 独自 RequestDataValueProcessor は SpringerRequestDataValueProcessor を実装する
拡張部品以外で独自の `RequestDataValueProcessor` を登録する場合は `jp.co.nekonet.springer.mvc.support.SpringerRequestDataValueProcessor` をコンポーネントクラスとして実装する（チェインは Order 値順）。

## 参考・推奨実装パターン

- 二重送信は、ボタン無効化・PRGパターンに加え、トークンチェックを併用検討すると有効。
- 二重送信例外（`DoubleSubmitException`）のメッセージ表示は `th:if` でリクエスト属性 `SPRINGER_DOUBLE_SUBMIT_LAST_EXCEPTION_MESSAGE` を判定。ログイン時は `SPRING_SECURITY_LAST_EXCEPTION`。
