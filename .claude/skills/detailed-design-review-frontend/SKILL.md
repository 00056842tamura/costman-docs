---
name: detailed-design-review-frontend
description: 詳細設計WB（frontend.md）をReacter規約・基本設計との整合性観点でAIレビューする（工程4 SS・frontend×機能ID）
---

# 詳細設計AIレビュースキル・フロントエンド版（工程4 SS）

`frontend/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/frontend.md` の **詳細設計WB** を以下の3観点でレビューする:
1. Reacter 規約への準拠
2. 基本設計（`docs/base-design/`）との整合性
3. WB 網羅性（frontend.md の全セクション）

> **レビューレポートは specs（push・PR対象）**。`phase-{N}` 概念は廃止（frontend×機能ID）。

## 入力形式
```
/detailed-design-review-frontend
案件キー: inventory-2026-001
機能ID: front-inter-001
スタック: frontend
SS issue-id: {issue_id}
```

---

## 実行手順

### Step 1: 対象ファイルの確認
- [ ] `frontend/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/frontend.md` が存在するか確認
- [ ] `frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md`・`画面アクション遷移図_{機能ID}.md`（`/detailed-design-gen` Step5で生成済み）が存在するか確認（観点D の対象）

### Step 2a: frontend.md 先読みと特性判定

`frontend.md` を読み込み、以下のキーワードで機能特性フラグを判定する:

| 判定キーワード | 特性フラグ |
|---|---|
| `React Hook Form` / `Zod` / `フォーム` / `バリデーション` | `has_form = true` |
| `AG-Grid` / `グリッド` / `ColDef` / `reacter-grid` | `has_grid = true` |
| `Route` / `ルーティング` / `画面遷移` / `Navigate` | `has_routing = true` |
| `ErrorBoundary` / `エラーハンドリング` / `ErrorPage` | `has_error_handling = true` |
| `useContext` / `Context` / `Provider` | `has_context = true` |
| `useMemo` / `useCallback` / `React.memo` / `パフォーマンス` | `has_performance = true` |
| `CSS` / `スタイリング` / `module.css` | `has_css = true` |
| `getMessage` / `メッセージ管理` / `message.ts` | `has_messaging = true` |
| `Cache-Control` / `キャッシュ` | `has_cache = true` |
| `sourcemap` / `vite.config` / `ビルド設定` | `has_build_config = true` |

### Step 2b: 常時読み込みファイル

以下のドキュメントを**必ず**読み込む:

| ドキュメント | 用途 |
|---|---|
| `docs/base-design/機能概要_{機能ID}.md` | 機能・処理概要（整合性確認の基準） |
| `docs/base-design/Web_API_IF定義書_{機能ID}.md` | HTTPメソッド・パス・型（整合性確認の基準） |
| `docs/base-design/シーケンス図_{機能ID}.md` | 画面遷移・処理フロー（整合性確認の基準） |
| `docs/base-design/画面状態遷移図_{機能ID}.md` | UI工程確定済みの状態一覧（観点D縦軸チェックの基準） |
| `frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md` | 同じWBから生成された「画面遷移」「アクション定義」表（観点D横軸チェックの比較対象） |
| `frontend/docs/detail-design/画面アクション遷移図_{機能ID}.md` | 同上（観点D横軸チェックの比較対象） |
| `.claude/rules/reacter-coding-standards.md` | 実装全般・`any` 型・`React.FC` 禁止 |
| `.claude/rules/reacter-folder-structure.md` | 命名・フォルダー構成 |
| `.claude/rules/reacter-server-communication.md` | サーバ通信（axios）・エラー表示 |
| `.claude/rules/reacter-security.md` | セキュリティ（XSS / CSP） |

> `frontend/docs/detail-design/共通設計書.md` が存在する場合は追加で読み込む（非機能共通方針〔ログイン方式・共通レイアウト・ルーティング全体構成・エラーハンドリング方針・状態管理方針・環境変数・CSP〕の整合性確認の基準。観点B-8で使用）。上流 `docs/base-design/非機能共通設計.md` にfrontendの非機能共通設計がまだ反映されていない場合は存在しないため、その場合は観点B-8を「対象外（上流未確定）」として扱う。

### Step 2c: 条件付き読み込みファイル

Step 2a の判定結果に基づき、該当するファイルのみ追加で読み込む:

| 特性フラグ | 追加で読み込むファイル |
|---|---|
| `has_form = true` | `.claude/rules/reacter-form-validation.md` |
| `has_grid = true` | ⚠️ 対応ルールファイルなし（`reacter-grid.md` は廃止済み・後継なし。要対応） |
| `has_routing = true` | `.claude/rules/reacter-routing.md` |
| `has_error_handling = true` | `.claude/rules/reacter-error-handling.md` |
| `has_context = true` | `.claude/rules/reacter-state-management.md` |
| `has_performance = true` | `.claude/rules/reacter-performance.md` |
| `has_css = true` | `.claude/rules/reacter-css.md` |
| `has_messaging = true` | `.claude/rules/reacter-libraries.md` |
| `has_cache = true` | `.claude/rules/reacter-cache-control.md` |
| `has_build_config = true` | `.claude/rules/reacter-app-config.md` |

### Step 3: 3観点でレビューを実施

#### 観点A: Reacter規約準拠

##### A-常時（全機能に適用）

| # | チェック項目 | 確認箇所（frontend.md） | 根拠規約 |
|---|---|---|---|
| A-1 | コンポーネント名が PascalCase か | 画面構成・コンポーネント仕様テーブル | `reacter-folder-structure.md` |
| A-2 | Feature ディレクトリが `src/features/{機能名}/` 配下か | 基本情報「Feature ディレクトリ」 | `reacter-folder-structure.md` |
| A-3 | フォームに React Hook Form + Zod の使用が明記されているか | コンポーネント仕様の「主な処理」・Zodバリデーションセクション | `reacter-form-validation.md` |
| A-4 | 型定義に `any` 型が使われていないか | 共通型定義セクションの TypeScript 型 | `reacter-coding-standards.md` |
| A-5 | カスタムフック名が `use` から始まっているか | コンポーネント仕様テーブルのフック名 | `reacter-folder-structure.md` |
| A-6 | Axios インスタンス（`src/lib/axiosConfig.ts`）の使用が明記されているか | API 接続仕様テーブル | `reacter-server-communication.md` |
| A-7 | `dangerouslySetInnerHTML` / `innerHTML` 直接代入がないか | コンポーネント仕様・処理記述 | `reacter-security.md` |
| A-8 | `React.FC` を使用していないか | コンポーネント仕様の型定義 | `reacter-coding-standards.md` |
| A-9 | CSS Modules ファイル名が `コンポーネント名.module.css` か | コンポーネント仕様（CSSファイル言及がある場合） | `reacter-folder-structure.md` |
| A-10 | CSP設計にワイルドカード `*` のみの指定がないか（`connect-src *` は 🚫） | セキュリティ設計方針 | `reacter-security.md`（CSP） |
| A-11 | サーバエラーメッセージを画面に直接表示しない設計か | API接続仕様「エラー処理」列 | `reacter-server-communication.md` |
| A-12 | 参照制御はサーバ側で実施する設計か（フロントで非表示のみは 🚫） | API接続仕様・表示制御記述 | `reacter-security.md` |
| A-13 | ファイル名・変数名の先頭・末尾にアンダースコアがないか | コンポーネント仕様・型定義の名称 | `reacter-folder-structure.md` |

##### A-ルーティング（`has_routing = true` の場合のみ）

| # | チェック項目 | 確認箇所 | 根拠規約 |
|---|---|---|---|
| A-R1 | React Router（`react-router` or `react-router-dom`）を使用しているか | ルーティング設計 | `reacter-routing.md §3-1` |
| A-R2 | リダイレクトに `<Navigate to="..." replace />` を使用しているか | 画面遷移定義 | `reacter-routing.md §3-3` |
| A-R3 | メニュー単位で `lazy` + `Suspense` コード分割が設計されているか | コンポーネント構成 | `reacter-routing.md §3-11` |
| A-R4 | コード分割した親パスに `/*` が付いているか（例: `/item/*`） | ルートパス定義 | `reacter-routing.md §3-12` |

##### A-エラーハンドリング（`has_error_handling = true` の場合のみ）

| # | チェック項目 | 確認箇所 | 根拠規約 |
|---|---|---|---|
| A-E1 | `ErrorBoundary` でアプリ全体（または適切スコープ）を囲む設計か | エラー処理設計 | `reacter-error-handling.md` |
| A-E2 | `ErrorPage` で `error.message`（サーバーエラー等）を直接表示しない設計か | エラー画面仕様 | `reacter-error-handling.md` |
| A-E3 | イベントハンドラ・非同期処理内のエラーは `showBoundary(error)` で補足しているか | 非同期処理設計 | `reacter-error-handling.md` |

##### A-AG-Grid（`has_grid = true` の場合のみ）

| # | チェック項目 | 確認箇所 | 根拠規約 |
|---|---|---|---|
| A-G1 | `reacter-grid` ラッパー経由で AG-Grid を使用しているか（直接 import は 🚫） | コンポーネント仕様 | ⚠️ ルール未整備（`reacter-grid.md` は廃止済み・後継なし。実装済みの `reacter-grid` ラッパー実体があれば準拠、無い場合は要確認） |
| A-G2 | グリッドを囲む `div` に `height` が明示されているか | 画面構成・レイアウト記述 | ⚠️ ルール未整備（同上） |
| A-G3 | `ColDef` にジェネリクスで行データの型を指定しているか（`any` は 🚫） | 型定義・コンポーネント仕様 | `reacter-coding-standards.md`（`any` 型禁止） |

##### A-ビルド設定（`has_build_config = true` の場合のみ）

| # | チェック項目 | 確認箇所 | 根拠規約 |
|---|---|---|---|
| A-B1 | 本番ビルドで `sourcemap: false`（変更禁止）になっているか | ビルド設定仕様 | `reacter-security.md §8-3` |
| A-B2 | 本番ビルドで `esbuild.drop: ['console', 'debugger']` が設定されているか | ビルド設定仕様 | `reacter-app-config.md` |

#### 観点B: 基本設計（`docs/base-design/`）との整合性

| # | チェック項目 | 確認方法 |
|---|---|---|
| B-1 | API パス・HTTP メソッドが `Web_API_IF定義書_{機能ID}` と一致するか | API接続仕様テーブル vs. 定義書 |
| B-2 | リクエスト型・レスポンス型が定義書と一致するか | API接続仕様・共通型定義 vs. 定義書 |
| B-3 | HTTPステータスコードとエラー処理が定義書と一致するか | API接続仕様「エラー処理」列 vs. 定義書 |
| B-4 | 画面遷移が `シーケンス図_{機能ID}.md` と一致するか | 画面遷移テーブル vs. シーケンス図 |
| B-5 | 機能概要の「処理概要」がコンポーネント構成に反映されているか | 画面構成・コンポーネント仕様 vs. 機能概要 |
| B-6 | Zod バリデーションルールが定義書の「編集内容」と一致するか | Zodバリデーションテーブル vs. 定義書 |
| B-7 | 用語・項目名・コンポーネント名等の表記が基本設計と一致するか（CPC-2） | `.claude/rules/cross-process-consistency.md` の判定基準に従い、固有名詞・項目名・列名等は完全一致、説明的記述は意味的一致で確認 |
| B-8 | frontend.md/コンポーネント仕様書のルーティング・エラーハンドリング・状態管理・ログイン方式の記述が `共通設計書.md` の非機能方針と矛盾しないか（存在する場合） | frontend.md/コンポーネント仕様書_{機能ID}.md vs 共通設計書.md を照合（CPC-2の完全一致/意味的一致の判定基準に従う） |

#### 観点C: WB網羅性（frontend.md の全セクション）

frontend.md テンプレートの全7セクションを対象に確認する。

| # | セクション | 確認内容 |
|---|---|---|
| C-1 | 基本情報 | 機能名・Feature ディレクトリ・対応機能ID・対応USAPIエンドポイントが全て記載されているか |
| C-2 | 画面構成 | 全画面の「画面名・コンポーネント名・ルートパス・概要」が記載されているか |
| C-3 | コンポーネント仕様 | 各コンポーネントの「Props・主なState・主な処理」が記載されているか |
| C-4 | API接続仕様 | 全API操作の「HTTP・エンドポイント・リクエスト型・レスポンス型・エラー処理」が記載されているか |
| C-5 | 入力バリデーション（Zodスキーマ） | 全フィールドの「型・バリデーションルール・エラーメッセージ」が記載されているか |
| C-6 | 画面遷移 | 全操作の「遷移先・条件」が記載されているか |
| C-7 | 状態管理 | 「管理方法・説明」が記載されているか（React Query / React Hook Form / useState の使い分けが明示されているか） |
| C-8 | 共通型定義 | TypeScript 型が記載されており、API接続仕様と整合しているか |

#### 観点D: 画面アクション遷移図の整合性（縦軸・横軸）

`画面アクション遷移図_{機能ID}.md`は`コンポーネント仕様書_{機能ID}.md`と同じWB（frontend.md）から生成されるが、生成後に実際に内容が一致しているかは別途確認が必要（生成時の一致指示だけでは保証されない）。

| # | 軸 | チェック項目 | 確認方法 |
|---|---|---|---|
| D-1 | 横軸（同一機能ID内の生成物間） | 画面アクション遷移図の「アクション×状態対応表」の各行（アクション・遷移先・条件）が、コンポーネント仕様書の「画面遷移」表と一致するか | 両表のアクション名・遷移先・条件を1行ずつ照合 |
| D-2 | 横軸（同一機能ID内の生成物間） | 画面アクション遷移図の各アクションが、コンポーネント仕様書の「アクション定義」表のトリガー・処理シーケンスと矛盾しないか（画面表示の変化がシーケンスの内容と一致するか） | 両表のアクション行を1行ずつ照合 |
| D-3 | 縦軸（UI工程の正本との整合） | 画面アクション遷移図のノードに表記した状態名が、`docs/base-design/画面状態遷移図_{機能ID}.md`の「状態一覧」に定義された状態と矛盾しないか（UI工程確定済みの状態定義を書き換えていないか） | 画面アクション遷移図のノード名 vs 画面状態遷移図の状態一覧を照合 |
| D-4 | 構文 | Mermaid `flowchart` の構文が正しいか（ノードID重複なし・エッジの参照先が全て定義済みノードか） | 図のソースを構文確認 |

### Step 4: 結果の出力（レポートは specs・push・PR対象）

```
## 詳細設計レビュー結果（工程4 SS・frontend×{機能ID}）

### 観点A: Reacter規約違反

#### 常時適用
- A-1 コンポーネント命名（違反 N 件）: ...
- A-10 CSP設計（違反 N 件）: ...
...（A-1〜A-13 全て確認。違反 0 件の項目も「問題なし」と明記）

#### 条件付き（適用した場合のみ記載）
- A-R（ルーティング）: 適用 / 非適用
  - A-R1 ...: ...
- A-E（エラーハンドリング）: 適用 / 非適用
  - A-E1 ...: ...
- A-G（AG-Grid）: 適用 / 非適用
  - A-G1 ...: ...
- A-B（ビルド設定）: 適用 / 非適用
  - A-B1 ...: ...

### 観点B: 基本設計との整合性不一致
- B-1 APIパス・HTTPメソッド（不一致 N 件）: ...
- B-4 画面遷移（不一致 N 件）: ...
...

### 観点C: WB網羅性不足
- C-4 API接続仕様（不足 N 件）: ...
...

### 観点D: 画面アクション遷移図の整合性不一致
- D-1 画面遷移表との不一致（不一致 N 件）: ...
- D-3 画面状態遷移図との不一致（不一致 N 件）: ...
...（D-1〜D-4 全て確認。不一致 0 件の項目も「問題なし」と明記）

## 優先修正箇所
## 総評（PR提出可否）
```

レポートは `frontend/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/review-report.md`（specs・push・PR対象）。

### Step 5: ADR-SS-{n} の起票（強制ゲート）

レビュー結果に関わらず、本レビューで確認した設計上の判断事項ごとに **ADR-SS-{n}** を必ず起票する。

- **finding が 0 件の場合**: `ADR-SS-001-design.md`（finding なし・Reacter 規約準拠確認済み）を 1 件作成する
- **finding がある場合**: 対処方針・採用したアプローチごとに 1 件ずつ起票する

```
命名規則: ADR-SS-{n}-design.md（{n} = 001, 002, ... 連番）
格納先: frontend/specs/{案件キー}/detail-design/{issue_id}_{機能ID}/adr/
内容: 判断事項のタイトル・選択した設計・選択しなかった代替案・採用理由
```

### Step 6: 修正案の提示と手戻り連携

重大な違反は修正案を提示し、確認後にファイルを修正する。修正後は `.claude/rules/cross-process-consistency.md`「不整合発見時の手戻り連携手順」を適用する（手順1 インラインフィックス判定 〜 手順5 大規模手戻りエスカレーション。既存の「上位設計書確認」の内容は手順4 に相当し、そのまま包含される）。手戻りID採番（手順3）を経由しない簡易修正は、手順1のインラインフィックス判定に該当する場合のみ許容する。

---

## 制約
- 🚫 コードの生成・修正はしない（設計書のレビューのみ）
- 🚫 **WB 修正に `/springer-review-fix` を流用しない**（PG 工程向けスキルのため。WB 修正は本スキルの Step 6 内で完結させる）
- ✅ 違反箇所は具体的な箇所（セクション名・コンポーネント名・フィールド名等）を明示する
- ✅ レビューレポートは specs（push・PR対象）。`phase-{N}` 概念は使わない
- ✅ 「警告なし」の場合も全観点を確認したことを明示する
- ✅ ルールファイルは Step 2a の特性判定後、必要なもののみ読み込む（常時 3 ファイル＋条件付き）

## 関連
- 前: `/detailed-design-gen`（frontend.md WB生成）
- 次: PR（`frontend/docs/detail-design/` がある場合）→ 人間レビュー → 工程6 PG `/reacter-code-gen`
- コードレビュー: `/reacter-code-review`（実装後）

## 作業指示
$ARGUMENTS
