---
name: blackbox-test-gen
description: テストシナリオ.mdのAPIテスト観点をもとにControllerのブラックボックスAPIテストを生成する（工程8 UT）
---

# ブラックボックステスト生成スキル（工程8 UT）

`docs/base-design/テストシナリオ.md` の「APIテスト / コンポーネントテスト観点」をもとに、
`@WebMvcTest` + MockMvc を使ったController APIテストクラスを生成する。

> **スコープ:** `@WebMvcTest` によるController層のAPIテスト（ServiceはMock）。
> `@SpringBootTest` による全レイヤー統合テストはスコープ外。

## 入出力（工程8 UT）
- 入力: `docs/base-design/テストシナリオ.md`（受け入れ条件カバレッジ・push 済み正本）
- 出力（テストコード）: `{スタック}/src/test/`（スタックリポ・直接）。作業は `feature/{案件キー}-ut-{機能ID}`

## 入力形式

```
/blackbox-test-gen
案件キー: {案件キー}
機能ID: {カテゴリ}-{3桁連番}
対象スタック: {bs / us-api 等}
対象Controller: {ControllerクラスFQCN}
```

---

## 実行手順

### Step 1: 入力チェック

- [ ] `docs/base-design/テストシナリオ.md` が存在するか確認
- [ ] `{スタック}/docs/detail-design/` の Web_API_IF定義書（SS の Controller 設計・WB）が存在するか確認
- [ ] 対象Controllerクラスが `{スタック}/src/main/java/` 以下に存在するか確認

### Step 2: テストケース抽出

以下を読み込みテストケースを導出する:

- `docs/base-design/テストシナリオ.md` のAPIテスト / コンポーネントテスト観点全セクション
  - テスト前提条件（DBセットアップ）
  - 正常系（API-正常-N）
  - 異常系（API-異常-N）
  - 境界値（BV-N）
  - 認証・認可テスト（AUTH-N）
- `{スタック}/docs/detail-design/` の Controller 設計（Web_API_IF定義書）の出力仕様・例外・エラー仕様

### Step 3: テストクラスの生成

`_hint/ItemControllerTest.java` のスタイルに従って
`{Controllerクラス名}BlackboxTest.java` を生成する。

生成先: `{スタック}/src/test/java/{パッケージパス}/controller/`

**生成するテストの構成:**
```java
@WebMvcTest({Controllerクラス名}.class)
class {Controllerクラス名}BlackboxTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private {ServiceインターフェースFQCN} {service変数名};

    // 正常系: テストシナリオ.md の API-正常-N に対応
    @Test
    void {メソッド名}_正常系_テスト名() throws Exception { ... }

    // 異常系・境界値・認証テストも同様に生成
}
```

### Step 4: 完了報告

```
✅ {Controllerクラス名}BlackboxTest.java を生成しました（Step 8 ブラックボックステスト）。

📋 生成したテスト:
   - 正常系: {N}件（API-正常-* に対応）
   - 異常系: {N}件（API-異常-* に対応）
   - 境界値: {N}件（BV-* に対応）
   - 認証テスト: {N}件（AUTH-* に対応）

🎯 次のアクション（UT工程内・三者整合）:
   /consistency-check（案件キー: {案件キー} 機能ID: {カテゴリ}-{3桁連番} 対象スタック: ...）
```

---

## 制約

- ✅ テストシナリオ.md の全テストケース番号に対応するテストを生成する
- 🚫 テストシナリオ.md に記載のないテストケースを勝手に追加しない
- ✅ 認証テストは `@WithMockUser` / `@WithAnonymousUser` を使用する
- ✅ テストメソッド名は `{メソッド名}_{条件}_{期待結果}` の命名規則に従う
- 🚫 `@SpringBootTest` を使わない

## 関連Step・スキル

- 前工程: 工程6 PG（実装・`/springer-review` 完了後）
- 並行実行: `/springer-unit-test-gen`（ホワイトボックス単体テスト）
- 次: `/consistency-check`（UT工程内・三者整合）

---

## 作業指示

$ARGUMENTS
