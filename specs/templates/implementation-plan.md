> ⚠️ 本テンプレートは旧ワークフロー（実装計画＝単一 implementation-plan.md）のもの。新ワークフローの計画工程（SS-Plan/PG-Plan/PT-Plan）は `specs/templates/plan.md`（スタック×機能ID の sub-issue 棚卸し）を使う。

# 実装計画 — issue-{番号}: {タイトル}

## 分割粒度: {指定した粒度}

<!-- 粒度の推奨: スタック単位 / 機能単位 / 1 Phase にまとめる -->
<!-- レイヤー単位（Controller / Service / Repository）は非推奨 -->

---

## Phase 1: {Phase名}

### 対象スタック
{例: `bs/`、`frontend/` など}

### 実装範囲
{この Phase で実装するクラス・API・画面などを箇条書きで記載}

### Step 4: 詳細設計 対象クラス
{クラス名と種別（Controller / Service / Repository 等）の一覧}
- {クラス名}（{新規 / 変更}）: {概要}

### Step 8: テスト生成 対象
- ホワイトボックス:
  {Service・Repository の単体テスト対象メソッドとテストケース概要}
- ブラックボックス:
  {Controller・Frontend の APIテスト対象エンドポイントと test-scenario.md の対応番号}

### 完了条件
- Step 5 マージ済み（詳細設計PR）
- Step 7 マージ済み（製造PR）
- Step 10-1 確認済み（テスト結果人間承認）
- Step 11 マージ済み（実装PR）

---

## Phase 2: {Phase名}（必要に応じて追加・削除）

### 対象スタック
{例: `us-api/` など}

### 実装範囲

### Step 4: 詳細設計 対象クラス

### Step 8: テスト生成 対象
- ホワイトボックス:
- ブラックボックス:

### 完了条件
- Step 5 マージ済み（詳細設計PR）
- Step 7 マージ済み（製造PR）
- Step 10-1 確認済み（テスト結果人間承認）
- Step 11 マージ済み（実装PR）

---

<!-- Phase が 1 つの場合は Phase 2 以降を削除 -->
<!-- Phase が 3 つ以上の場合は同じ形式でセクションを追加 -->
