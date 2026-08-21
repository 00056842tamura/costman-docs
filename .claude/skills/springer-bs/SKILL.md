---
name: springer-bs
description: Springer BS アプリ — 作業コンテキスト設定
---

# Springer BS アプリ — 作業コンテキスト設定（プロダクト版）

あなたは今、**BS アプリ（業務 API）** のコードに取り組みます。
CLAUDE.md と `.claude/rules/springer-*.md` の Springer プログラミング規約を **すべて** 適用してください。

## プロダクト作業時の注意

- 対象パッケージは `bs/` 配下です
- ベースパッケージは `bs/CLAUDE.md` 冒頭の宣言に従ってください
- 他プロダクトのパッケージ（`他スタック`）を直接 import しないでください
- `_hint/` 配下の見本ファイルはプロダクトルート直下を参照します

---

## Controller (`@RestController`)

- REST API として実装すること
- リクエストボディは `@RequestBody` + Request クラス（`controller/request/` パッケージ）で受け取ること
- レスポンスボディは View クラス（`controller/view/` パッケージ）で返すこと
- バリデーションは `@Validated` を使用し、Springer アノテーション（`jp.co.nekonet.springer.validator.constraints`）を積極的に活用すること
- ハンドラーメソッドの `BindingResult` は `@Validated` が付与された引数の直後に配置すること
- 🚫 `HttpServletResponse` の直接使用禁止

### ConflictException の Controller ハンドリング

`ConflictException` は Controller で `@ExceptionHandler` を定義して HTTP 409 を返すこと。

```java
@ResponseStatus(HttpStatus.CONFLICT)
@ExceptionHandler(ConflictException.class)
public RestErrorInfo conflict(ConflictException ex) {
    return new RestErrorInfo(ex);
}
```

`RestErrorInfo`（`jp.co.nekonet.springer.exceptionhandler.RestErrorInfo`）のレスポンス形式:

```json
{
    "errorCode": "string",
    "message": "string",
    "detail": null
}
```

---

## Service (`@Service` + `@Transactional`)

- **必ず `@Transactional` をクラスレベルに付与**すること
- ビジネスロジックを実装し、Repository を呼び出すこと
- Controller が期待する形にデータを変換して返すこと

### トランザクション動作

| 状況 | 結果 |
|---|---|
| 正常終了（または checked exception が呼び出し元に返る） | コミット |
| RuntimeException が呼び出し元に返る | ロールバック |

- 🚫 データアクセス例外（`DBDuplicateKeyException` 等）を Service で `catch` 禁止
- 🚫 同一クラス内で `@Transactional` メソッドを呼び出しても新規トランザクションは開始されない

---

## Repository (`@Repository`)

- DB アクセスは MyBatis Mapper を経由すること
- **データアクセス例外はこのレイヤーでのみ `catch`** すること
- Mapper からの戻り値を確認し、想定外の結果は業務例外にラップして上位に伝搬させること

### 戻り値のハンドリング

```java
// SELECT 1件: null なら ResourceNotFoundException
ItemCategory result = this.itemMapper.selectRecord(id);
if (result == null) {
    throw new ResourceNotFoundException("xxx.error.business.ItemNotFound", new String[]{id});
}

// UPDATE/DELETE: 更新件数不一致なら ConflictException (楽観排他)
int count = this.itemMapper.updateRecord(item);
if (count != 1) {
    throw new ConflictException("xxx.error.business.ItemConflict", new String[]{item.getId()});
}

// INSERT: 一意制約違反を ConflictException にラップ
try {
    this.itemMapper.insertRecord(item);
} catch (DBDuplicateKeyException e) {
    throw new ConflictException("xxx.error.business.ItemAlreadyExists", e);
}
```

---

## 例外クラス体系

### 業務例外（`jp.co.nekonet.springer.exceptions`）

| クラス | 用途 | HTTP ステータス |
|---|---|---|
| `ApplicationException` | 汎用業務エラー | 400 |
| `ResourceNotFoundException` | リソース未検出 | 404 |
| `ConflictException` | 重複・排他競合 | 409 |
| `SystemException` | 汎用システムエラー | 500 |

### データアクセス例外 — DB (`jp.co.nekonet.springer.database`)

| クラス | 発生条件 |
|---|---|
| `DBDuplicateKeyException` | 一意制約違反 |
| `DBPessimisticLockingFailureException` | 悲観排他エラー |
| `DBOptimisticLockingFailureException` | 楽観排他エラー |
| `DBCannotGetJdbcConnectionException` | JDBC 接続エラー |
| `DBDataAccessException` | その他の SQL 例外 |

- ✅ Repository で `catch` した DB 例外は、必ず業務例外にラップして再スローすること
- 🚫 Controller・Service で DB 例外クラスを `import`・直接参照禁止

---

## MyBatis Mapper

### インターフェイス定義

```java
@Mapper
public interface ItemMapper {
    Item selectRecord(String id);
    List<Item> selectList(ItemSearchCondition condition);
    Cursor<Item> selectListCursor(ItemSearchCondition condition);  // 大量件数
    int insertRecord(Item item);
    int updateRecord(Item item);
    boolean deleteRecord(@Param("id") String id, @Param("version") int version);
}
```

### XML ファイル配置

- ファイル名: `XxxMapper.xml`（インターフェイスと同名）
- 配置場所: `src/main/resources/` + インターフェイスと同じパッケージパス

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="jp.co.example.bs.item.repository.mapper.ItemMapper">

  <select id="selectRecord" resultType="jp.co.example.bs.item.model.Item">
    select id, name, end_date
    from m_item
    where id = #{id}
  </select>

</mapper>
```

### resultType（スネークケース→キャメルケース自動変換）

`resultType` を使用するには `application.yml` に以下の設定が必要:

```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

DB カラム名（スネーク）とクラスのフィールド名（キャメル）が自動的にマッピングされる。

### resultMap（親子関係・カスタムマッピング）

カラム名の自動変換では対応できない場合（JOIN結果の親子構造など）は `resultMap` を使用:

```xml
<resultMap id="ItemResultMap" type="jp.co.example.bs.item.model.Item">
  <id column="id" property="id" />
  <result column="name" property="name" />
  <result column="name_kana" property="nameKana" />
  <result column="end_date" property="endDate" />
  <association property="itemCategory" javaType="jp.co.example.bs.item.model.ItemCategory">
    <result column="item_category_id" property="id" />
    <result column="item_category_name" property="name" />
  </association>
  <association property="maker" javaType="jp.co.example.bs.item.model.Maker">
    <result column="maker_id" property="id" />
    <result column="maker_name" property="name" />
  </association>
</resultMap>

<select id="selectRecord" resultMap="ItemResultMap">
  select i.id, i.name, i.name_kana, i.end_date,
         ic.id as item_category_id, ic.name as item_category_name,
         m.id as maker_id, m.name as maker_name
  from m_item i
  left join m_item_category ic on i.item_category_id = ic.id
  left join m_maker m on i.maker_id = m.id
  where i.id = #{id}
</select>
```

### jdbcType の指定

- ✅ `INSERT`・`UPDATE`・`DELETE` 文で `null` の可能性があるカラムには `jdbcType` を指定すること

```xml
#{endDate, jdbcType=DATE}
#{description, jdbcType=VARCHAR}
#{price, jdbcType=INTEGER}
```

### 複数引数の場合

```java
boolean deleteRecord(@Param("id") String id, @Param("version") int version);
```

```xml
where id = #{id} and version = #{version}
```

### Cursor（大量件数処理）

```java
try (Cursor<Item> cursor = this.itemMapper.selectListCursor(condition)) {
    for (Item item : cursor) {
        // 1件ずつ処理
    }
} catch (IOException e) {
    throw new SystemException("xxx.error.system.FileFailed", e);
}
```

---

## 動的 SQL

### `<if>` — 条件付き SQL

```xml
<select id="selectList">
  select id, name from m_item
  where m_item.id like #{id} || '%'
  <if test="name != null">
    and m_item.name like #{name} || '%'
  </if>
  <if test="nameKana != null">
    and m_item.name_kana like #{nameKana} || '%'
  </if>
</select>
```

### `<where>` — WHERE 句の自動生成（先頭の AND/OR を自動除去）

```xml
<select id="selectList">
  select id, name from m_item
  <where>
    <if test="id != null">m_item.id like #{id} || '%'</if>
    <if test="name != null">and m_item.name like #{name} || '%'</if>
    <if test="nameKana != null">and m_item.name_kana like #{nameKana} || '%'</if>
  </where>
</select>
```

### `<set>` — UPDATE の SET 句（末尾カンマを自動除去）

```xml
<update id="updateRecord">
  update m_authentication
  <set>
    <if test="authentication.passwordHash != null">password_hash = #{authentication.passwordHash},</if>
    <if test="authentication.lockedAt != null">locked_at = #{authentication.lockedAt},</if>
    last_modified_at = CURRENT_TIMESTAMP(),
  </set>
  where id = #{authentication.id}
</update>
```

### `<choose>/<when>/<otherwise>` — switch-case に相当

```xml
<choose>
  <when test="order == '11'">order by average_score asc, id asc</when>
  <when test="order == '12'">order by average_score desc, id asc</when>
  <otherwise></otherwise>
</choose>
```

### `<foreach>` — IN 句の生成

```xml
<select id="selectUsers" resultMap="User">
  SELECT id, name FROM m_user
  WHERE id in
  <foreach item="condition" index="idx" collection="list"
      open="(" separator="," close=")">
    #{condition.id}
  </foreach>
</select>
```

### `${}` — 文字列直接展開（SQL インジェクション注意）

テーブル名・カラム名はプレースホルダーにできないため `${}` で展開する。  
**⚠️ 展開される文字列はエスケープされないため SQL インジェクションに注意。ユーザー入力値に使用禁止。**

```java
List<Item> selectList(@Param("condition") ItemSearchCondition cond, @Param("table") String tableName);
```

```xml
<select id="selectList" resultMap="Item">
  SELECT id, name FROM ${table} WHERE id LIKE #{condition.id} || '%'
</select>
```

---

## 一時ファイルの取り扱い

### パターン1: 呼び出し元から渡した一時ファイル（Controller で削除）

```java
@PostMapping("/admin/makers/edit")
public String updateList(@Validated MakerAllUpdateForm form, BindingResult bindingResult, ...) {
    Path makerCsvPath = null;
    try {
        makerCsvPath = Files.createTempFile("FoodShop", ".csv");
        form.getCsvFile().transferTo(makerCsvPath);
        this.makerService.updateList(makerCsvPath);
    } catch (IOException | IllegalStateException e) {
        // エラー処理
    } finally {
        if (makerCsvPath != null) {
            try {
                Files.deleteIfExists(makerCsvPath);
            } catch (Exception e) {
                LOGGER.warn(MessageManager.getMessage(MessageCode.WARN_FILE_DELETION_FAILURE), makerCsvPath.toString());
            }
        }
    }
}
```

### パターン2: 呼び出し元に返す一時ファイル（TemporaryFileService で自動削除）

```java
import jp.co.nekonet.springer.mvc.support.temporary.TemporaryFileService;

public class ItemServiceImpl implements ItemService {

    private final TemporaryFileService temporaryFileService;

    @Override
    public Path createCsv() {
        Path path = Files.createTempFile("item", ".csv");
        this.temporaryFileService.register(path);  // レスポンス後に自動削除される
        // ... ファイル生成処理 ...
        return path;
    }
}
```

- ✅ `MultipartFile.getInputStream()` で取得した `InputStream` は使用後に必ず `close` すること
- 🚫 アップロードファイルのオンメモリ全量保持禁止

---

## マスキング

### マスキング種別

| 種別 | アノテーション | 概要 |
|---|---|---|
| 全桁マスキング | `@Sensitive` | すべての文字を `*` でマスキング |
| 奇数桁マスキング | `@Sensitive(OddMasker.class)` | 奇数桁のみマスキング（例: `山田太郎` → `*田*郎`） |

```java
import jp.co.nekonet.springer.masking.MaskingToStringBuilder;
import jp.co.nekonet.springer.masking.Sensitive;

public class User {
    private String userId;

    @Sensitive
    private String name;

    @Override
    public String toString() {
        return MaskingToStringBuilder.toString(this);
    }
}
```

### マスキングの有効・無効設定

```yaml
# 全体設定（デフォルト true）
springer:
  masking:
    enabled: false

# 個別設定（特定クラス・フィールドの無効化）
springer:
  masking:
    category:
      jp.co.example.model.User: false          # クラス全体
      jp.co.example.model.User.name: true      # フィールド（より深い設定が優先）
      ROOT: false                              # 未マッチ時のデフォルト
```

### 独自マスキング処理

```java
@Component
public class MyMasker implements Masker {
    @Override
    public String execute(Object value) {
        return "[masked]";
    }
}

// フィールドへの適用
@Sensitive(MyMasker.class)
private String name;
```

---

## メッセージリソース

```java
import jp.co.nekonet.springer.message.MessageManager;

// プレースホルダーなし
String message1 = MessageManager.getMessage("hello.any");

// プレースホルダーあり
String message2 = MessageManager.getMessage("hello.one", "佐藤");
String message3 = MessageManager.getMessage("hello.two", "鈴木", "田中");
```

---

## 入力チェック

### 標準バリデーター

| アノテーション | チェック内容 | 属性 |
|---|---|---|
| `@NotNull` | null でない | - |
| `@NotEmpty` | 文字列・配列・List・Map が空でない | - |
| `@Size` | 文字列・配列等の長さ最小/最大 | `min`、`max` |
| `@Min` | 整数の最小値 | `value` |
| `@Max` | 整数の最大値 | `value` |
| `@Range` | 整数の最小/最大 | `min`、`max` |
| `@DecimalMin` | 数値の最小値 | `value`、`inclusive` |
| `@DecimalMax` | 数値の最大値 | `value`、`inclusive` |
| `@Digits` | 数値の最大桁 | `integer`、`fraction` |
| `@Pattern` | 正規表現 | `regexp` |
| `@NumberFormat` | 数値フォーマット | `pattern` |
| `@DateTimeFormat` | 日時フォーマット | `pattern` |

- ✅ サロゲートペアに対応する必要がある場合は `@Size` の代わりに `@CodePointLength` を使用すること
- ✅ Form クラスのフィールドはプリミティブ型ではなくラッパー型を使用すること（`int` → `Integer` 等）

### Springer バリデーター（`jp.co.nekonet.springer.validator.constraints`）

| No. | アノテーション | チェック内容 | null 時 | 空文字時 |
|:---:|---|---|:---:|:---:|
| 1 | `@Half` | 半角 | - | - |
| 2 | `@HalfAlphaNumeric` | 半角英数字 | - | - |
| 3 | `@HalfAlphabet` | 半角英字 | - | - |
| 4 | `@HalfNumeric` | 半角数字 | - | - |
| 5 | `@HalfKatakana` | 半角カタカナ | - | - |
| 6 | `@NotHalfKatakana` | 半角カタカナを含まない | - | - |
| 7 | `@Full` | 全角 | - | - |
| 8 | `@Hiragana` | ひらがな | - | - |
| 9 | `@FullKatakana` | 全角カタカナ | - | - |
| 10 | `@MinByteLength` | 最小バイト数 | - | ○ |
| 11 | `@MaxByteLength` | 最大バイト数 | - | ○ |
| 12 | `@NotContains` | 指定の文字を含まない | - | - |
| 13 | `@SafeEncoding` | 文字エンコーディング（デフォルト: Shift_JIS） | - | - |
| 14 | `@Enable` | 有効文字（Unicodeに定義、ISO制御文字でない） | - | - |
| 15 | `@UploadFileRequired` | アップロードファイルの必須 | 常にチェック | 常にチェック |
| 16 | `@UploadFileNotEmpty` | アップロードファイルサイズが空でない | - | - |
| 17 | `@UploadFileMaxSize` | アップロードファイルの最大バイト数（デフォルト: 1048576） | - | - |

`-` = null/空文字時はチェックしない（`@NotNull` との組み合わせで制御）  
`○` = null/空文字時もチェックする  
文字種チェック（1〜9, 12, 14）は `acceptable` 属性で容認文字を追加可能

---

## APIキー認証

BS アプリが APIキー認証を採用する場合の実装手順。

### アプリケーションレイヤー設定

```yaml
springer:
  application-layer: NONE
```

### リクエストヘッダー形式

```
Authorization: ApiKey {ID}.{APIキー}
```

- ID・APIキー最大長はデフォルト 128 文字（`springer.security.apikey.id.max-length` / `api-key.max-length` プロパティで変更可）
- ✅ APIキーは平文で保持せず BCrypt ハッシュで DB に永続化すること

### SecurityConfig

```java
import jp.co.nekonet.springer.security.apikey.ApiKeyConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .with(new ApiKeyConfigurer<>(), Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
```

### ApiKeyUserDetailsService の実装

```java
@Service
public class ApiKeyUserDetailsServiceImpl implements ApiKeyUserDetailsService {

    private final AuthenticationService authenticationService;

    public ApiKeyUserDetailsServiceImpl(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public UserDetails loadUserDetails(String id) throws UsernameNotFoundException {
        try {
            UserAuthentication auth = this.authenticationService.getOne(id);
            return new CustomUserDetails(
                    auth.getId(),
                    auth.getPasswordHash(),   // BCrypt ハッシュ
                    auth.isEnabled(),
                    auth.isAccountNonExpired(),
                    auth.isCredentialsNonExpired(),
                    auth.isAccountNonLocked(),
                    AuthorityUtils.createAuthorityList(auth.getAuthority()),
                    this.toUserProfile(auth));
        } catch (ResourceNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }
}
```

### ログレベルの注意

- ⚠️ `jp.co.nekonet.springer.security.apikey.ApiKeyPreAuthenticatedProcessingFilter` は `DEBUG` ログに APIキーが出力されるため、**運用時は `OFF` または `INFO` 以上に設定すること**

---

## メッセージ ID 命名規約

| 種別 | 命名パターン | 例 |
|---|---|---|
| 業務例外 | `{prefix}.error.business.{名前}` | `webapp.error.business.ItemNotFound` |
| システム例外 | `{prefix}.error.system.{名前}` | `webapp.error.system.FileFailed` |
| API レスポンスメッセージ | `{prefix}.message.{名前}` | `webapp.message.ItemCreated` |
| バリデーションエラー | `{prefix}.validation.{名前}` | `webapp.validation.InvalidItemCode` |
| 情報ログ | `{prefix}.log.info.{名前}` | `webapp.log.info.ProcessStart` |
| 警告ログ | `{prefix}.log.warn.{名前}` | `webapp.log.warn.FileDeleteFailed` |
| エラーログ | `{prefix}.log.error.{名前}` | `webapp.log.error.DBAccessFailed` |

---

## 作業指示

$ARGUMENTS
