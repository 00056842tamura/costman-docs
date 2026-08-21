---
name: springer-us-mpa
description: Springer US アプリ (MPA 系) — 作業コンテキスト設定
---

# Springer US アプリ (MPA 系) — 作業コンテキスト設定（プロダクト版）

あなたは今、**US アプリ（BFF / MPA 系、Thymeleaf 使用）** のコードに取り組みます。
CLAUDE.md と `.claude/rules/springer-*.md` の Springer プログラミング規約を **すべて** 適用してください。

## プロダクト作業時の注意

- 対象パッケージは `us-mpa/` 配下です
- ベースパッケージは `us-mpa/CLAUDE.md` 冒頭の宣言に従ってください
- 同プロダクトの BS（`bs/`）の API を HTTP 経由で呼び出します（Java import 禁止）
- 他プロダクトのパッケージを直接 import しないでください

---

## Controller (`@Controller`)

- 画面遷移は View 名（文字列）を返すこと
- リクエストは Form クラス（`controller/form/` パッケージ）で受け取ること
- バリデーションは `@Validated` + `BindingResult`（Form クラスの直後に配置）で処理すること
- Ajax レスポンスには `@ResponseBody` を付与すること
- 🚫 `th:utext="..."` および `[(データ取得式)]` による非エスケープ展開禁止（XSS 対策）
- 🚫 `HttpServletResponse` の直接使用禁止

### 空文字→null 自動変換

`StringTrimmerEditorControllerAdvice` により、Form クラスおよびクエリ文字列の String フィールドへの空文字は **自動的に `null` に変換** されます（入力文字のトリムも自動実行）。

- ✅ Form クラスのフィールドはプリミティブ型ではなくラッパークラスを使用すること（`int` → `Integer` 等）
- ✅ 空文字を null として扱うバリデーション（`@NotNull`）を活用すること

```yaml
# トリムを無効にする場合（Springer 1.5.1 以降、デフォルト true）
springer:
  validator:
    string-trimmer-editor-controller-advice:
      trim: false
```

---

## Service (`@Service`)

- `@Transactional` は **付与しない**（US Service はトランザクション制御不要）
- ビジネスロジックを実装し、Repository（BS アプリへの API 呼び出し）を経由してデータを取得・更新すること
- 🚫 BS アプリのエラーメッセージを呼び出し元クライアントに直接出力禁止

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

## セッション

- ✅ セッションに格納するクラスは `Serializable` を実装すること
- ✅ `@SessionAttributes` を使用する場合は `@ModelAttribute` で必ず名前を指定すること

```java
@Controller
@SessionAttributes("itemForm")
public class ItemController {

    @ModelAttribute("itemForm")
    public ItemForm setUpItemForm() {
        return new ItemForm();
    }
}
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

`-` = null/空文字時はチェックしない（`StringTrimmerEditorControllerAdvice` により空文字は null 変換済み）  
`○` = null/空文字時もチェックする  
文字種チェック（1〜9, 12, 14）は `acceptable` 属性で容認文字を追加可能

---

## 二重送信防止

- ✅ 更新系処理のハンドラーメソッドに `@TokenCheck` を付与すること
- ✅ Thymeleaf フォームは `th:action` を使用すること（`_double` トークンが hidden フィールドに自動付与される）
- 🚫 Ajax リクエストへのトークンチェック適用禁止（JavaScript によるボタン無効化で対応すること）

```java
@PostMapping("/sample")
@TokenCheck(value = "/home", errorCode = "com.example.app.message")
public String postSample() {
    // ...
}
```

**`@TokenCheck` 属性**

| 属性 | 型 | 説明 |
|---|---|---|
| `value` | `String` | トークン不一致時のリダイレクト先パス（デフォルト: `/error`）|
| `errorCode` | `String` | エラーメッセージのメッセージコード |
| `useContextPath` | `UseContextPath` | リダイレクト先にコンテキストパスを使用するか（`DEFAULT` / `USED` / `NOT_USED`）|

- トークン不一致時は `DoubleSubmitException` がスローされ `DoubleSubmitExceptionHandler` がリダイレクトを処理する
- `GET`・`HEAD`・`TRACE`・`OPTIONS` にマッピングされたメソッドは `@TokenCheck` を付与してもチェックがスキップされる

```html
<!-- リダイレクト後のエラーメッセージ表示 -->
<p th:if="${SPRINGER_DOUBLE_SUBMIT_LAST_EXCEPTION_MESSAGE} != null"
   th:text="${SPRINGER_DOUBLE_SUBMIT_LAST_EXCEPTION_MESSAGE}"
   class="text-danger">エラーメッセージ</p>
```

---

## ページネーション

### 方式の選択

| 方式 | クラス | 特徴 |
|---|---|---|
| 逐次検索 | `Pageable` / `PageImpl` | ページごとに DB 検索。整合性が高い |
| 全件検索 | `PagedListHolder<E>` | 全件を取得してセッションに保持。ページ遷移が高速 |

### 逐次検索方式（`spring-data-commons`）

```java
@GetMapping({"/", "/items"})
public String getItems(
        @PageableDefault(page = 0, size = 5) Pageable pageable,
        @Validated SearchForm searchForm, BindingResult result, Model model) {

    itemSearchCondition.setPageNumber(Integer.toString(pageable.getPageNumber(), 0));
    itemSearchCondition.setPageSize(Integer.toString(pageable.getPageSize(), 0));

    long count = this.itemService.getCount(searchCondition);
    List<Item> pagingItems = this.itemService.getListByPaging(searchCondition);

    Page<Item> items = new PageImpl<Item>(pagingItems, pageable, count);
    model.addAttribute("items", items);
}
```

BS 側 Mapper XML での SQL 例:

```xml
LIMIT #{pageSize} OFFSET #{pageLocation}
```

### 全件検索方式（`PagedListHolder`）

```java
@Controller
@SessionAttributes("pagination")
public class ItemSearchController {

    @ModelAttribute("pagination")
    public PagedListHolder<Item> pagedListHolder() {
        return new PagedListHolder<Item>();
    }

    @GetMapping({"/", "/items"})
    public String getItems(
            @ModelAttribute("pagination") PagedListHolder<Item> pagination,
            @Validated ItemSearchForm itemSearchForm, BindingResult result, Model model) {

        List<Item> items = this.itemService.getList(searchCondition);
        pagination.setSource(items);
        pagination.setPage(0);
        pagination.setPageSize(5);
        pagination.setMaxLinkedPages(5);
    }

    @GetMapping(value = "/items", params = "page")
    public String pagination(
            @ModelAttribute("pagination") PagedListHolder<Item> pagination, Model model) {
        if (pagination.getSource() == null || pagination.getSource().isEmpty()) {
            return "redirect:/items";
        }
        // ...
    }
}
```

- ✅ `PagedListHolder` をセッションに格納する場合は `@SessionAttributes` と `@ModelAttribute` で名前を指定すること
- ✅ セッションから情報を取得できない場合はリダイレクトで再検索すること

---

## メッセージ ID 命名規約

| 種別 | 命名パターン | 例 |
|---|---|---|
| 業務例外 | `{prefix}.error.business.{名前}` | `webapp.error.business.ItemNotFound` |
| システム例外 | `{prefix}.error.system.{名前}` | `webapp.error.system.NetworkFailed` |
| 画面表示メッセージ | `{prefix}.message.{名前}` | `webapp.message.ItemSearchTitle` |
| バリデーションエラー | `{prefix}.validation.{名前}` | `webapp.validation.InvalidItemCode` |
| 情報ログ | `{prefix}.log.info.{名前}` | `webapp.log.info.ProcessStart` |
| 警告ログ | `{prefix}.log.warn.{名前}` | `webapp.log.warn.RetryOccurred` |

---

## 作業指示

$ARGUMENTS
