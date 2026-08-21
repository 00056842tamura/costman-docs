# ADR-005: `/unit-test-all` を `/springer-unit-test-gen` に改名

## 目的（この対応で達成したいこと）
- **Why（なぜ改名するか）**: `/unit-test-all`の「all」は「Controller/Service/Repositoryの全レイヤー」を指す（`/unit-test-all`の実際の対象は`bs`/`us-mpa`/`us-api`/`admin-us-api`/`batch`の5バックエンドスタックのみで、`frontend`は含まれない）。しかし名称だけを見ると「全スタック（frontend含む）」と誤解されやすい。ユーザーからの指摘により、Reacter側の対をなすスキル`/reacter-unit-test-gen`と対称的な命名（`/springer-unit-test-gen`）にすることで、この誤解を解消できることを確認した。
- **What（何を変更するか）**: `.claude/skills/unit-test-all/`を`.claude/skills/springer-unit-test-gen/`に改名し、`SKILL.md`のfrontmatter `name`を`springer-unit-test-gen`に変更する。リポジトリ内の現行ドキュメント・スキルにある`/unit-test-all`への参照を全て`/springer-unit-test-gen`に更新する。
- **How（どう漏れなく反映するか）**: `grep`でリポジトリ全体の`unit-test-all`参照を全件抽出し、各参照元を「現行ドキュメント（更新対象）」と「過去の記録・非参照ドラフト（対象外・不可侵）」に分類した上で、対象を機械的に一括更新する。更新後、`unit-test-all`の残存参照が「対象外」に分類したファイルのみであることを`grep`で再確認する。

## メタ情報
| 項目 | 値 |
|---|---|
| 日付 | 2026-07-08 |
| ステータス | 対応計画確定・実装はこのADR作成後に本ブランチで実施 |
| 対象 Issue | N/A（案件に属さない機構メンテナンス） |
| 起票 Step | 工程外（ADR-004の議論中にユーザーから提案があり検討） |
| 関連 ADR | ADR-003（`/unit-test-controller`系の非推奨化。本対応後も`/unit-test-controller`等のプレフィックスは変更しない＝影響節参照）・ADR-004（reviewer系の重複調査。本対応の対象外） |

## 背景

### ① 状況
ADR-004（Skill/Agent同名ペアの重複調査）に関するやり取りの中で、`/reacter-unit-test-gen`と対をなすバックエンド側のスキル名が`/unit-test-all`であることについて、ユーザーから「`all`という名前だとfrontendも含む印象を与える。`reacter-unit-test-gen`に合わせて`springer-unit-test-gen`にすべきでは」という提案があった。

### ② 調査結果
- `/unit-test-all`の実際の対象スタックは`bs`/`us-mpa`/`us-api`/`admin-us-api`/`batch`のみで、`frontend`は`/reacter-unit-test-gen`が別途担当する。「全レイヤー（Controller/Service/Repository）」の意であり「全スタック」ではないため、名称の「all」は誤解を招く。
- `springer-unit-test-gen`という名称は現時点でリポジトリ内に存在せず、命名の衝突はない。
- このリポジトリには既存の`springer-*`命名の慣習（`springer-bs`・`springer-us-api`・`springer-us-mpa`・`springer-scaffold`・`springer-review`・`springer-review-fix`）があり、`springer-unit-test-gen`はこの慣習にも整合する。
- `grep`でリポジトリ全体の`unit-test-all`参照を全件抽出した結果、27ファイルが該当した。このうち、過去の記録として現状を保持すべきファイル（履歴不可侵の原則）と、現行ドキュメントとして更新すべきファイルを以下の通り分類した。

**更新対象（19ファイル）**

| ファイル | 対象箇所 |
|---|---|
| `.claude/skills/unit-test-all/SKILL.md` | ディレクトリ自体を`.claude/skills/springer-unit-test-gen/`へ改名・frontmatter `name`更新 |
| `CLAUDE.md` | §4手順（L82）・§6スキル表（L182・L217） |
| `DEVELOPER_GUIDE.md` | §3ツリー（L100・L112・L115・L121のディレクトリ名自体）・§6全体像早見表（L389）・§6.8.4/6.8.5（L660・L673）・§6.10現行スキル列挙（L1018）・§8.2エージェント種類・表（L337・L343・L350-352）・§8.2共通スキル表（L1260）・§8.3スキル一覧（L1284）・§8.3エージェント一覧（L1305-1307） |
| `docs/templates/README.md` | 生成タイミング表（L17・L18・L69-72） |
| `docs/templates/40_基本設計/テストシナリオ.md` | 工程・生成物対応表（L84・L96） |
| `docs/onboarding/claude-code-guide.md` | 導入ガイド（L33・L70） |
| `.github/ISSUE_TEMPLATE/pt.md` | PT issueテンプレート対応フロー（L38） |
| `docs/test/README.md` | 位置づけ説明（L20） |
| `.claude/skills/reacter-unit-test-gen/SKILL.md` | 「Springer側の対応スキル」への言及（L8） |
| `.claude/skills/consistency-check/SKILL.md` | 前工程スキルの言及（L197） |
| `.claude/skills/rework-guide/SKILL.md` | 再開手順（L129） |
| `.claude/skills/issue-plan/SKILL.md` | 次工程誘導（L111） |
| `.claude/skills/issue-init/SKILL.md` | 次のアクション誘導（L150） |
| `.claude/skills/blackbox-test-gen/SKILL.md` | 並行実行スキルの言及（L104） |
| `_templates/bs-template/CLAUDE.md`・`us-api-template`・`us-mpa-template`・`batch-template`・`admin-us-api-template`（各CLAUDE.md） | 「テスト生成」欄の関連スキル列挙 |
| `DEVELOPER_GUIDE_proposal_A_新構成.md`・`DEVELOPER_GUIDE_proposal_B_マージ構成.md` | ※追記（下記「追記」節参照）。当初は対象外としたが、ユーザー指示により参考時の不整合防止のため更新対象に変更 |

**対象外・不可侵（履歴・非参照ドラフトのため変更しない）**

| ファイル | 対象外の理由 |
|---|---|
| `docs/changelog/work-logs/*.md`（全日付・archive含む） | 作業ログは各時点の記録であり、当時の正しい名称（`unit-test-all`）を保持する（履歴不可侵） |
| `docs/changelog/CHANGELOG.md` | 過去の変更履歴エントリ。改名前の状態を記録した過去のエントリのため書き換えない |
| `docs/decisions/ADR-002-pt-unit-test-coverage-gaps.md` | 過去のADR。起票時点で正しかった名称を保持する（ADR-050・ADR-051で確立済みの「履歴の不可侵」原則に従う） |
| `DEVELOPER_GUIDE.md` L445・L797 | L445は「### ステップ別フロー早見表（参考・旧 Step 0〜12 ベース／全工程は §6.2〜§6.8.5 へ移行済み）」セクション（L423〜L462）内、L797は「### （旧）Phase内サイクル: Step 4〜11【🚧 全移行済み・参考】」セクション（L689〜L904）内。いずれも旧ワークフロー（Phase番号ベース）の参考記録として明示的に維持されている箇所であり、CLAUDE.md §4の「旧 Step 0〜12 ベースの記述は参考として一部残存」という既定方針に従い変更しない（※L797は当初調査で見落とし。下記「追記」節参照） |

### ③ 制約
- ADR-003で`/unit-test-controller`・`/unit-test-service`・`/unit-test-repository`は非推奨化したが、まだ`unit-test-`プレフィックスを維持している（削除していない）。本対応は`/unit-test-all`のみの改名であり、これらのプレフィックスには手を加えない（ADR-004の選択肢B/Dが採用されれば将来的に整理される想定）。
- `.claude/skills/unit-test-all/SKILL.md`の本文自体（Step構成・実装ルール）は変更しない。frontmatterの`name`とディレクトリ名のみを変更する（機能・挙動は変えない、名称のみの変更）。

## 決定事項
1. `.claude/skills/unit-test-all/`を`.claude/skills/springer-unit-test-gen/`に改名し、`SKILL.md`のfrontmatter `name: unit-test-all`を`name: springer-unit-test-gen`に変更する。
2. 上記「更新対象（19ファイル）」の全箇所で`/unit-test-all`という表記を`/springer-unit-test-gen`に置き換える。
3. 上記「対象外・不可侵」に分類したファイルは変更しない。
4. 変更後、リポジトリ全体を`grep`し、`unit-test-all`の残存参照が「対象外・不可侵」に分類したファイルのみであることを確認する（論理破綻チェック）。

## 理由
- 名称の誤解（「all」がfrontend含む印象を与える）は実害（ユーザーの疑問）として既に発生しており、修正の必要性は明確。
- `springer-unit-test-gen`という名称は衝突がなく、既存の`springer-*`命名慣習・`reacter-unit-test-gen`との対称性の両方に合致するため、代替案を検討する必要はないと判断した。
- 更新対象と対象外の分類は、ADR-050・ADR-051で確立した「履歴（work-log・CHANGELOG・過去ADR）は書き換えない」「参照されない別案ドラフトは対象外」という既存の原則をそのまま適用した。

## 影響
- ユーザーが`/unit-test-all`という名前で呼び出すワークフロー（DEVELOPER_GUIDE.mdの手順・issue-init等の誘導文言）は、今後`/springer-unit-test-gen`という名前になる。機能・生成物・入出力仕様は変更しない（名称のみの変更のため非破壊的）。
- `.claude/agents/{controller,service,repository}-test-gen.md`は`/unit-test-all`という名称を本文中で参照していない（Step2の呼び出し元スキル名を明記していない構造のため）ため、Agent側の修正は不要。
- ADR-003で非推奨化した`/unit-test-controller`系との名称ファミリーの非対称性（`springer-unit-test-gen`と`unit-test-controller`が並存）は残る。ADR-004の結論が出るまでの一時的な状態として許容する。

## 未確定事項（バックログ）
- ADR-004で選択肢B/Dが採用された場合、`/unit-test-controller`系も`springer-*`または`{layer}-test-gen`系の命名に統一されるべきかは、ADR-004の議論結果を踏まえて別途検討する。
- 本対応はリポジトリ内の参照のみを対象とした。ユーザー・チームメンバーが個人的なメモ・外部ドキュメントで`/unit-test-all`という名前を記録している場合、そちらは本対応の範囲外（周知が必要な場合は別途対応）。

## 対応計画（実装詳細）
「背景②調査結果」の「更新対象（19ファイル）」表に記載の全箇所を機械的に置換する。手順:

1. `.claude/skills/unit-test-all/SKILL.md`を読み込み、frontmatterの`name`を変更した上で`.claude/skills/springer-unit-test-gen/SKILL.md`として新規作成し、元の`.claude/skills/unit-test-all/`ディレクトリを削除する。
2. 残り18ファイルの該当箇所（表に記載の行）を、`/unit-test-all` → `/springer-unit-test-gen`（DEVELOPER_GUIDE.md §3ツリーの`unit-test-all/`はディレクトリ名として`springer-unit-test-gen/`に）に置換する。
3. 置換後、`grep -rn "unit-test-all"`をリポジトリ全体に実行し、残存箇所が「対象外・不可侵」表のファイルのみであることを確認する。
4. `.claude/skills/springer-unit-test-gen/SKILL.md`の内容が改名前の`.claude/skills/unit-test-all/SKILL.md`と（frontmatterの`name`以外）完全一致することを確認する（機能を変えていないことの裏付け）。

### 対象外（変更しない）
- `.claude/agents/{controller,service,repository}-test-gen.md`（`/unit-test-all`という名称を本文中で参照していないため変更不要）。
- ADR-003で非推奨化した`/unit-test-controller`・`/unit-test-service`・`/unit-test-repository`・`/unit-test-controller-validation`（本対応の対象外。ADR-004の結論待ち）。

### 検証（MUST・実施必須）
- 改名後、`grep -rn "unit-test-all"`で残存箇所を再確認し、「対象外・不可侵」表と完全一致することを確認する。
- `.claude/skills/springer-unit-test-gen/SKILL.md`と改名前のバックアップ内容をdiffし、frontmatterの`name`行以外に差分がないことを確認する。
- DEVELOPER_GUIDE.md §3のディレクトリツリー表記と、実際の`.claude/skills/`配下のディレクトリ構成が一致することを確認する。

## セルフレビュー（8観点チェック）

1. **中核具体性**: ✅解消。更新対象19ファイル・対象外5カテゴリを実際の行番号まで具体的に列挙し、「改名する」という抽象的な決定で終わらせていない。
2. **網羅性（実態照合）**: ✅解消。`grep`でリポジトリ全体（27ファイル）を実際に検索し、各ファイルの該当行を`Read`で確認した上で更新対象・対象外を分類した。DEVELOPER_GUIDE.md内の「旧」セクション（L445）も見出しを確認し対象外と判定した。
3. **既存分岐の保持**: ✅解消。ADR-003が非推奨化した`/unit-test-controller`系の名称は変更対象に含めず、既存の分岐（非推奨だが削除していない状態）を維持した。
4. **決定権者の明示**: ✅解消。改名自体はユーザーからの直接指示があり、対象範囲の分類（履歴不可侵の原則適用）はADR-050・051で確立済みの基準をそのまま適用したため、新たな独断判断は発生していない。
5. **逆依存の確認**: ✅解消。`.claude/agents/{layer}-test-gen.md`が`/unit-test-all`という名称を参照しているか実際に確認し、参照していないため変更不要と判定した。
6. **境界条件**: ✅解消。「現行ドキュメントか、過去の記録か」の境界が曖昧なケース（DEVELOPER_GUIDE.md L1018の旧セクション近くにある現行スキル列挙）を個別に確認し、現行の説明文であるため更新対象に含めると判定した。
7. **義務レベルの明示**: ✅解消。検証手順を「MUST・実施必須」として明記した。
8. **暗黙前提の再言語化**: ✅解消。「同じセクション内なら全て同じ扱い（旧/現行）のはず」という暗黙の前提を置かず、行ごとに個別確認したことを明示した。

**未解決（要確認）**: なし（本ADRは実装前提の対応計画確定が目的であり、実装後にDEVELOPER_GUIDE.md L1018のような境界的判定に誤りがあれば、レビューで確認されたい）。

## 追記（2026-07-08・実装後のレビューを受けた訂正）

### ① DEVELOPER_GUIDE.md L797の記載漏れ
- **What**: 当初調査（②調査結果）は`DEVELOPER_GUIDE.md`の対象外箇所としてL445のみを明記し、同じ「旧・参考」区分に属するL797（旧Step 8のコマンド例）を調査表に記載していなかった。ユーザーからの確認依頼により発覚した。
- **Why**: L797はL445とは別の見出しセクション（L689〜L904「### （旧）Phase内サイクル」）に属するため、grep結果の27ファイル・該当行を精査する際に、L445と同一セクションだと誤認し見落とした。
- **How**: 実装は変更しない（L797は当初からL445と同じ理由で意図的に更新対象外としていたため、コードの修正は不要）。「対象外・不可侵」表にL797を明記し、調査の記載漏れを訂正した（本ADR上記の該当行を参照）。

### ② `DEVELOPER_GUIDE_proposal_A_新構成.md`・`_proposal_B_マージ構成.md`の対象外判定の見直し
- **What**: 当初この2ファイルは「どこからも参照されない別案ドラフト」として対象外・不可侵に分類したが、ユーザーからの指示により**更新対象に変更**し、両ファイル内の`/unit-test-all`表記（ディレクトリツリーの`unit-test-all/`表記を含む）を`/springer-unit-test-gen`に置き換えた。
- **Why**: 「ドラフトで採用されていない」という位置づけ自体は変わらないが、ユーザーの指摘により「将来このドラフトを参考にする際、現行の正しいスキル名との不整合に気づかず誤って古い名称を参照してしまうリスク」が対象外判定では考慮されていなかったことが判明した。この観点は当初の「履歴不可侵」の趣旨（＝ある時点の正しい記録を後から書き換えない）とは異なる（ドラフトは特定時点の記録ではなく、いつでも参照され得る参考資料であるため）。
- **How**: 両ファイル中の`/unit-test-all`（コマンド参照）および`unit-test-all/`（ディレクトリツリー表記）を`/springer-unit-test-gen`・`springer-unit-test-gen/`にそれぞれ置換した。ドラフトとしての不採用の事実・その他の内容は変更していない。

### 決定権者の明示
①・②とも、実施内容の判断はユーザーからの直接指示に基づく（AIの自己判断による変更ではない）。①はコード変更なし・ADR記載の訂正のみ。②はユーザー指示に基づくスコープ変更を実施済み。
