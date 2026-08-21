<!-- UI PR テンプレート（基本設計・案件リポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- docs/base-design/機能概要_{機能ID}.md から1〜2文で引用 -->

## 成果物
- docs/base-design/機能一覧.md（要件ID {要件ID} → 機能ID {機能ID...} 分解・採番）
- docs/base-design/機能概要_{機能ID}.md
- docs/base-design/Web_API_IF定義書_{機能ID}.md（1機能ID=1API）
- docs/base-design/シーケンス図_{機能ID}.md
- docs/base-design/テーブル一覧.md（追記・T###採番）・テーブル定義書_{テーブルID}_{テーブル名}.md（bs/batchのみ・**正本**）
- docs/base-design/実装対象クラス一覧.md（追記）
- docs/base-design/テストシナリオ.md（追記）
- docs/base-design/ロバストネス図_{要件ID}.md・クラス図.md（追記）・画面状態遷移図_{機能ID}.md（frontendのみ）
- docs/base-design/Web_API_IF一覧表.md（行追記）
- （任意）ファイルレイアウト一覧_{機能ID}.md / ファイル定義書_{機能ID}.md

## 機能ID間インターフェース
<!-- 同一要件から分解された機能ID間（例: us-apiのAPIとbsのAPI）の呼び出し関係・破壊的変更の有無を明記 -->

## 影響スタック
{{STACK_BLOCK}}

## 設計の意思決定（specs も本 PR に含めて push）
- discussion-log: specs/{{CASE}}/base-design/{issue_id}/discussion-log.md
- ADR: specs/{{CASE}}/base-design/{issue_id}/adr/ADR-UI-{n}-design.md（決定事項ごとに連番・全件）

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] 機能一覧の機能ID（`{カテゴリ}-{3桁連番}`）採番に重複がないか・要件IDとの対応が正しいか
- [ ] 関連する機能ID間の I/F が整合しているか
- [ ] Web_API_IF一覧表に当該機能が追記されているか
- [ ] 認証・認可方針が明確か
- [ ] テストシナリオが受け入れ条件を全件カバーしているか
- [ ] テーブル定義書のカラム定義・制約・インデックスが妥当か（SS工程では変更されないため念入りに確認）
- [ ] 上位設計書との整合を確認している（`/basic-design-review` の review-report.md の存在で確認・要件定義との用語・表記ゆれなし）
- [ ] discussion-log/ADRの未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（次工程への持ち越しがある場合は次工程issueの上流成果物節に転記済み）
