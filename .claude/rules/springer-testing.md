---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 20 テスト

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-20-01 E2E の共通処理ファイルに Playwright 操作/検証を定義しない
Playwright API を用いた検証・画面操作は共通処理ファイル（`Common.ts`）に定義しない（シナリオから画面操作が見えなくなり見通しが悪くなる）。

### R-20-02 ダイアログ表示操作に await を付けない
ダイアログを表示する操作（alert/confirm 等）には `await` を付与しない（承認/キャンセルまで待機するため `accept`/`dismiss` に到達できずタイムアウトする）。一方 Dialog の `accept`/`dismiss` のいずれかは必ず呼ぶ。

## ⚠ 非推奨 (AVOID)

### R-20-A1 @Sql スクリプトはロールバックされない点に注意する
`@Sql` で指定した SQL はテスト実行前にコミットされロールバックされない。必要に応じて接続先 DB のバックアップ取得・リストアを行う。

## ✅ 必須 (ALWAYS)

### ユニットテスト — 単位・構成
- **R-20-03** PL/BL/DL 各層の全クラスおよび共通処理クラスをユニットテスト対象とする。
- **R-20-04** テスト対象 1 クラスにつき基本 1 テストクラスを作成し、クラス名は `[対象クラス名]Test` とする。テスト対象と同一パッケージ・`/src/test/java/` 配下に配置する。
- **R-20-05** テストメソッドの作成単位は、Controller＝リクエストハンドラーメソッド 1 つ、Service/Repository＝インターフェイス定義メソッド単位、Utility＝外部公開メソッド単位とする。
- **R-20-06** `@DisplayName` には「テスト対象メソッド名」「正常系/異常系の区分」「ケース内容」を必ず記載する。
- **R-20-07** バリデーション検証は利用アノテーションが異なるため、Controller テストとは別のテストクラス（識別子付き、例 `〜ValidationTest`）に分ける。
- **R-20-08** テスト用 Bean 生成の `@TestConfiguration` クラスは、テストクラス内に `static` 内部クラスとして定義する。
- **R-20-09** テストクラス内で生成したファイルは必ず削除処理を実装する。

### ユニットテスト — 検証観点（最低限）
- **R-20-10** 各クラスの掲載検証観点は「最低限実施すべき内容」とし、仕様により別途観点がある場合は漏れなく検証する。
  - Controller: マッピング定義 / 処理内容（Service 呼出制御・引数・Session 保存）/ レスポンス内容（ステータス・ヘッダ・View 名・リダイレクト・Body・モデル属性）/ 例外ハンドリング。
  - Service: 処理内容（戻り値・Repository/別 Service 呼出と引数）/ 例外ハンドリング。
  - Repository: API 呼び出し（ホスト・パス・メソッド・パラメータ）/ 処理内容（戻り値・DB レコード状態）/ 例外ハンドリング。
  - Utility: 処理内容（戻り値）/ 例外ハンドリング。

### ユニットテスト — レイヤー別構成
- **R-20-11** Controller テストは `@SpringBootTest`＋`@AutoConfigureMockMvc`、`MockMvc` で擬似実行する。依存 Service は `@MockitoBean` でモック化し、`@BeforeEach` でモック状態をリセットする。
- **R-20-12** Service テストは依存 Repository を `Mockito.mock` でモック化し、テスト対象はコンストラクタ呼び出しで生成する（Spring コンテキスト不要）。
- **R-20-13** Spring Security 有効時の MockMvc での POST/PUT/DELETE は `.with(csrf())` で CSRF トークンを付与する（付与しないと 403）。

### E2E（Playwright）
- **R-20-14** シナリオはアプリの各層（US/BS/DS）をすべて横断するよう作成する。すべての外部システムにアクセスするシナリオを最低 1 つ作成する。
- **R-20-15** テスト結果は遷移先画面の妥当性・表示崩れ・実行前後の DB 差分・外部システム連携結果を確認する。
- **R-20-16** テストファイルの拡張子は `.spec.ts` とする。`playwright.config.ts` を所定内容（`fullyParallel:false`・`workers:1`・`reporter:"html"` 等）で設定する。
- **R-20-17** セレクターは画面単位の定数として定義する（命名規則をプロジェクトで定める）。Locator 取得はセレクター文字列指定で行う（`getByRole`/`getByLabel` 等は使わない）。
- **R-20-18** Playwright の非同期 API には `await` を付与する（ダイアログ表示操作を除く＝R-20-02）。
- **R-20-19** スクリーンショット期待値は生成・更新後、初回テスト実行前に必ず目視確認する。

## ✨ 推奨 (PREFER)

- **R-20-20** テストメソッド名は `test+[対象メソッド名(先頭大文字)]+連番` で命名する。
- **R-20-21** `test.step` のネストは 3 階層程度までを目安とする。step 名は操作内容がわかる名称にする。
- **R-20-22** スナップショット初回実行は `--update-snapshots` を付けて期待値を生成する。

## 条件付き事項

### R-20-C1 バリデーション（JSON 返却）のテスト構成
JSON を返すハンドラのバリデーション検証は `@SpringBootTest(webEnvironment=RANDOM_PORT)`＋`@AutoConfigureRestTestClient`、`RestTestClient`＋`@LocalServerPort` で実 HTTP 通信テストする。

### R-20-C2 バリデーション（事前認証が必要）のテスト構成
認証が必要なハンドラは `@TestConfiguration` の `TestSecurityConfig`（`@Order(HIGHEST_PRECEDENCE)` の `SecurityFilterChain` で対象パスを `permitAll`・`STATELESS`・`csrf().disable()`）を static 内部クラスで定義する。

### R-20-C3 Repository テストのアノテーション
- API アクセス: `@SpringBootTest(properties="springer.rest.rest-client-bean-post-processor.enabled=false")`＋`@AutoConfigureMockRestServiceServer`、`MockRestServiceServer` でモック化し `@BeforeEach` で `reset()`。
- DB アクセス: `@SpringBootTest`＋`@Transactional`＋`@Sql`（初期データ投入）。`@Transactional` でメソッド終了時にロールバックされる。

### R-20-C4 JSON 比較モードの選択
緩い検証は `JsonCompareMode.LENIENT`、厳密検証は `JsonCompareMode.STRICT`。レスポンス Body（JSON 配列）の検証は STRICT とする。

### R-20-C5 E2E の直列実行制御
データ更新シナリオの同時実行やシナリオ間に先行関係がある場合は `workers:1`・`fullyParallel:false` で直列実行に制御する。実行順序はフォルダ名含むファイル名の昇順。

## 参考・推奨実装パターン

- パラメータ化テスト（`@ParameterizedTest`＋`@ValueSource`/`@CsvSource`/`@MethodSource`/`@NullSource` 等）。
- Controller/Service/Repository/Utility の MockMvc・Mockito・MockRestServiceServer・JdbcTemplate 検証パターンは `06/05.サンプル.md` を参照。
- E2E シナリオ基本形（`test`→`test.step`→`page.locator(...).fill/click`→`expect(...)`）、API リファレンス（操作/検証/`waitForResponse`/`waitForEvent("download")`）は `07/03・05` を参照。
