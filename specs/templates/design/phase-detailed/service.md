# {機能名} Service 詳細設計

> 工程4 SS 詳細設計WB — {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/service.md に配置（specs・push・PR対象）

## 基本情報

| 項目 | 値 |
|-----|---|
| インターフェース | `{機能名}Service` |
| 実装クラス | `{機能名}ServiceImpl` |
| FQCN（実装） | `{ベースパッケージ}.service.impl.{機能名}ServiceImpl` |
| 対象スタック | BS / US-MPA / US-API（いずれかを選択） |
| 対応機能ID | {機能一覧.md の機能ID} |
| トランザクション | BS / バッチ → `@Transactional`（クラスレベル）/ US → なし |
| 関連 Repository | `{機能名}Repository` |

## メソッド一覧

| メソッド名 | 概要 | トランザクション |
|---|---|---|
| getList | 検索条件に合致する一覧を取得 | readOnly |
| getOne | 1件取得（該当なしは例外） | readOnly |
| createOne | 新規登録（重複は例外） | required |
| updateOne | 更新（楽観排他） | required |
| deleteOne | 削除（楽観排他） | required |

---

## {メソッド名} — {概要}

### シグネチャ

```java
{戻り値型} {メソッド名}({引数型} {引数名});
```

### 処理フロー

1. {ステップ1の説明}
2. {ステップ2の説明}

### 業務ルール（関連項目チェック仕様）

| # | ルール内容 | 違反時の例外 |
|---|---|---|
| 1 | 価格は 1 以上 9999999 以下 | `ApplicationException`（`bs.validation.InvalidPrice`） |
| 2 | 終了日は現在日付以降 | `ApplicationException`（`bs.validation.PastEndDate`） |
| 3 | カテゴリが存在すること | `ResourceNotFoundException`（`bs.error.business.CategoryNotFound`） |

（業務ルールなしの場合は「なし」と記載）

### データ編集仕様

| # | 対象フィールド | 処理内容（変換・計算など） |
|---|---|---|
| 1 | password | BCrypt でハッシュ化してから保存 |
| 2 | createdAt | 現在日時をセット |

（データ編集なしの場合は「なし」と記載）

### 例外（Javadoc `@throws` に記載）

- `ResourceNotFoundException`（メッセージキー: `bs.error.business.XxxNotFound`）
- `ConflictException`（メッセージキー: `bs.error.business.XxxConflict`）

---

<!-- 以降、メソッドごとに同じフォーマットで追記 -->
