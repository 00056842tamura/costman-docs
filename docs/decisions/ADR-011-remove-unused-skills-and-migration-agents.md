# ADR-011: 未使用スキル（e2e_code・validation-confirmation-gen）と移行ボリューム試算エージェント（計14種）の削除

## メタ情報
| 項目 | 値 |
|---|---|
| 日付 | 2026-07-15 |
| ステータス | 承認済み |
| 対象 Issue | N/A（案件に属さない機構メンテナンス。ユーザーとの質疑を契機） |
| 起票 Step | 工程外（プロダクト機構〔`.claude/`・`CLAUDE.md`・`docs/`〕の整合性メンテナンス） |
| 関連 ADR | ADR-001（`/config-review` によるルール参照ドリフト修正。本ADRは同ADRの決定事項7・未確定事項バックログの一部を上書きする） |

## 背景

### ①状況
ユーザーから「要件定義〜単体テスト（8工程モデル）の中で使われていないスキルは何か」との質疑があり、調査したところ以下が判明した。

1. `e2e_code`（Playwright E2Eテスト生成）・`validation-confirmation-gen`（妥当性確認実施票生成）は CLAUDE.md §6 に「工程非依存の独立スキル」として記載されているが、§4 の8工程（SA・UI・SS-Plan・SS・PG-Plan・PG・PT-Plan・PT）の「手順」列には一度も登場せず、ユーザーの実運用（8工程モデルの範囲）では使用されていなかった。
2. `reacter-mig-*`（6種）・`springer-mig-*`（8種）の移行ボリューム試算エージェント計14種は、`.claude/agents/` に定義だけが存在し、CLAUDE.md・全ルールファイル・全スキルのどこからも呼び出されていなかった。ADR-001（2026-07-03）で既にこの状態は確認されており、当時は「新規構築を見送り、現状を維持」（決定事項7）と決定されていた。

### ②課題・問題
1. の2スキルは、ユーザーの実運用工程では到達しないにもかかわらず、CLAUDE.md・複数の README・スキル定義・issueテンプレートに広く参照が残り、ドキュメントの見た目上の複雑さを増やしていた。
2. の14エージェントは、呼び出し元スキル（`.claude/skills/{reacter|springer}-migration-plan/`）が実在しないため実行時に必ず失敗する状態が継続しており、ADR-001以降も解消されていなかった。

### ③制約
- CLAUDE.md §10 により `docs/` 配下の既存ファイルは無断で変更しないという制約があるが、本対応はユーザーからの直接の削除指示に基づく。
- 既存ADR（ADR-001・ADR-002等）は遡及修正しない原則のため、ADR-001本文は変更せず、本ADRで決定の上書きを記録する。

## 決定事項

1. `.claude/skills/e2e_code/`・`.claude/skills/validation-confirmation-gen/` を削除した。
2. `.claude/agents/reacter-mig-{comms,form,quality,secbuild,structure,style}.md`（6件）・`.claude/agents/springer-mig-{build,comms,db,exception,logging,security,structure,tx}.md`（8件）計14件を削除した。ADR-001 決定事項7（現状維持）を本ADRにより上書きする。
3. CLAUDE.md §4（工程別テーブルのPT行・リリース前ゲート行）・§6（Reacter/共通スキル一覧表）から `/e2e_code`・`/validation-confirmation-gen` の行を削除し、リリース前ゲートの妥当性確認実施票確認・PTの完了ゲート説明は「人間が実施」に文言を修正した。
4. 削除した2スキルを参照していた非履歴ドキュメント（`.claude/skills/reacter-unit-test-gen/SKILL.md`・`.claude/skills/springer-unit-test-gen/SKILL.md`・`.github/ISSUE_TEMPLATE/pt.md`・`docs/templates/README.md`・`docs/test/README.md`・`README.md`・`docs/onboarding/claude-code-guide.md`）を、スキル名を削除し「人間が作成（生成スキルなし）」等の表現に統一した。
5. 作業ログ（`docs/changelog/work-logs/`）・既存ADR（ADR-001・ADR-002）は履歴のため遡及修正せず、削除済みスキル・エージェントへの参照が残ったままとした。
6. `.claude/rules/coding-standards.md`・`.claude/rules/naming-conventions.md`・`docs/onboarding/claude-code-guide.md` に残っていた旧ファイル名参照（`springer-naming.md`・`springer-checkstyle.md`・`springer-package-structure.md`・`springer-class-definition.md`・`springer-architecture.md`）を、現行ファイル名（`springer-package-class-naming.md`・`springer-coding-style.md`・`springer-tech-stack-dependency.md`・`springer-di-bean.md`・`springer-architecture-layer.md`）へ修正した。`springer-prohibited.md`（単一の後継ファイルなし）は ADR-001 の既存判断を踏襲し、無理な推測をせず現状のまま残した。

## 理由

- **2スキルを削除した理由**: CLAUDE.md §4 の手順テーブルに一度も登場しないことをユーザーとともに確認済みであり、ユーザーが明示的に不要と判断したため。8工程モデルの範囲外（リリース前ゲート・E2E）の機能自体を削除する判断はプロダクト側の裁量であり、本ADRはその実行記録である。
- **14エージェントを削除した理由（ADR-001決定の上書き）**: ADR-001時点では「将来の別タスクとして着手できるよう記録」する現状維持判断だったが、今回ユーザーが明示的に削除を指示したため、この判断を更新した。呼び出し元スキル・観点台帳・計測スクリプトが未着手のまま約2週間経過しており、実データに基づく再設計より先に、実行不能な定義だけを残すコストの方が大きいとユーザーが判断したものと理解している。
- **履歴ドキュメントを遡及修正しない理由**: `.claude/rules/github-ops.md`§3-B・過去ADRの扱いに関するプロダクトの一般原則（履歴は書き換えない）を踏襲した。
- **`springer-prohibited.md` を放置した理由**: ADR-001 で「単一の後継ファイルに解決できない参照は無理な推測をせず現状のまま残す」という基準が既に確立されており、本ADRもその基準を継承した。

## 影響

### 削除（動作に直接影響）
- スキル2件（`e2e_code`・`validation-confirmation-gen`）・エージェント14件（`reacter-mig-*`・`springer-mig-*`）が `.claude/` から削除された
- 移行ボリューム試算クラスタ（ADR-001未確定事項バックログの一部）は着手対象から外れた。将来必要になった場合は本ADRとADR-001の双方を参照し、ゼロから設計判断を行うこと

### 修正済み（参照整合性）
- CLAUDE.md・README.md・docs/templates/README.md・docs/test/README.md・docs/onboarding/claude-code-guide.md・.github/ISSUE_TEMPLATE/pt.md・reacter-unit-test-gen/springer-unit-test-gen の各SKILL.mdから、削除済みスキルへの参照が解消された
- coding-standards.md・naming-conventions.md・claude-code-guide.md のルールファイル参照ドリフト（一部）が解消された

### 現状維持
- `docs/changelog/work-logs/2026-07-02.md`・`2026-07-13.md`・ADR-001・ADR-002 内の削除済みスキル・エージェントへの言及は履歴として残存する（遡及修正しない）
- `.claude/rules/coding-standards.md`・`naming-conventions.md` の `springer-prohibited.md` 参照は未解決のまま残存する（ADR-001から継続）

## 未確定事項（バックログ）

- [x] ADR-001 未確定事項「`reacter-migration-plan`/`springer-migration-plan` の呼び出し元 SKILL.md・観点台帳・measure.sh の新規設計」: 対応不要（解消: 14エージェント自体を削除したため対象消滅・2026-07-15）
- [x] ADR-001 未確定事項「`reacter-mig-*`/`springer-mig-*` エージェント内部の個別ルールファイル参照の是正」: 対応不要（解消: エージェント自体を削除したため対象消滅・2026-07-15）
- [ ] `springer-prohibited.md`（単一の後継ファイルなし）の扱い確定: ADR-001から未解決のまま持ち越し
