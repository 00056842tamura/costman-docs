---
name: springer-review
description: Springer 規約 全レイヤー横断レビュー
---

# Springer 規約 全レイヤー横断レビュー（プロダクト版）

以下の手順で、指定された機能の全レイヤーを **並列で** Springer 規約に照らしてレビューしてください。

## プロダクト作業時の注意

- 対象スタックは `bs/` `us-mpa/` `us-api/` `batch/` のいずれかです
- ルート `CLAUDE.md` ＋ `.claude/rules/springer-*.md` ＋ 対象スタックの `CLAUDE.md` の3層をすべて参照してください
- スタック間の依存違反（US → DB 直接アクセス等）も観点に含めてください

## 入力形式

```
/springer-review
src/main/java/jp/co/example/bs/item/
```

または

```
/springer-review
Controller: src/main/java/jp/co/example/bs/item/controller/ItemController.java
Service:    src/main/java/jp/co/example/bs/item/service/impl/ItemServiceImpl.java
Repository: src/main/java/jp/co/example/bs/item/repository/impl/ItemRepositoryImpl.java
Mapper XML: src/main/resources/jp/co/example/bs/item/repository/mapper/ItemMapper.xml
```

## 実行手順

### Step 1: 対象ファイルの特定

$ARGUMENTS がパッケージパスの場合、以下のファイルを自動的に検索してください：
- `controller/` 以下の `*Controller.java`
- `service/impl/` 以下の `*ServiceImpl.java`
- `repository/impl/` 以下の `*RepositoryImpl.java`
- `repository/mapper/` 以下の `*Mapper.xml`

### Step 2: 3 サブエージェントを並列起動

**Agent ツールを 1 つのメッセージで 3 つ同時に呼び出すこと（並列実行）。**

各エージェント定義ファイルを読み込み、その内容をプロンプトの冒頭に含めた上で対象ファイルパスとアプリ種別を付加して Agent ツールを呼び出すこと。
また、本スキルの「記載ルール（全ファイル共通・重要）」（場所の記載・曖昧表現禁止・件数の単位）も**サブエージェントのプロンプトに必ず含めること**（エージェント定義ファイルにも同内容が組み込まれているが、明示的に再掲することで伝達漏れを防ぐ）。

各エージェントは**観点台帳 `.claude/agents/springer-review-checklist.md` を起点**にレビューし、担当観点すべてに `準拠 / 違反 / 非該当` を付与して「未評価 0 件」を保証する。台帳の各観点の「適用条件」で非該当を判定し、詳細が必要な観点のみ出典ルールを開く（全ルール常時全読込は不要）。

**各レイヤーは観点が多い（Controller 約38 / Service 約30 / Repository 約39）ため、テーマ分割で各 3 並列**に起動する（#3）。したがって本ステップは合計 **9 エージェントを 1 メッセージで並列起動**する。

| サブエージェント | 定義ファイル | タスクとして渡す情報 |
|---|---|---|
| Controller T1/T2/T3 | `.claude/agents/controller-reviewer.md` | Controller パス、アプリ種別、**`theme=T1` / `T2` / `T3`**（3 回） |
| Service T1/T2/T3 | `.claude/agents/service-reviewer.md` | Service パス、アプリ種別、**`theme=T1` / `T2` / `T3`**（3 回） |
| Repository T1/T2/T3 | `.claude/agents/repository-reviewer.md` | Repository パス、Mapper XML パス、アプリ種別、**`theme=T1` / `T2` / `T3`**（3 回） |

各レイヤーの 3 テーマ結果は、後段でレイヤーごとにマージして 1 つの結果に統合する（重複観点なし＝T1/T2/T3 は排他的に観点を分担）。

---

### Step 2.5: 横断系ルールの補助チェック（3レイヤーに収まらないもの）

3 サブエージェントは Controller / Service / Repository の各レイヤーを担当するが、以下の**横断系ルール**はレイヤーに紐づかないため、対象スタックに該当する設定・実装があれば orchestrator（本スキル実行者）が追加で確認すること（`.claude/rules/springer-*.md` を参照）：

| 観点 | ルール | 主な確認先 |
|---|---|---|
| 設定ファイル構成（profile・`@Value`/`@ConfigurationProperties`） | `springer-config.md` | `application*.yml`・Config クラス |
| 分散トレーシング・IF ログ運用レベル（マスク不可カテゴリ） | `springer-resilience.md` | `application*.yml`・logging 設定 |
| Actuator・ヘルスチェック（公開制御・BASIC 認証・Readiness/Liveness） | `springer-resilience.md` | `application*.yml` |
| 流量制御・閉塞制御（Tomcat/HikariCP/HttpClient・ServletFilter→503） | `springer-resilience.md` | `application*.yml`・Filter |
| バッチ設計（1ジョブ1シェル・リラン性・戻り値・タイムアウト） | `springer-batch.md` | `batch/` スタック |
| API 認証認可方式（Implicit/ROPC 不使用・PKCE・トークン検証） | `springer-security-auth.md` | SecurityConfig・認可サーバ連携 |
| ユニットテスト規約（`〇〇Test`・層別構成） | `springer-testing.md` | `src/test/`（別途 `/unit-test-*` で担保） |
| E2E（全層横断・外部システム・DB 差分） | `springer-testing.md` | E2E テスト（`reacter-testing-playwright.md` も参照） |

横断系で違反が見つかった場合は、Step 3 のサマリーに「### 横断（違反 N 件）」セクションを追加して報告すること。

---

### Step 2.6: 詳細設計整合性チェック（CPC）

Step 2.5 と同様に、3 サブエージェントの担当レイヤーに紐づかない横断的な確認として、orchestrator（本スキル実行者）自身が実施する（サブエージェントを新設しない・9 エージェント並列構造は変更しない）。

**実行主体**: 主エージェント自身（サブエージェント不使用）。

**入力**:
- `{スタック}/docs/detail-design/プログラム仕様書_{機能ID}.md`（外部連携あり時は外部IF定義書も）
- `{スタック}/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/{controller,service,repository}.md`（詳細設計WB）

**確認内容**:
| 観点 | 確認内容 |
|---|---|
| (a) 構造的一致（CPC-1） | クラス設計仕様との構造的一致（メソッドシグネチャ・例外・トランザクション設定等が詳細設計と一致するか） |
| (b) 用語・表記の統一性（CPC-2） | `.claude/rules/cross-process-consistency.md` の判定基準に従い、用語・項目名・エンティティ名等が詳細設計WB・プログラム仕様書と一致するか（固有名詞・項目名等は完全一致、説明的記述は意味的一致） |

**発見時**: Step 3 のサマリーに「### 詳細設計整合性（違反 N 件）」セクションを追加すること。不整合を発見した場合は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を、Step 4 のレポート出力後・`/springer-review-fix` での修正時に適用する。

> `.claude/agents/springer-review-checklist.md`（観点台帳ベースの3レイヤー並列構造）は本チェックの対象外・変更しない。

---

### Step 2.7: 2 パス検証（#5・敵対的）

各レイヤーとも観点が多く取りこぼしリスクがあるため、Step 2 の各レイヤー 3 テーマ結果をマージした後、**検証エージェントで再検査**する。**3 レイヤー分の検証を 1 メッセージで並列起動**する：

- `.claude/agents/springer-review-verifier.md` を読み込み、その内容をプロンプト冒頭に含めて Agent ツールを呼び出す。
- 各検証に渡す情報: レイヤー（`Controller` / `Service` / `Repository`）、対象ファイルパス（Repository は Mapper XML も）、アプリ種別、**当該レイヤー 3 テーマのマージ済み判定結果**。
- 検証エージェントの「追加違反 / 判定訂正 / 未評価」の指摘を、各レイヤーのマージ済み結果に反映する。

---

### Step 3: 結果の統合

各エージェントの結果をレイヤー別にまとめ、以下の形式で出力してください。各レイヤーで **「未評価 0 件」** が報告されていることを確認し、未評価が残る場合は当該エージェントを再実行すること。

```
## レビュー結果サマリー

### Controller（違反 N 件 / 評価 M 件・未評価 0 件）
- [禁止] CTL-09 L42: ...
- [警告] CMN-09 L78: ...

### Service（違反 N 件 / 評価 M 件・未評価 0 件）
- ...

### Repository / Mapper XML（違反 N 件 / 評価 M 件・未評価 0 件）
- ...

### 横断（違反 N 件）  ※Step 2.5 で該当があった場合のみ
- [禁止] X-02 ...: ...

### 詳細設計整合性（違反 N 件）  ※Step 2.6 で該当があった場合のみ
- [不整合] (CPC-2) service.md L10 vs ItemServiceImpl.java L42: ...

## 優先修正箇所
（重大度が高い順に上位3件）
```

---

### Step 3.5: 件数統一の横断チェック

Step 3 で統合した全レイヤーの結果に対し、以下の点を確認してから Step 4 へ進むこと：

- 全観点 ID ファイルの `指摘件数:` と `### N.` の連番が一致しているか
- 複数ファイルの指摘が 1 つの `### N.` にまとまっていないか（ファイル単位で分割されているか）
- 件数の単位（ファイル単位）が観点をまたいで統一されているか

不一致があれば該当ファイルを修正してから次のステップへ進む。

---

### Step 4: レビューレポートのファイル出力（必須・修正の取りこぼし防止）

メインエージェントは結果を表示するとともに、**複数ファイルに分けて**出力する。後続の修正フェーズはこのファイルを唯一の真実として消し込む。

**タイムスタンプの取得（必須・推定値を使わないこと）**

フォルダ名に使う `YYYYMMDD-HHMMSS` は、モデルが推定した値（例: `120000`）を**絶対に使わない**。
ファイル出力の直前に以下の PowerShell コマンドを実行し、その出力値をそのままフォルダ名に使用すること：

```powershell
Get-Date -Format "yyyyMMdd-HHmmss"
```

**出力先**（specs・push・PR対象）
- `{スタック}/specs/{案件キー}/implementation/{issue_id}_{機能ID}/springer-review-report.md`
- 受け入れ条件・テスト観点は `docs/base-design/テストシナリオ.md`・`docs/` を参照。
- レビュー対象コードは `{スタック}/src/main/`。
- issue 外: `reports/springer-review/springer-review-{対象名}-{YYYYMMDD-HHMMSS}/`
- 判別できない場合はユーザーに出力先を確認する。
- 各ファイルは **Write ツール・絶対パス**で個別に書き込む（「⚠ 実行上の制約」を厳守。`cd` 禁止・シェルでの書き込み禁止）。

## ⚠ 実行上の制約（必読）

- **Bash ツールで `cd` を使用しない**: CWD を変更するとフックの相対パスが壊れ Write/Edit ツールがブロックされる。ファイルパスは常に絶対パスで指定する。
- **レポートファイルの書き込みは Write ツールのみ使用**: Python スクリプト・`node -e`・`printf` 等のシェルコマンドによる代替書き込みは禁止。Write ツールがブロックされた場合は原因を調査し解消してから書き込む（フックエラーは `.claude/settings.json` の CWD 問題が原因）。

### 生成ファイル構成

```
springer-review-{対象名}-{YYYYMMDD-HHMMSS}/
├─ summary.md          # サマリ（正確な件数・全指摘の要約一覧）
├─ MAY.md              # MAY 指摘を全件まとめる
├─ {観点ID}.md         # MUST / SHOULD 指摘を観点ID単位で1ファイル（例 SEC-01.md, STR-03.md）
└─ ...                 # 指摘のある観点ID（分類 MUST/SHOULD）の数だけ生成
```

- **観点IDファイル（`{観点ID}.md`）**: 分類が **MUST または SHOULD** の指摘を、台帳の観点ID単位で 1 ファイルにする。**指摘が 1 件以上ある観点IDのみ生成**（0 件の ID はファイルを作らない）。1 観点IDに複数の指摘箇所があれば**全件**を 1 ファイルに列挙する。
  - 観点IDの分類は台帳で確定済み（その ID は MUST か SHOULD のいずれか）。ファイル冒頭に分類を明記し、**MUST と SHOULD が一目で判別できる**ようにする。
- **`MAY.md`**: 分類 MAY の指摘（条件付き任意・参考 `REF-`・横断の可読性/保守性・ルール非紐づけ）を**全件**まとめる。観点IDごとには分けない。
- **`summary.md`**: 全指摘の要約一覧と**正確な件数**。曖昧件数は禁止。

### 各ファイルのフォーマット

**`{観点ID}.md`（MUST / SHOULD）**
```
# CTL-20 SecurityConfig のアノテーション・認可設定

- **分類:** MUST
- **制約レベル:** 🚫禁止 (NEVER)
- **根拠ルール:** springer-security-auth.md
- **指摘件数:** 2 件

## 指摘箇所
### 1. [指摘のタイトル]
- **場所:** `{対象名}/src/x/X.tsx` (L12-L20)
- **問題点:** -
- **修正案:** -
```

**`MAY.md`**
```
# MAY 指摘一覧（M 件）

## 1. [指摘のタイトル]
- **観点ID / 根拠:** `X-01`（条件付き） / springer-config.md / 可読性は「ルール非紐づけ」
- **制約レベル:** 条件付き / 参考・推奨実装パターン / ルール非紐づけ
- **場所:** `path/to/application.yml` (L40)
- **問題点 / 修正案:** -
```

**`summary.md`**
```
# Springer コードレビュー サマリ

- **対象:** {対象名}（対象ファイル N 件）
- **実施日時:** YYYY-MM-DD HH:MM:SS

## 分類別件数
| 分類 | 件数 |
|---|---|
| MUST | A |
| SHOULD | B |
| MAY | C |
| 合計 | A+B+C |

## MUST / SHOULD 指摘一覧（観点ID別）
| 観点ID | 分類 | 制約レベル | タイトル | 件数 | ファイル |
|---|---|---|---|---|---|
| CTL-20 | MUST   | 🚫禁止 | SecurityConfig の認証=401/認可=403 設定           | 2 | CTL-20.md |
| CMN-14 | MUST   | 🚫禁止 | ロガーは LoggerFactory.APP.getLogger() で取得       | 1 | CMN-14.md |
| CTL-11 | SHOULD | ⚠警告  | グローバル例外ハンドラの MVC 例外ステータス整合      | 1 | CTL-11.md |

## MAY 指摘（要約）
| No | 観点ID/根拠 | タイトル | 件数 | 代表場所 |
|---|---|---|---|---|
| 1 | X-01（条件付き） | 設定ファイル profile 構成 | 1 | `...application.yml` (L30) |
（詳細は MAY.md 参照）
```

### 記載ルール（全ファイル共通・重要）

**場所の記載ルール**
- 問題箇所が複数ある場合でも、**ファイル名と行番号を問題箇所ごとにすべて各行に列挙**する。件数まとめ・「複数ファイル」等の概略表記は禁止。
- 1 箇所: `` `path/to/file.tsx` (L12) `` または `(L12-L20)`。複数箇所は箇条書きで各行を列挙。

**曖昧表現の禁止**
- すべてのフィールド（場所・問題点・修正案・タイトル・サマリ表・件数）で「等」「〜など」「多数」「複数箇所」「複数ファイル」「XX以上」「XX件以上」「概算」「〜他」を禁止する。
- 件数・ファイル数・観点ID数は**正確な数**を記載する（例: ×「20ファイル以上」→ ○「21ファイル」）。`summary.md` の件数は観点IDファイル・MAY.md の実件数と一致させる。

**件数の単位**
- 1 件 = 1 ファイルの 1 指摘箇所とする（ファイル単位でカウントする）
- 「設定が存在しない」等の不作為違反も、問題が確認されたファイルごとに 1 件として数える（例: タイムアウト設定がない場合、対象ファイルが 3 件あれば 3 件と計上する）
- 修正が 1 箇所で済む場合でも、問題が確認されたファイルはすべて列挙する


## 横断（該当時）
（X-xx の指摘を同形式で記載）

## 消し込みルール（修正フェーズで使用）
- `[禁止]` は原則すべて `対応済` にする。見送る場合は `対応不要（理由）` を必須記載。
- 最終ゲート: **未対応 0 件**（`未対応` が残る状態で完了としない）。
- 修正は `/springer-review-fix {このファイルのパス}` で実施し、ステータスを消し込む。
```

- ID は `R-{連番}`（このレポート内で安定）。観点 ID（CMN/CTL/SVC/REP/X-xx）と必ず併記する。
- 全 finding の初期ステータスは `未対応`。
- 出力後、ファイルパスをユーザーに伝え、**修正は `/springer-review-fix` で行う**よう案内する（このスキルでは原則ファイルを直接修正しない）。
