# {機能名} Repository 詳細設計

> 工程4 SS 詳細設計WB — {スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/repository.md に配置（specs・push・PR対象）

## 基本情報

| 項目 | 値 |
|-----|---|
| インターフェース | `{機能名}Repository` |
| 実装クラス | `{機能名}RepositoryImpl` |
| FQCN（実装） | `{ベースパッケージ}.repository.impl.{機能名}RepositoryImpl` |
| 対象スタック | BS / バッチ / US-MPA / US-API（いずれかを選択） |
| MyBatis Mapper | `{機能名}Mapper`（BS / バッチのみ） |
| Mapper XML 配置 | `src/main/resources/{パッケージパス}/repository/mapper/{機能名}Mapper.xml` |
| 対象テーブル | `T_XXX`（BS / バッチのみ） |

## アプリ種別

| アプリ種別 | 実装方式 |
|---|---|
| BS / バッチ | MyBatis Mapper 経由で DB アクセス |
| US-MPA / US-API | RestClient で同プロダクトの BS API を呼び出す |

## メソッド一覧

| メソッド名 | 概要 | SQL 種別 |
|---|---|---|
| getList | 検索条件一覧取得 | SELECT |
| getOne | 主キー1件取得 | SELECT |
| createOne | 1件登録 | INSERT |
| updateOne | 1件更新（楽観排他） | UPDATE WHERE version = ? |
| deleteOne | 1件削除（楽観排他） | DELETE WHERE version = ? |

---

## {メソッド名} — {概要}（BS / バッチ）

### データアクセス仕様

| 識別 | 操作 | 対象テーブル | 主な条件 | 使用インデックス | 想定最大件数 |
|---|---|---|---|---|---|
| DA-001 | SELECT | T_ITEM | WHERE id = #{id} | PK（id） | 1件 |
| DA-002 | SELECT | T_ITEM | WHERE name LIKE #{name} | IDX_ITEM_NAME | 1,000件 |

### シグネチャ

```java
{戻り値型} {メソッド名}({引数型} {引数名});
```

### Mapper XML

```xml
<select id="{mapperId}" resultType="{FQCN}">
  SELECT {カラム列}
  FROM T_{テーブル名}
  WHERE {条件}
</select>
```

### Repository 実装

```java
@Override
public {戻り値型} {メソッド名}({引数型} {引数名}) {
    // SELECT の場合: null チェック → ResourceNotFoundException
    // UPDATE/DELETE の場合: 件数チェック → ConflictException
    // INSERT の場合: DBDuplicateKeyException → ConflictException
}
```

---

## {メソッド名} — {概要}（US-MPA / US-API）

### シグネチャ

```java
{戻り値型} {メソッド名}({引数型} {引数名});
```

### RestClient 呼び出し仕様

| 項目 | 値 |
|-----|---|
| HTTP メソッド | GET / POST / PUT / DELETE |
| パス | `/items/{id}` |
| レスポンス型 | `{クラス名}` |
| エラーマッピング | `RestHttpResourceNotFoundException` → `ResourceNotFoundException` |

### Repository 実装

```java
@Override
public {戻り値型} {メソッド名}({引数型} {引数名}) {
    try {
        return this.bsApiRestClient.{method}()
                .uri("{path}", {args})
                .retrieve()
                .body({型}.class);
    } catch (RestHttp{XxxException} e) {
        throw new {XxxException}("{メッセージキー}", new String[]{...}, e);
    }
}
```

---

<!-- 以降、メソッドごとに同じフォーマットで追記 -->
