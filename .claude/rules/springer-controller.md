---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 05 Controller（Thymeleaf・セッション含む）

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: クラス定義/DI は [03](springer-package-class-naming.md)/[04](springer-di-bean.md)、入力チェックは [06](springer-validation-input.md)、例外は [10](springer-exception.md)、ファイルは [15](springer-file-prevention.md)。

## 🚫 禁止 (NEVER)

### R-05-01 Thymeleaf でエスケープなしの文字列展開をしない
`th:utext="データ取得式"` および `[(データ取得式)]` での文字列展開は禁止。
- 理由 / 背景: これらは XSS に対してエスケープしない。`th:text` / `[[…]]` はエスケープされる（ただし `[[…]]` インライン展開自体は非推奨）。

### R-05-03 HttpServletResponse を直接使用しない
レスポンスは主な戻り値（`String` / `ResponseEntity<?>` / `ModelAndView`）で返し、`HttpServletResponse` を直接使用しない。
- 補足: 本ルールは禁止カテゴリとして確定する。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-05-02 BindingResult は @Validated 付き Form クラスの直後に配置する
引数順は ①`@Validated` ②Form クラス ③`BindingResult`。`BindingResult` は必ず Form クラスの直後に続けて記述する（US/BS 共通）。

### R-05-04 セッション格納クラスは Serializable を実装する
セッションに格納するクラスは `Serializable` を実装する。
- 理由 / 背景: Redis 等の外部サービスでセッション管理するため。`@SessionScope` Bean、`@SessionAttributes` 用クラスとも対象。
- 補足: `@SessionScope` Bean にはセッション削除 API がないため、不要になったら保持フィールドを `null` クリアしてメモリを解放する（`@SessionAttributes` は `SessionStatus.setComplete()` で破棄＝R-05-C1）。

### R-05-N1 アノテーション種別を用途で使い分ける
画面系 US-Controller は `@Controller`、外部公開 API・REST API を受ける Controller（BS、および US の API）は `@RestController` を付与する（API は BS-Controller の規約に従う）。

### R-05-N2 ハンドラーメソッドは public で定義する
ハンドラーメソッドのアクセス修飾子は `public` とする（US/BS 共通）。

### R-05-N3 リクエスト受け取りの規約に従う
- `@PathVariable` の変数名はパス変数 `{ }` の名前と一致させる。
- JSON リクエストボディ（BS）は専用マッピングクラスを用意し `@RequestBody` で受け取る。

### R-05-N4 画面遷移は View 名を String で、データは Model で返す（画面系）
View 名は拡張子なしのテンプレート名（サブフォルダは `"menu/hoge"`）を `String` で返す。View へ渡す値は `Model.addAttribute()`（リクエストスコープ）でセットする。

## ✨ 推奨 (PREFER)

### R-05-N5 クラス共通パスは @RequestMapping でまとめる
複数ハンドラーで共通の上位リクエストパスは、クラスに `@RequestMapping("上位パス")` を付け、各メソッドで下位パスを指定する。

### R-05-N6 Form/Request はモデルクラスへ変換してから Service を呼ぶ
送信データクラス（Form/Request）の入力値をデータ受け渡し用モデルクラスに詰め替えてから Service を呼び出す（R-01-04 と整合）。

### R-05-N7 大容量ファイルダウンロードは ResponseEntity<FileSystemResource> でメモリを抑える
ファイルダウンロードは `ResponseEntity<FileSystemResource>` を返し、`Content-Type`（必要に応じ `Content-Disposition`）をヘッダーに設定する。詳細・一時ファイル削除は [15](springer-file-prevention.md)。

## 条件付き事項

### R-05-C1 @SessionAttributes 使用時は @ModelAttribute で名前を必ず指定する
`@SessionAttributes` でセッション格納する場合、`@ModelAttribute` で必ず名前を指定する（名前未指定だと該当セッションを使わないハンドラーでも処理が動くため）。取得は `@ModelAttribute(value="名", binding=false)`、削除は `SessionStatus.setComplete()`。

### R-05-C2 HTTP メソッドのマッピングアノテーション
US 画面系は `@GetMapping`/`@PostMapping` の 2 種、BS は `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` の 4 種を使い分ける。

### R-05-C3 変数名 ≠ パラメーター名のときは @RequestParam("名") で属性指定する
メソッド引数名とリクエストパラメーター名が同じなら `@RequestParam` のみ、異なるなら `@RequestParam("パラメーター名")` を指定する。

### R-05-C4 動的に決まる HTTP ステータスが必要な画面は ModelAndView を返す
処理結果でステータスが変わる場合、戻り値を `ModelAndView` にし View 名・Model・`HttpStatus` を設定する。

### R-05-C5 リダイレクトは "redirect:..." を返し、データ受け渡しは RedirectAttributes
リダイレクトは `"redirect:/path"` を返す。データ受け渡しはクエリストリング（`addAttribute`）／パス置換／フラッシュスコープ（`addFlashAttribute`）を使い分ける。

### R-05-C6 REST のレスポンス返却（BS）4 パターン
(1) ボディのみ＝対象クラス返却（既定 200）、(2) ボディなし＝`void`（慣例で `@ResponseStatus(NO_CONTENT)` の 204）、(3) 自由設定＝`ResponseEntity<?>`、(4) 固定で 200 以外＝`@ResponseStatus`。

### R-05-C7 @Controller でも Ajax レスポンスは @ResponseBody で JSON を返す
`@Controller` の画面系 Controller でも、ハンドラーに `@ResponseBody` を付ければ JSON（Ajax レスポンス）を返せる（基本構造は BS-Controller を参照）。

## 参考・推奨実装パターン

- Controller の例外ハンドリング（原則 `@ExceptionHandler`、`SystemException` は不可でグローバル委譲、入力復元が必要なら try-catch）は [springer-exception.md](springer-exception.md) を参照。
