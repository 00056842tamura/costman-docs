---
name: reacter-unit-test-gen
description: reacter-testing-vitest.mdの規約に基づきコンポーネント/カスタムフックのVitestユニットテストを生成する（工程8 PT・frontend×機能ID）
---

# Reacter ユニットテスト生成スキル（工程8 PT）

指定された機能のコンポーネント・カスタムフックの Vitest 単体テストを生成し、加えて **テストdocs を直接生成**する。Springer側の `/springer-unit-test-gen` に対応するReacter版だが、Reacterには Controller/Service/Repository のようなレイヤー分離が無いため単一スキルで完結する。

> PT issue は PT-Plan で先行起票済み。`phase-{N}` 概念は廃止（frontend×機能ID）。
> **スコープ**: Vitest + Testing Library によるコンポーネント/カスタムフックのユニットテストのみ。E2Eテスト（Playwright）は本スキルのスコープ外（生成スキルなし）。

## プロダクト作業時の注意

- 対象は `frontend/` のみ（スタックリポ）
- テスト出力先は `frontend/src/test/features/{feature}/`（機能単位）または `frontend/src/test/shared/`（横断ロジック）。`frontend/CLAUDE.md`・`.claude/rules/reacter-folder-structure.md` [STR-36] のディレクトリ構成規約に従い、`src/test/` 配下に対象ソースと同じディレクトリ構造をミラーリングして配置する（Springerの `src/test/java/` が `src/main/java/` をミラーする構成と揃える）
- `_hint/` はプロダクトルート直下を参照します（`ItemListPage.test.tsx`・`useItem.test.ts`）
- **入力**（常時読み込み・プロンプト貼り付けも可）:
  1. PT-Plan の `plan.md`（テスト対象・観点）
  2. `docs/base-design/テストシナリオ.md`（受け入れ条件・「コンポーネントテスト観点」セクション。UI工程で作られる抽象的な骨格）
  3. **SS詳細設計**（**期待値の正本**）: WB `frontend/specs/{案件キー}/detail-design/{issue_id(SS)}_{機能ID}/frontend.md` ＋ `frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md`
  4. 実装コード `frontend/src/features/{feature}/`
- **テストシナリオ.md と SS詳細設計の役割分担**: テストシナリオ.mdは「どの観点でテストすべきか」の骨格、コンポーネント仕様書は「その観点の期待値（表示文言・Props/State・バリデーションルール）」の正本。テストシナリオの観点に対応する具体的な文言・数値がコンポーネント仕様書に見つからない場合は黙って推測せず `⚠️ TODO: 要確認` として報告する
- **テストdocs**（テスト仕様書_{機能ID}・テスト計画書・機能要件対比表）を `frontend/docs/unit-test/` に**直接生成**する（`frontend/docs/test/` は旧パス・使用しない）

## 入力形式

```
/reacter-unit-test-gen
機能名: Item（例）
対象: frontend/src/features/item/

Component: ItemListPage
  ### 表示要素検証
  - 一覧表示成功（3件取得）: 引数なし → タイトル・3行のアイテム名が表示される
  - 0件表示: 検索結果0件 → 「該当するデータがありません」が表示される
  ### Props/State変化検証
  - 検索フォーム入力時にstateが更新される
  ### ユーザ操作検証
  - 「検索」ボタンクリックで検索実行
  - 「登録」ボタンクリックで登録画面へ遷移
  ### 入力バリデーション
  - name未入力で送信 → 「名称は必須です」エラー表示

Hook: useItem
  ### 取得値の検証
  - 正常時: items配列を返す
  - エラー時: error状態を返す
  ### API通信（MSW）
  - GET /items 200 → 3件取得
  - GET /items 500 → エラー状態
```

## 実行手順

### Step 0: 入力チェック

- [ ] `frontend/specs/{案件キー}/detail-design/{issue_id(SS)}_{機能ID}/frontend.md`（WB）が存在するか確認
- [ ] `frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md` が存在するか確認
- [ ] `docs/base-design/テストシナリオ.md` の「コンポーネントテスト観点」セクションが存在するか確認

### Step 1: 対象ファイルの確認と期待値の確定

$ARGUMENTS から対象コンポーネント・カスタムフックのファイルパスとテストケースを取得する。各ファイルを `frontend/src/` 以下で検索し内容を確認する。

続けて、対象機能IDのコンポーネント仕様書を読み込み、以下の観点で期待値を確定する。

| テスト種別 | 期待値の確定元 | 反映方法 |
|---|---|---|
| 表示要素検証 | コンポーネント仕様書「コンポーネント仕様」 | `getByText`/`getByRole` でテキスト・ボタン名を検証する |
| Props/State変化検証 | コンポーネント仕様書「コンポーネント仕様」のProps/State列 | Props変更時の再レンダリング結果・State変化を検証する |
| ユーザ操作検証 | コンポーネント仕様書「画面遷移」＋テストシナリオ.md | `fireEvent.click` 等の操作後の結果を検証する |
| 入力バリデーション（境界値相当） | コンポーネント仕様書「入力バリデーション（Zodスキーマ）」 | 無効値入力時に該当エラーメッセージが表示されることを検証する |
| API通信 | コンポーネント仕様書「API接続仕様」のエラー処理列 | MSWでHTTPステータスをモックしエラー分岐を検証する |

### Step 2: テスト観点の判定

`.claude/rules/reacter-testing-vitest.md` の観点に従い、対象ごとに以下を判定する。

- コンポーネント: 表示要素検証／Props・State変化検証／ユーザ操作検証（3観点）
- カスタムフック: `renderHook` で個別テストする（複雑なロジック・共通利用フックの場合は特に必須）
- API通信を伴う場合: MSW の `TEST_HANDLERS` でエンドポイント別にモック定義する

### Step 3: 生成（ファイル数に応じて直接／並列）

生成するテストファイル数に応じてサブエージェントを使うか判断する（Springerの固定3並列はレイヤー数由来のため、レイヤー概念のないReacterには適用しない）。

| ファイル数 | 方針 |
|---|---|
| 8以下 | サブエージェントを使わずメイン会話で直接Writeする |
| 9〜16 | 最大2並列。1エージェントあたり最低4ファイルを担当させる |
| 17以上 | 最大3並列。担当ファイルが均等になるよう分割する |

並列化する場合、各エージェントへのプロンプトには以下を必ず含める:
- 対象コンポーネント/フックのファイルパスとテストケース（Step1で確定した期待値を含む）
- `_hint/ItemListPage.test.tsx`（コンポーネント見本）または `_hint/useItem.test.ts`（フック見本）の体裁（インデント・記述順）に合わせること
- `.claude/rules/reacter-testing-vitest.md` のCSS Modulesセレクター規約（クラス名はハッシュ化されるため正規表現で記述する）
- テストファイルは `src/test/` 配下に対象ファイルと同じディレクトリ構造をミラーリングして配置し（例: `src/features/item/components/ItemListPage.tsx` → `src/test/features/item/components/ItemListPage.test.tsx`）、命名は `[対象ファイル名].test.tsx(ts)`
- `src/` 配下の実装ファイルは編集しないこと
- `React.FC`/`any` 型/`dangerouslySetInnerHTML` を含むコードを生成しないこと

### Step 4: 完了前チェック（報告前に必ず実施）

以下をすべて確認してから完了報告を行う。1件でも漏れがあれば生成を追加してから報告する。

- [ ] Step1で列挙した全テストケース（表示要素・Props/State・ユーザ操作・バリデーション・API通信）に対応する `it()` が存在する
- [ ] カスタムフックがある場合は `renderHook` によるテストが存在する
- [ ] API通信を伴うテストは MSW の `TEST_HANDLERS` が設定されている

### Step 5: テストdocsの直接生成

テストコード・テストケースから、テストdocs を **直接生成**する（`docs/templates/70_テスト/` をベースに）:
- **`テスト仕様書_{機能ID}.md`**（PSK70・per-機能）: 生成したテストケース（表示要素/Props・State/ユーザ操作/バリデーション/API通信・受け入れ条件番号）を仕様書化。**テンプレ: `docs/templates/70_テスト/プログラムテスト仕様書.md`**（Springer側の`/springer-unit-test-gen`と同一テンプレを使用。「対象プログラム」欄はコンポーネント名/カスタムフック名を記入する）。`docs/templates/70_テスト/テスト仕様書.md`は使用しない（PT工程の単体テストではなく、結合テスト・システムテスト・運用テスト向けのシナリオベーステンプレートのため対象読者が異なる。現時点でこれらの工程を担う生成スキルは存在しない）。
  - **本テンプレはPT工程独自の内容（製造規模・テスト密度・検証履歴・テストケース一覧）のみを持つ**。個別の規約準拠チェックは工程6 PGの`/reacter-code-review`（観点台帳 COM/SEC/CSS/FRM/STR/QLT）で既に確認済みのため、PTで重複して確認しない。
- **`テスト計画書.md`**（初回・スタック共通）: PT-Plan で繰り延べた分をここで生成（目的・範囲・方法・開始/完了基準）。既存があればスキップ。テンプレ: `docs/templates/70_テスト/テスト計画書.md`。
- **`機能要件対比表.md`**（スタック共通・累積）: **採番 `{BR}-{SR}-{FR}-{TC}`**（例 `A-01-01-1`）で機能要件 ↔ テストケースのトレースを記す。今回機能分を差分追記。テンプレ: `docs/templates/70_テスト/機能要件対比表.md`。

`frontend/docs/unit-test/` が無ければ `mkdir -p`。不明値は `⚠️ TODO: 要確認`。

> ⚠️ **妥当性確認実施票は本 PT 工程の責務ではない**（テスト成果物ではなくリリース判定ゲート向けの承認書類）。PT 完了後、人間がリリース前ゲートまでの任意のタイミングで作成する（生成スキルなし）。

### Step 6: 三者整合・テスト実行・人間確認（誘導）
```
🎯 次のアクション:
   1. /consistency-check（設計書/コード/テストの三者整合）
   2. npm test を実行し、結果を /work-log と PT issue に記録
   3. 人間が全件合格を確認
   4. docs-to-pr（PT・スタックリポ・テストコード＋テストdocs を PR）
```

## 制約
- 🚫 テストシナリオ.md・コンポーネント仕様書に記載のないテストケースを勝手に追加しない
- 🚫 `React.FC`/`any` 型/`dangerouslySetInnerHTML` を含むコードを生成しない
- ✅ CSS Modulesのクラス名はハッシュ化されるためセレクターは正規表現で記述する（`reacter-testing-vitest.md` [QLT-14]）
- ✅ 複雑なロジックを持つカスタムフック・共通利用フックは `renderHook` で個別テストする（`reacter-testing-vitest.md` [QLT-15]）
- ✅ Step4の完了前チェックを満たさない限り完了報告してはならない

## 関連スキル・参照先
- 前工程: `/reacter-code-gen`（実装コード生成）
- 規約: `.claude/rules/reacter-testing-vitest.md`
- 見本: `_hint/ItemListPage.test.tsx`・`_hint/useItem.test.ts`
- 次工程: `/consistency-check`（三者整合）→ `docs-to-pr`

## 作業指示
$ARGUMENTS
