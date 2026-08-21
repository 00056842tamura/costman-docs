# ADR-002: PT（単体テスト）工程における3件のカバレッジギャップへの対応

## メタ情報
| 項目 | 値 |
|---|---|
| 日付 | 2026-07-06 |
| ステータス | 承認済み（対応方針のみ。実装内容は対応計画のレビュー後に確定） |
| 対象 Issue | N/A（案件に属さない機構メンテナンス） |
| 起票 Step | 工程外（工程8 PT の機構〔`unit-test-all`・`consistency-check`・Reacter テスト生成〕に関するユーザーとの質疑を契機） |
| 関連 ADR | ADR-001（`/config-review` によるルール参照ドリフト修正） |

## 背景

### ①状況
ユーザーから工程8 PT（単体テスト）の `unit-test-all` スキルについて質疑があり、実ファイルを確認した結果、以下3件のギャップが判明した。

### ②課題・問題

**課題1: `unit-test-all` の入力に詳細設計（SS工程成果物）が含まれない**
`.claude/skills/unit-test-all/SKILL.md` の入力は「PT-Planの`plan.md`・`docs/base-design/テストシナリオ.md`・実装コード`{スタック}/src/main`」の3つのみで、詳細設計WB（`{スタック}/docs/detail-design/`の外部IF定義書・プログラム仕様書）は含まれない。境界値・異常系テストの生成精度に影響する可能性がある。

**課題2: `consistency-check` に「詳細設計書→テストコード」の直接チェックが存在しない**
`.claude/skills/consistency-check/SKILL.md` の3チェック（A: 詳細設計書→コード、B: コード→テストコード、C: テストシナリオ→テストコード）を確認したところ、詳細設計書に記載されテストシナリオ.mdには反映されていない境界値・エラーコード等を捕捉する経路が構造的に存在しないことが判明した。

**課題3: Reacter（フロントエンド）向けの単体テスト生成スキルが存在しない**
`unit-test-all/SKILL.md` は対象スタックを `bs`/`us-mpa`/`us-api` のみに限定しており（`admin-us-api`・`batch`も未記載）、Reacter（`frontend/`）は対象外。`.claude/rules/reacter-testing-vitest.md` という規約ファイルは存在するにもかかわらず、これを用いてVitestテストを生成するスキルが `.claude/skills/` に一つも存在しない（`/e2e_code` はPlaywrightによるE2Eのみ）。

### ③制約
- 上記3件はいずれも実装（コード）の不具合ではなく、LLM機構（スキル・エージェント定義）自体の設計上の欠落・不足である。
- 修正は `unit-test-all`・`consistency-check` という工程8 PTの中核スキルに及ぶため、他のスキル・エージェント・オーケストレーター・ドキュメント（DEVELOPER_GUIDE.md・CLAUDE.md・docs/templates等）への影響調査が必要。
- ユーザーの指示により、影響調査と対応計画の作成を先行させ、計画のレビュー・承認後に実装へ着手する（本ADRの時点では実装しない）。

## 決定事項

上記3件のギャップは全て対応（機構への取り込み）することを決定する。ただし、具体的な実装方法（入力追加の形式・チェック追加の設計・新設スキルの構成等）は、影響調査を踏まえた対応計画のレビュー・承認を経て確定する。

## 理由

- 課題1・2は、詳細設計にのみ存在する情報がテスト生成・整合性検証のいずれからも漏れる可能性がある構造的なギャップであり、テストの網羅性・信頼性に直接関わるため対応が必要と判断した。
- 課題3は、Reacterスタックに対する単体テスト（Vitest）生成手段が皆無という機構全体のI-P-O（インプット・プロセス・アウトプット）上の欠落であり、Springer側との対称性の観点からも対応が必要と判断した。
- 影響調査を先行させる理由は、`unit-test-all`・`consistency-check` がPT工程の中核スキルであり、変更が `.claude/agents/`（controller/service/repository-test-gen等）・オーケストレーター・関連ドキュメント（DEVELOPER_GUIDE.md・CLAUDE.md・docs/templates）に波及する可能性が高いため、デグレ・論理破綻を避けるために事前の全体調査が不可欠と判断した。

## 影響

本ADR時点では未確定。対応計画（`specs/`または`docs/decisions/`に別途作成予定）の影響調査フェーズで以下を明らかにする：
- スキル・ルール等LLM機構全体への影響（`.claude/skills/`・`.claude/agents/`・`.claude/orchestrators/`・`.claude/rules/`）
- ドキュメントへの影響（`DEVELOPER_GUIDE.md`・`CLAUDE.md`・`docs/templates/`・各スタック`CLAUDE.md`）
- デグレ・論理破綻のリスク（既存の直接編集モデル・工程ゲート・手戻りフロー等との整合性）

## 未確定事項（バックログ）

- [ ] 課題1: `unit-test-all` への詳細設計入力追加方法（常時読み込みか条件付きか、どのファイルを対象にするか）
- [ ] 課題2: `consistency-check` への「設計書→テストコード」チェック追加の具体設計（既存3チェックとの重複回避）
- [ ] 課題3: Reacter単体テスト生成スキルの新設方針（`unit-test-all` への統合か、独立スキル`/reacter-unit-test-gen`等の新設か）。`admin-us-api`・`batch` の`unit-test-all`対象スタック未記載も併せて確認する
- [ ] 上記3件を含む対応計画の作成・レビュー・承認（本ADRの次ステップ）
