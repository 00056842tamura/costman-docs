<!-- SS PR テンプレート（詳細設計・スタックリポ）。pr-open.sh が {{}} プレースホルダーを自動置換します。 -->

## 工程 / 案件
- 案件キー（Milestone）: {{CASE}}
- 工程: {{PHASE_LINE}}
- 対応 issue: {{ISSUE_LINE}}
- ベースブランチ: feature/{{CASE}}

## 概要
<!-- docs/base-design/機能概要_{機能ID}.md の要点・本 PR の詳細設計範囲 -->

## 成果物（{スタック}/docs/detail-design/）
- 外部IF定義書.md（外部連携あり時）
- **プログラム仕様書_{機能ID}.md**（backend/batch）
- **コンポーネント仕様書_{機能ID}.md**・**画面アクション遷移図_{機能ID}.md**（frontend）
- **メッセージ一覧.md**（backend/batch・累積追記）
- （batch）ジョブネット一覧.md / ジョブフロー一覧_{機能ID}.md

※ テーブル一覧・テーブル定義書は UI 工程 `docs/base-design/` が正本のため、本 PR（SS）には含まれない（本工程は読み込み専用）。

## 影響スタック
{{STACK_BLOCK}}

## 検討の記録（specs・本 PR に含めて push）
- 設計WB: {スタック}/specs/{{CASE}}/detail-design/{issue_id}_{機能ID}/controller|service|repository|frontend|batch.md
- ADR-SS-{n}・review-report: 同ディレクトリ配下

## チェック
- [ ] 対応 issue の完了ゲートを満たしている
- [ ] 影響スタックを正しく明記している
- [ ] フック（規約チェック）の警告を解消している
- [ ] 形式設計書が基本設計（docs/base-design/）と整合しているか
- [ ] Springer 規約（DI・トランザクション・例外・MyBatis 等）に準拠しているか
- [ ] repository.md のカラム定義が `docs/base-design/` のテーブル定義書（テーブル単位）と網羅的に一致しているか
- [ ] 上位設計書との整合を確認している（review-report.md＋ADR-SS-{n} の存在で確認）
- [ ] discussion-log/ADRの未解決事項（バックログ）が解消済み、または承認済みの持ち越しとして記録されている（次工程への持ち越しがある場合は次工程issueの上流成果物節に転記済み）
