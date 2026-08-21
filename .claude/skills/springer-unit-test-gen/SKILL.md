---
name: springer-unit-test-gen
description: 全レイヤーユニットテストを {スタック}/src/test に一括生成し、テスト仕様書/テスト計画書/機能要件対比表を {スタック}/docs/unit-test/ に直接生成する（工程8 PT・スタック×機能ID）
---

# 全レイヤーユニットテスト一括生成（工程8 PT・プロダクト版）

指定された機能の Controller・Service・Repository テストを **並列で** 生成し、加えて **テストdocs を直接生成**する。

> PT issue は PT-Plan で先行起票済み。`phase-{N}` 概念は廃止（スタック×機能ID）。

## プロダクト作業時の注意

- 対象スタックは `bs/` `us-mpa/` `us-api/` `batch/` のいずれかです（スタックリポ）。`batch` は Controller レイヤーが無い構成のため、Step2 では Service・Repository の2サブエージェントのみ起動する
- テスト出力先は `{スタック}/src/test/java/...`、Repository テスト SQL は `{スタック}/src/test/resources/sql/...`
- `_hint/` はプロダクトルート直下を参照します
- **入力**（常時読み込み・プロンプト貼り付けも可）:
  1. PT-Plan の `plan.md`（テスト対象・観点）
  2. `docs/base-design/テストシナリオ.md`（受け入れ条件・**「どのテストケースが存在すべきか」の網羅性の基準**）
  3. **SS詳細設計**（**「テストケースの期待値を何で確定するか」の正本**）: WB `{スタック}/specs/{案件キー}/detail-design/{issue_id(SS)}_{機能ID}/{controller,service,repository}.md` ＋ 形式設計書 `{スタック}/docs/detail-design/プログラム仕様書_{機能ID}.md`・`メッセージ一覧.md`・（該当時）`外部IF定義書.md`
  4. 実装コード `{スタック}/src/main`
- **テストシナリオ.md と SS詳細設計の役割分担**: テストシナリオ.mdはUI工程（基本設計）で作られる抽象的な骨格（正常系・異常系・境界値の「観点」）であり、具体的な数値・エラーコード・メッセージCDまでは確定していない。SS詳細設計はそれらを確定させた正本。テストシナリオの境界値行（BV-N）に対応する具体的数値がSS詳細設計に見つからない場合は黙って推測せず `⚠️ TODO: 要確認` として報告する
- **テストdocs**（テスト仕様書_{機能ID}・テスト計画書・機能要件対比表）を `{スタック}/docs/unit-test/` に**直接生成**する（`documentation` 廃止・直接編集モデル）

## 入力形式

```
/springer-unit-test-gen
機能名: Item（例）

Controller: ItemController
  ### getList
  - 処理成功（3件取得）: 引数 id=null, name=null
  - 処理成功（0件）: 引数 id="M0001", name="テスト食品"
  ### getOne
  - 処理成功: 引数 "M0001"
  - 該当なし（404）: 引数 "X9999"

Service: ItemServiceImpl
  ### getList
  - 処理成功: 引数 name="メーカー"、戻り値3件
  ### getOne
  - 処理成功: 引数 "M0001"

Repository: ItemRepositoryImpl
  テーブル構造: src/test/resources/sql/create/T_ITEM.sql
  クリア用SQL: src/test/resources/sql/delete/T_ITEM.sql
  初期データSQL（新規作成）: src/test/resources/sql/data/ItemRepositoryImplTest.sql
  初期データ参考SQL: src/test/resources/sql/data/sample_insert.sql
  ### getList
  - 処理成功（3件）: 引数 id=null, name=null
  ### getOne
  - 処理成功: 引数 "M0001"
  - 該当なし（ResourceNotFoundException）: 引数 "X0001"
```

## 実行手順

### Step 1: 対象ファイルの確認

$ARGUMENTS から Controller・Service・Repository の各クラス名とテストケースを取得してください（`batch` は Controller が存在しないため Service・Repository のみ）。
各クラスを `src/main/java/` 以下で検索して内容を確認してください。

続けて、対象機能IDのSS詳細設計（WB・プログラム仕様書・メッセージ一覧）を読み込み、以下の観点で期待値を確定してください。

| テスト種別 | 期待値の確定元 | 反映方法 |
|---|---|---|
| 正常系 | テストシナリオ.md | そのまま反映する |
| 異常系（単項目バリデーション） | controller.md「単体項目チェック仕様」のエラーコード列＋プログラム仕様書のメッセージCD列 | `assertThrows`／`verify(messageManager)` の引数をこの値で厳密化する |
| 境界値 | service.md「業務ルール」の具体的数値・違反時の例外/メッセージキー | テストシナリオのBV-Nに対応する入力値・期待メッセージをこの数値で確定する |
| メッセージ本文 | メッセージ一覧.md | アサーションのメッセージ本文／メッセージキーの正本とする |

### Step 2: レイヤー別サブエージェントを並列起動（通常3・`batch`は2）

**Agent ツールを 1 つのメッセージで同時に呼び出すこと（並列実行）。**

各エージェント定義ファイルを読み込み、その内容をプロンプトの冒頭に含めた上で対象クラス情報とテストケース、Step1で確定した詳細設計の該当節を付加して Agent ツールを呼び出すこと。

| サブエージェント | 定義ファイル | タスクとして渡す情報 |
|---|---|---|
| Controller テスト生成（`batch`は対象外） | `.claude/agents/controller-test-gen.md` | Controller クラスパス、Controller セクションのテストケース、**詳細設計の該当節**（単体項目チェック仕様・エラーコード・メッセージCD） |
| Service テスト生成 | `.claude/agents/service-test-gen.md` | Service クラスパス、Service セクションのテストケース、**詳細設計の該当節**（業務ルール・境界値・違反時メッセージキー） |
| Repository テスト生成 | `.claude/agents/repository-test-gen.md` | Repository クラスパス、DB 情報（テーブル構造・クリア・初期データ SQL パス）、Repository セクションのテストケース |

---

### Step 3: 結果確認

各エージェントの生成結果を確認し、クラス間の整合性（例: Service テストの mock 設定が Repository の実際のシグネチャと一致しているか）を確認してください。不整合があれば修正してください。

### Step 4: テストdocsの直接生成

テストコード・テストケースから、テストdocs を **直接生成**する（`docs/templates/70_テスト/` をベースに）:
- **`テスト仕様書_{機能ID}.md`**（PSK70・per-機能）: 生成したテストケース（正常/異常/境界・受け入れ条件番号）を仕様書化。**テンプレ: `docs/templates/70_テスト/プログラムテスト仕様書.md`（batchは`プログラムテスト仕様書_バッチ.md`）**。`docs/templates/70_テスト/テスト仕様書.md`は使用しない（PT工程の単体テストではなく、結合テスト・システムテスト・運用テスト向けのシナリオベーステンプレートのため対象読者が異なる。現時点でこれらの工程を担う生成スキルは存在しない）。
  - ファイル名は`テスト仕様書_{機能ID}.md`のまま維持する（テンプレの見出し「プログラムテスト仕様書」は内容の型を示すものであり、ファイル名の命名規約は変更しない）。
  - **本テンプレはPT工程独自の内容（製造規模・テスト密度・検証履歴・テストケース一覧）のみを持つ**。個別の規約準拠チェック（命名規約・Javadoc・DB例外処理等）は工程6 PGの`/springer-review`（観点台帳 CMN/CTL/SVC/REP/X）で既に確認済みのため、PTで重複して確認しない。
- **`テスト計画書.md`**（初回・スタック共通）: PT-Plan で繰り延べた分をここで生成（目的・範囲・方法・開始/完了基準）。既存があればスキップ。テンプレ: `docs/templates/70_テスト/テスト計画書.md`。
- **`機能要件対比表.md`**（スタック共通・累積）: **採番 `{BR}-{SR}-{FR}-{TC}`**（例 `A-01-01-1`）で 機能要件 ↔ テストケースのトレースを記す。今回機能分を差分追記。テンプレ: `docs/templates/70_テスト/機能要件対比表.md`。

`{スタック}/docs/unit-test/` が無ければ `mkdir -p`。不明値は `> ⚠️ TODO: 要確認`。

> ⚠️ **妥当性確認実施票は本 PT 工程の責務ではない**（テスト成果物ではなくリリース判定ゲート向けの承認書類）。PT 完了後、人間がリリース前ゲートまでの任意のタイミングで作成する（生成スキルなし）。

### Step 5: 三者整合・テスト実行・人間確認（誘導）
```
🎯 次のアクション:
   1. /consistency-check（設計書/コード/テストの三者整合・レポートは issue 添付）
   2. mvn test / npm test を実行し、結果を /work-log と PT issue に記録
   3. 人間が全件合格を確認（旧 Step10-1 の人間ゲート）
   4. docs-to-pr（PT・スタックリポ・テストコード＋テストdocs を PR）
```
