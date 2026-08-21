---
name: springer-scaffold
description: 詳細設計WB（specs）と形式設計書（プログラム仕様書・メッセージ一覧含む）を入力に Springer 全レイヤー実装コードを {スタック}/src/main に生成する（工程6 PG・スタック×機能ID・docs生成なし）
---

# Springer 新機能全レイヤー実装一括生成（プロダクト版）

以下の手順で、指定された機能の全レイヤー（model・Controller・Service・Repository + Mapper XML）のスケルトンを生成してください。

## 事前確認 — スケルトン作成済みかの確認

**Phase 1 に進む前に、必ず以下を確認すること。**

対象機能のスケルトン（Controller・Service・Repository 等のクラスファイル）が既に作成済みかどうかをユーザーに確認する。

### スケルトン未作成の場合
そのまま Phase 1 へ進む。

### スケルトン作成済みの場合
以下のリスクを説明し、`/springer-bs` の実行を促す。処理を続行しない。

```
⚠️ スケルトンが既に作成済みのため、/springer-scaffold の実行はお勧めしません。

理由:
/springer-scaffold は全レイヤーのファイルを新規生成するため、
既存のスケルトンコードを上書きし、作成済みの実装が失われるリスクがあります。

推奨アクション:
既存スケルトンに対して実装を追加・補完するには /springer-bs を使用してください。
/springer-bs は既存ファイルを前提に差分実装ができるため、上書きリスクがありません。
```

---

## プロダクト作業時の注意

- アプリ種別に応じて以下のパッケージに生成します:
  - BS: `bs/src/main/java/<ベースパッケージ>/...`
  - US-MPA: `us-mpa/src/main/java/<ベースパッケージ>/...`
  - US-API: `us-api/src/main/java/<ベースパッケージ>/...`
- ベースパッケージは対象パッケージの `CLAUDE.md` 冒頭の宣言に従ってください
- 生成前にパッケージ間依存ルール（`.claude/rules/product-rules.md`）を確認してください

## 入力形式

```
/springer-scaffold
案件キー: inventory-2026-001
機能ID: bs-001
スタック: bs            # bs / us-api / us-mpa
PG issue-id: {issue_id}
実装モード: フル実装（デフォルト）  # 「スケルトン」でメソッドボディをコメントのみにする
```

> **エンティティ仕様・API 仕様はプロンプト貼り付けではなく、詳細設計成果物から読み込む**（工程6 PG）:
> - **詳細設計WB**: `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/{controller,service,repository,batch}.md`（同じスタックリポ内で SS 工程の PR がマージ済みのため直接読める）
> - **形式設計書**: `docs/base-design/テーブル一覧.md`＋対象テーブルの `テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（UI工程が正本・テーブル単位）・`{スタック}/docs/detail-design/外部IF定義書.md`
> - **実装計画**: PG-Plan の `plan.md`（実装クラス・**実装順序**）／ベースパッケージ: スタック `CLAUDE.md`
> 生成単位は **スタック×機能ID**（旧 `Phase番号` は廃止）。コードは `{スタック}/src/main/` へ直接。

## 実行手順

### Phase 0: 詳細設計WB・形式設計書の読込（17§3-1 回収）

コード生成に先立ち、対象 `スタック×機能ID` の詳細設計を読み込み、エンティティ仕様・API 仕様・SQL を確定する:
- `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/{controller,service,repository,batch}.md`（WB・同じスタックリポ内で SS 工程の PR がマージ済みのため直接読める）
- `docs/base-design/テーブル一覧.md`＋対象テーブルの `テーブル定義書_{テーブルID}_{テーブル名（論理）}.md`（UI工程が正本・テーブル単位・カラム・型・制約・インデックス）・`{スタック}/docs/detail-design/外部IF定義書.md`
- **`{スタック}/docs/detail-design/プログラム仕様書_{機能ID}.md`**（SS 工程で生成済み・クラス設計仕様の主要入力）
- **`{スタック}/docs/detail-design/メッセージ一覧.md`**（SS 工程で生成済み・実装時のメッセージID参照用）
- PG-Plan `plan.md`（実装クラス一覧・実装順序）／スタック `CLAUDE.md`（ベースパッケージ・Springer バージョン）

> プロンプトにエンティティ/API を貼り付けない。**WB・形式設計書が単一の入力源**。

### Phase 1: model クラスの生成（先に完了させること）

以下の model クラスを `{ベースパッケージ}.model` パッケージに生成してください：

- **エンティティクラス**（例: `Item.java`）
  - `$ARGUMENTS` のエンティティ仕様に基づくフィールド定義
  - コンストラクターインジェクションではなく通常の getter/setter 構成
  - 機密情報フィールドには `@Sensitive` を付与
  - `toString()` は `MaskingToStringBuilder.toString(this)` を使用
  - 全フィールドに Javadoc

- **検索条件クラス**（例: `ItemSearchCondition.java`）
  - 検索パラメータをフィールドとして定義

Phase 1 完了後、生成した model クラスのパスを確認してから Phase 2 に進んでください。

### Phase 2: 3 エージェントを並列起動

Phase 1 の model クラスを前提として、以下の 3 つの Agent を **同時に** 起動してください。

---

**Agent 1 — Controller スケルトン生成**

Springer プログラミング規約に従い、次の仕様で Controller クラスのスケルトンを生成すること。

共通ルール:
- CLAUDE.md の全ルールを適用すること
- アプリ種別が BS の場合は `@RestController`、US-MPA は `@Controller`、US-API は `@RestController`
- リクエストは `{ベースパッケージ}.controller.request/form/` パッケージのクラスで受け取ること
- レスポンスは `{ベースパッケージ}.controller.view/` パッケージのクラスで返すこと
- バリデーションは `@Validated` + Springer バリデーターアノテーションを使用すること
- `BindingResult` は `@Validated` 引数の直後に配置すること
- コンストラクターインジェクションのみ使用し、メンバー変数は `final` にすること
- `ConflictException` は `@ExceptionHandler` でハンドリングし `RestErrorInfo` で返すこと
- 全クラス・メソッド・フィールドに Javadoc を記述すること

ハンドラーメソッドのボディを以下のとおり完全に実装すること（実装モードが「スケルトン」の場合はメソッドボディをコメントのみにする）:
- Service メソッドを呼び出し、結果を View クラスにマッピングして返すこと
- バリデーションエラーがある場合は適切なエラーレスポンスを返すこと
- 正常時の HTTP ステータスコードを API 仕様どおりに設定すること

生成物:
- `ItemController.java`（Controller 本体）
- 必要な Request/Form/View クラス（API 仕様に基づく）

対象機能・API 仕様: [$ARGUMENTS の API 仕様セクション]
model クラスパス: [Phase 1 で生成したパス]

---

**Agent 2 — Service スケルトン生成**

Springer プログラミング規約に従い、次の仕様で Service インターフェイスと実装クラスのスケルトンを生成すること。

共通ルール:
- CLAUDE.md の全ルールを適用すること
- インターフェイス名: `ItemService`、実装クラス名: `ItemServiceImpl`
- 実装クラスには `@Service` を付与すること
- BS アプリの場合は `@Transactional` をクラスレベルに付与すること
- US アプリの場合は `@Transactional` を付与しないこと
- インターフェイスのメソッドシグネチャに `throws` 節を記述しないこと（Javadoc `@throws` に記載）
- Controller の Form/Request クラスを引数に使用しないこと（model クラスを使用）
- コンストラクターインジェクションのみ使用し、メンバー変数は `final` にすること
- 全メソッドに Javadoc を記述すること（`@param`・`@return`・`@throws` タグ含む）

Service メソッドのボディを以下のとおり完全に実装すること（実装モードが「スケルトン」の場合はメソッドボディをコメントのみにする）:
- Repository を呼び出してデータを取得・更新すること
- Controller に返す前に model クラスへのデータマッピングを行うこと
- API 仕様に基づくビジネスロジック（存在チェック・重複チェック等）を実装すること

生成物:
- `ItemService.java`（インターフェイス）
- `impl/ItemServiceImpl.java`（実装クラス）

対象機能・API 仕様: [$ARGUMENTS の API 仕様セクション]
model クラスパス: [Phase 1 で生成したパス]

---

**Agent 3 — Repository・Mapper スケルトン生成**

Springer プログラミング規約に従い、次の仕様で Repository インターフェイス・実装クラス・Mapper インターフェイス・Mapper XML のスケルトンを生成すること。

共通ルール:
- CLAUDE.md の全ルールを適用すること
- Repository インターフェイス名: `ItemRepository`、実装クラス名: `ItemRepositoryImpl`
- 実装クラスには `@Repository` を付与すること
- BS アプリの場合: DB アクセスは MyBatis Mapper 経由のみ（SQL アノテーション禁止）
- BS アプリの場合: Mapper XML は `src/main/resources/` + 同じパッケージパスに配置
- US アプリの場合: RestClient / RestTemplate で BS API を呼び出すこと（DB 直アクセス禁止）
- コンストラクターインジェクションのみ使用し、メンバー変数は `final` にすること
- SELECT 1件の場合は `null` チェック → `ResourceNotFoundException` スローを実装すること
- UPDATE/DELETE の場合は更新件数チェック → `ConflictException` スローを実装すること
- INSERT の場合は `DBDuplicateKeyException` → `ConflictException` ラップを実装すること
- 全メソッドに Javadoc を記述すること

生成物（BS アプリの場合）:
- `ItemRepository.java`（インターフェイス）
- `impl/ItemRepositoryImpl.java`（実装クラス）
- `mapper/ItemMapper.java`（Mapper インターフェイス）
- `mapper/ItemMapper.xml`（Mapper XML、API 仕様に基づく SQL を実装すること。テーブル・カラム名が不明な場合は TODO コメントで記載）

生成物（US アプリの場合）:
- `ItemRepository.java`（インターフェイス）
- `impl/ItemRepositoryImpl.java`（RestClient/RestTemplate を使用した実装）

対象機能・API 仕様: [$ARGUMENTS の API 仕様セクション]
model クラスパス: [Phase 1 で生成したパス]

---

### Phase 3: 結果確認

生成したすべてのファイルを確認し、以下をチェックしてください：

1. **パッケージ整合性**: 全クラスのパッケージ宣言が正しいか
2. **型整合性**: Service が受け取る型と Controller が渡す型が一致しているか
3. **命名整合性**: Controller の `service.getOne(id)` 呼び出しと Service の `getOne(String id)` シグネチャが一致しているか
4. **Mapper 整合性**: Repository が呼ぶ Mapper メソッド名と XML の `id` が一致しているか

不整合があれば修正してください。

### Phase 4: 完了報告
```
✅ 実装を生成しました（工程6 PG・{スタック}×{機能ID}）。
📁 コード: {スタック}/src/main/（PR 対象）
📋 docs 生成なし（プログラム仕様書・メッセージ一覧はSS工程で {スタック}/docs/detail-design/ に生成済み）
📁 検討メモ: {スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/（push・PR 対象）
🎯 次のアクション: /springer-review（レポートは {スタック}/specs・push・PR 対象）→ docs-to-pr（PG・スタックリポ）
```

## 関連
- 前工程: PG-Plan（`/issue-plan 工程: PG-Plan`）→ `/issue-init 工程: PG`（先行起票分の着手）
- 既存スケルトンへの差分実装: `/springer-bs`・`/springer-us-api`・`/springer-us-mpa`
- レビュー: `/springer-review`（＋`/springer-review-fix`）
- 次: `docs-to-pr`（PG・スタックリポ・コード＋実装docs を PR）→ 工程7/8 UT
