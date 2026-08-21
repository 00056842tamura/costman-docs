# ADR-001: `/config-review` で検出したルールファイル参照ドリフトの修正方針

## メタ情報
| 項目 | 値 |
|---|---|
| 日付 | 2026-07-03 |
| ステータス | 承認済み |
| 対象 Issue | N/A（案件に属さない機構メンテナンス。`/config-review` 実行契機） |
| 起票 Step | 工程外（プロダクト機構〔`.claude/`・`CLAUDE.md`・`DEVELOPER_GUIDE.md`・`docs/architecture/`〕の整合性メンテナンス） |
| 関連 ADR | - |

## 背景

### ①状況
`/config-review` を実行したところ、2026-06-27 前後に実施された `.claude/rules/` の Springer 系ルールファイル再編（旧名から現行23ファイル構成への改名・統合）が、root `CLAUDE.md` §5 の参照テーブル以外の箇所に広く未追随であることが判明した。影響範囲は以下の3系統に及んだ。

1. **ルールファイル参照の破損**（最大規模）: 全スタック `CLAUDE.md`（`bs`/`us-api`/`us-mpa`/`admin-us-api`/`batch`）・`_templates/*/CLAUDE.md`・`controller/service/repository-reviewer` スキル・`controller-test-gen` スキル/エージェント・`springer-review`/`detailed-design-review-{batch,frontend}` スキル・`reacter-mig-*`（6種）・`springer-mig-*`（8種）エージェントが、存在しない旧ファイル名（`springer-architecture.md`・`springer-di.md`・`springer-transaction.md`・`reacter-checklist.md`・`reacter-grid.md` 等）を「事前準備で読み込むファイル」として指定していた。
2. **欠落ファイル**: `springer-version-matrix.md`（13箇所以上から参照）・`reacter-checklist.md`（3箇所から参照）が実体を持たないまま広く参照されていた。
3. **機能未実装のクラスタ**: `reacter-mig-*`/`springer-mig-*`（移行ボリューム試算エージェント計14種）が依存する `.claude/skills/{reacter|springer}-migration-plan/SKILL.md`（呼び出し元スキル）・`observation-ledger.md`（観点台帳）・`measure.sh`（計測スクリプト）がいずれも未作成で、エージェント定義だけが先行して存在する状態だった。

### ②課題・問題
上記①②はスキル/エージェントが実行時に存在しないファイルを読み込もうとして失敗する実害があり、③は「観点ID・判定手順・工数単価まで書かれた14個のエージェント」が実際には一切起動できない状態だった。

### ③制約
- `CLAUDE.md` §10 により `docs/` 配下の既存ファイルは無断で変更しないという制約があるため、`docs/architecture/overview.md` の修正はユーザーに直接確認を取った。
- 新規ファイル作成は本来 `/config-review` の対象外（既存ドキュメントの整合性修正が主目的）だが、ユーザーから直接修正の承認を得たため、影響範囲が明確で書き手の裁量が小さい項目（バージョン対応表・`.nvmrc`）は新規作成した。
- 移行ボリューム試算クラスタの再構築（呼び出し元スキル2種・観点台帳2種・計測スクリプト1種の新規設計）は、既存の90観点分の判定基準・出力スキーマ・工数単価を新たに定義する必要があり、これは「不整合の修正」ではなく「新機能の設計・実装」に相当する規模と判断した。

## 決定事項

1. ルールファイル参照は、旧ファイル名と現行23ファイル構成の対応が一意に確定できるものに限り、その場で新ファイル名へ修正した（対象: 上記①の全ファイル）。
2. 単一の後継ファイルに解決できない参照（`springer-prohibited.md`・`springer-web-patterns.md`）は、各ファイルの「絶対禁止事項」箇条書きと内容が重複する場合は参照行を除去し、それ以外は無理な推測をせず現状のまま残した。
3. 欠落ファイルのうち、内容の正本が既存ルール文書から一意に導出できるものは新規作成した: `.claude/rules/springer-version-matrix.md`（CLAUDE.md・`springer-openapi.md`・`springer-security-auth.md`・`springer-resilience.md` の既存記述から Springer/Spring Boot/Java 対応表を再構成）。
4. `reacter-checklist.md` は新規作成せず、参照元を実際に内容を持つ既存ファイル（`reacter-coding-standards.md`・`reacter-form-validation.md`・`reacter-app-config.md`・`reacter-server-communication.md`・`reacter-folder-structure.md`・`reacter-security.md`）への参照に置き換えた。
5. `frontend/.nvmrc` は `frontend/CLAUDE.md` の既存記載（Node.js 24 で固定）と一致する内容で新規作成した。
6. `docs/architecture/overview.md` は、実在するが記載されていなかった `admin-us-api`・`batch` を、ユーザーの明示的な承認を得たうえでシステム構成図・責務表に追加した。
7. `reacter-mig-*`/`springer-mig-*`（移行ボリューム試算・計14エージェント）は、**新規構築を見送り、現状を維持**した。DEVELOPER_GUIDE.md §5.5 に「現状未実装機能」であることと欠落している3種の土台（呼び出し元スキル・観点台帳・`measure.sh`）を明記し、将来の別タスクとして着手できるよう記録した。
8. `detailed-design-review-frontend/SKILL.md` 内の個別チェック項目表（A-1〜A-13・A-E・A-G・A-B）は、節番号（`§2.4` 等）を含む参照を現行ファイル名ベースの参照に修正した。AG-Grid 関連（A-G1/A-G2）は後継ルールファイルが存在しないため項目自体は削除せず「ルール未整備」と明記した。

## 理由

- **修正 vs 報告のみの判断基準**: 旧ファイル名 → 新ファイル名の対応が既存文書（root `CLAUDE.md` §5 の参照テーブル等）から一意に確認できる場合は「その場で修正」、複数ファイルへの分割や後継ルールの不在など裁量・確認が必要な場合は「報告のみ」とする基準を一貫して適用した。誤ったマッピングを機構ファイルに書き込むことは、参照が壊れたままの状態より悪い結果（一見正しく見えるが実際には無関係なルールを読み込む）を招くため。
- **移行ボリューム試算クラスタを見送った理由**: 観点台帳・計測スクリプトは約90観点分の判定基準・grep パターン・工数単価という実務データを新規に定義する必要があり、既存資料から一意に復元できる範囲を超える。誤った基準・単価を機構に組み込むと、将来の移行ボリューム試算の結果自体が信頼できなくなるリスクがあるため、実データ・実運用知見を持つ担当者による設計を待つべきと判断した。
- **docs/ 配下の変更にユーザー確認を挟んだ理由**: `CLAUDE.md` §10 の明示的な禁止事項（`docs/` 配下の既存ファイルを無断で変更しない）を遵守するため。

## 影響

### 修正済み（動作に直接影響）
- 全スタック `CLAUDE.md`（5）・`_templates/*/CLAUDE.md`（5）の必須参照ルールが実ファイルを指すようになった
- `controller/service/repository-reviewer`・`controller-test-gen`（スキル+エージェント）・`springer-review`・`detailed-design-review-{batch,frontend}` の各スキルが、実行時に存在するルールファイルを読み込めるようになった
- root `CLAUDE.md`「このリポジトリの構成」表に実在する5スタックが記載された
- DEVELOPER_GUIDE.md §5.5/§8.3 のエージェント種別数・§8.2 の廃止スキル参照が実態と一致した
- `springer-version-matrix.md`・`frontend/.nvmrc` が新規作成され、13箇所以上・複数箇所の参照が解決した
- `docs/architecture/overview.md` に `admin-us-api`・`batch` が反映された

### 現状維持（副作用なし・今後の判断が必要）
- `reacter-mig-*`/`springer-mig-*`（14エージェント）は依然 `Agent` ツールから起動しても機能しない。誤って起動を試みるとエラーになる可能性がある点は DEVELOPER_GUIDE.md に明記済み
- `springer-mig-*` エージェント内部の個別ルールファイル参照（`springer-tracing.md`・`springer-crypto.md` 等、大半が旧名）は未修正のまま。土台（呼び出し元スキル・台帳）が無い限り実害は発生しない

## 未確定事項（バックログ）

- [ ] `reacter-migration-plan`/`springer-migration-plan` の呼び出し元 `SKILL.md`・観点台帳（`observation-ledger.md`）・`measure.sh` の新規設計 — 実務の移行工数単価・grep 計測パターンの実データが必要。着手時は本 ADR を参照し、14エージェントの既存記述（観点ID・判定手順・単価テーブルへの言及）を仕様のインプットとして活用する
- [ ] `reacter-mig-*`/`springer-mig-*` エージェント内部の個別ルールファイル参照の是正（上記が着手されるタイミングで併せて対応）
- [ ] `springer-prohibited.md`（単一の後継ファイルなし）・`springer-mig-security.md` 内 `springer-web-patterns.md`（マッピング不明瞭）の扱い確定
- [ ] `detailed-design-review-frontend/SKILL.md` の AG-Grid チェック項目（A-G1/A-G2）— 後継ルールファイルの新設 or 項目の廃止をプロダクト側で判断する
