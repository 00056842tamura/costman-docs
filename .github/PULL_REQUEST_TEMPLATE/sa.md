<!-- SA PR テンプレート（要件定義・案件リポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- docs/requirements/要件定義_{要件ID}.md の「目的・背景」から1〜2文で引用 -->

## 成果物
- docs/requirements/要件一覧.md（R###採番: {要件ID}）
- docs/requirements/要件定義_{要件ID}.md
- （必要に応じ）docs/requirements/業務概要.md 等

## 影響スタック
{{STACK_BLOCK}}

## 設計の意思決定（specs も本 PR に含めて push）
- discussion-log: specs/{{CASE}}/requirements/{issue_id}/discussion-log.md の主要決定
- ADR: specs/{{CASE}}/requirements/{issue_id}/adr/ADR-SA-{n}-requirements.md（決定事項ごとに連番・全件）

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] 受け入れ条件が要件ごとに明確か
- [ ] 要件一覧の R### 採番に重複がないか（機能ID（`{カテゴリ}-{3桁連番}`）への分解は UI 工程で実施）
- [ ] セキュリティ・非機能要件の記載漏れがないか
- [ ] discussion-log/ADRの未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（次工程への持ち越しがある場合は次工程issueの上流成果物節に転記済み）
