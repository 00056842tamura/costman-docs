# {機能名} Controller 詳細設計

> 工程4 SS 詳細設計WB — {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/controller.md に配置（specs・push・PR対象）

## 基本情報

| 項目 | 値 |
|-----|---|
| クラス名 | `{機能名}Controller` |
| FQCN | `{ベースパッケージ}.controller.{機能名}Controller` |
| 対象スタック | BS / US-MPA / US-API（いずれかを選択） |
| 対応機能ID | {機能一覧.md の機能ID、例: bs-001} |
| 関連 Service | `{機能名}Service` |
| アノテーション | `@RestController` / `@Controller`（アプリ種別に応じて） |

## メソッド一覧

| メソッド名 | HTTP | パス | 概要 |
|---|---|---|---|
| getList | GET | `/items` | 一覧取得 |
| getOne | GET | `/items/{id}` | 1件取得 |
| create | POST | `/items` | 登録 |
| update | PUT | `/items/{id}` | 更新 |
| delete | DELETE | `/items/{id}` | 削除 |

---

## {メソッド名} — {概要}

### 入力仕様

| 区分 | パラメータ名 | 型 | 必須 | バリデーションアノテーション |
|---|---|---|---|---|
| クエリ / パス / ボディ | name | String | ○ / × | `@NotNull`, `@Size(max=20)` |

### 単体項目チェック仕様

| # | 項目名 | チェック内容 | エラーコード |
|---|---|---|---|
| 1 | email | メール形式（RFC 5322準拠） | `bs.validation.InvalidEmail` |
| 2 | password | 8文字以上・英数字混在 | `bs.validation.WeakPassword` |
| 3 | name | 最大20文字 | （Spring標準メッセージ） |

### 関連項目チェック仕様

| # | チェック対象項目 | チェック内容 | 実施場所 |
|---|---|---|---|
| 1 | startDate, endDate | 開始日 ≦ 終了日 | Validator クラス |

（相関チェックなしの場合は「なし」と記載）

### 処理フロー

1. リクエストを `{機能名}{操作}Request` にバインド
2. `@Validated` でバリデーション実行（失敗 → 400 返却）
3. `{機能名}Service.{メソッド名}(...)` を呼び出し
4. 結果を `{機能名}View` に変換して返却

### 出力仕様

| HTTP ステータス | 説明 | ボディ型 |
|---|---|---|
| 200 | 正常 | `{機能名}View` |
| 201 | 登録正常 | `{機能名}CreateView` |

### 例外・エラー仕様

| 例外クラス / HTTP | エラーコード | 発生条件 |
|---|---|---|
| 400 / VALIDATION_ERROR | — | バリデーション失敗 |
| 404 / `ResourceNotFoundException` | `bs.error.business.XxxNotFound` | 該当なし |
| 409 / `ConflictException` | `bs.error.business.XxxConflict` | 重複 / 排他 |

---

<!-- 以降、メソッドごとに同じフォーマットで追記 -->
