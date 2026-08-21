---
name: springer-us-api
description: Springer US アプリ (SPA / API 系) — 作業コンテキスト設定
---

# Springer US アプリ (SPA / API 系) — 作業コンテキスト設定（プロダクト版）

あなたは今、**US アプリ（BFF / SPA 系または REST API 系）** のコードに取り組みます。
CLAUDE.md と `.claude/rules/springer-*.md` の Springer プログラミング規約を **すべて** 適用してください。

## プロダクト作業時の注意

- 対象パッケージは `us-api/` 配下です
- ベースパッケージは `us-api/CLAUDE.md` 冒頭の宣言に従ってください
- 同プロダクトの BS（`bs/`）の API を HTTP 経由で呼び出します（Java import 禁止）
- SPA フロント（Reacter）からのリクエストを受け取ります（Reacter 規約は `.claude/rules/reacter-*.md` を参照）
- 他プロダクトのパッケージを直接 import しないでください

---

## Controller (`@RestController`)

- REST API として実装すること
- リクエストボディは `@RequestBody` + Request クラス（`controller/request/` パッケージ）で受け取ること
- レスポンスボディは View クラス（`controller/view/` パッケージ）または適切なレスポンス用クラスで返すこと
- バリデーションは `@Validated` を使用し、Springer アノテーション（`jp.co.nekonet.springer.validator.constraints`）を積極的に活用すること
- ハンドラーメソッドの `BindingResult` は `@Validated` が付与された引数の直後に配置すること
- 🚫 `HttpServletResponse` の直接使用禁止

### JSON リクエストの null / 空文字ルール

- ✅ 値がない項目は **`null` を送るか、項目自体を JSON から省略**すること
- 🚫 空文字（`""`）を送ることを禁止（`StringTrimmerEditorControllerAdvice` は `@RequestBody` には適用されないため）

### JSON 全項目で空文字→null 変換（Springer 1.3 以降）

```yaml
springer:
  jackson:
    object-mapper:
      empty-to-null-string: true  # デフォルト false
```

### 個々の項目で空文字→null 変換

```java
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jp.co.nekonet.springer.jackson.EmptyToNullStringDeserializer;

public class ItemRequest implements Serializable {
    private String id;

    @JsonDeserialize(using = EmptyToNullStringDeserializer.class)
    public void setId(String id) {
        this.id = id;
    }
}
```

---

## Service (`@Service`)

- `@Transactional` は **付与しない**（US Service はトランザクション制御不要）
- ビジネスロジックを実装し、Repository（BS アプリへの API 呼び出し）を経由してデータを取得・更新すること
- 🚫 BS アプリのエラーメッセージを呼び出し元クライアントに直接出力禁止（US 側で適切なメッセージに変換すること）

---

## Repository (`@Repository`)

- 🚫 DB への直接アクセス禁止
- RestClient または RestTemplate を使用して BS アプリの API を呼び出すこと
- **HTTP 系データアクセス例外はこのレイヤーでのみ `catch`** すること

### URI 組み立て

```java
URI uri = UriComponentsBuilder
        .fromUri(this.baseUri)
        .path("/items/{id}")
        .queryParam("name", "{name}")
        .build("item001", "テスト");

// プレースホルダーがない場合
URI uri = UriComponentsBuilder
        .fromUri(this.baseUri)
        .path("/items")
        .build(Collections.emptyMap());
```

- ✅ URI の組み立てにはプレースホルダーを使用すること（文字列連結禁止）
- ✅ プレースホルダーがない場合は `Collections.emptyMap()` を指定すること

---

## 例外クラス体系

### 業務例外（`jp.co.nekonet.springer.exceptions`）

| クラス | 用途 | HTTP ステータス |
|---|---|---|
| `ApplicationException` | 汎用業務エラー | 400 |
| `ResourceNotFoundException` | リソース未検出 | 404 |
| `ConflictException` | 重複・排他競合 | 409 |

### データアクセス例外 — HTTP (`jp.co.nekonet.springer.resttemplate`)

| クラス | 発生条件 |
|---|---|
| `RestHttpResourceNotFoundException` | BS アプリが 404 を返した |
| `RestHttpConflictException` | BS アプリが 409 を返した |
| `RestHttpClientErrorException` | BS アプリが上記以外の 400 系を返した |
| `RestHttpServerErrorException` | BS アプリが 500 系を返した |
| `ResourceAccessException` | I/O エラー (RestClient) |
| `RestResourceAccessException` | I/O エラー (RestTemplate) |

### US Repository での例外ハンドリング例

```java
@Override
public Item getOne(String id) {
    try {
        return this.restClient.get()
                .uri(...)
                .retrieve()
                .body(Item.class);
    } catch (RestHttpResourceNotFoundException e) {
        throw new ResourceNotFoundException("xxx.error.business.ItemNotFound", new String[]{id}, e);
    } catch (RestHttpConflictException e) {
        throw new ConflictException("xxx.error.business.ItemConflict", e);
    }
}
```

- 🚫 Service・Controller で HTTP データアクセス例外を `catch` 禁止

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
| 10 | `@MinByteLength` | 最小バイト数（デフォルト encoding: Windows-31J） | - | ○ |
| 11 | `@MaxByteLength` | 最大バイト数（デフォルト encoding: Windows-31J） | - | ○ |
| 12 | `@NotContains` | 指定の文字を含まない | - | - |
| 13 | `@SafeEncoding` | 文字エンコーディング（デフォルト: Shift_JIS） | - | - |
| 14 | `@Enable` | 有効文字（Unicodeに定義、ISO制御文字でない） | - | - |
| 15 | `@UploadFileRequired` | アップロードファイルの必須 | 常にチェック | 常にチェック |
| 16 | `@UploadFileNotEmpty` | アップロードファイルサイズが空でない | - | - |
| 17 | `@UploadFileMaxSize` | アップロードファイルの最大バイト数（デフォルト: 1048576） | - | - |

`-` = null/空文字時はチェックしない（`@RequestBody` では `StringTrimmerEditorControllerAdvice` が効かないため、null または省略で送ること）  
`○` = null/空文字時もチェックする  
文字種チェック（1〜9, 12, 14）は `acceptable` 属性で容認文字を追加可能

---

## メッセージ ID 命名規約

| 種別 | 命名パターン | 例 |
|---|---|---|
| 業務例外 | `{prefix}.error.business.{名前}` | `webapp.error.business.ItemNotFound` |
| システム例外 | `{prefix}.error.system.{名前}` | `webapp.error.system.NetworkFailed` |
| API レスポンスメッセージ | `{prefix}.message.{名前}` | `webapp.message.ItemCreated` |
| バリデーションエラー | `{prefix}.validation.{名前}` | `webapp.validation.InvalidItemCode` |
| 情報ログ | `{prefix}.log.info.{名前}` | `webapp.log.info.ProcessStart` |
| 警告ログ | `{prefix}.log.warn.{名前}` | `webapp.log.warn.RetryOccurred` |

---

## 作業指示

$ARGUMENTS
