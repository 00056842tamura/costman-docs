# docs/templates/ — PSK 設計標準テンプレート

このディレクトリは **PSK（設計標準ドキュメント）** を Markdown に変換したテンプレート集です。
各設計書は **工程モデル（工程0 + 8工程 + リリース前ゲート）** の各工程で、対応する**直接生成スキル**が `docs/` 配下へ直接出力します（旧 Step / `/documentation` による specs→docs 反映は廃止）。

> 工程モデルの詳細は `CLAUDE.md` §4・`README.md` §5 を参照。8工程 = SA 要件定義 / UI 基本設計 / SS-Plan 詳細設計計画 / SS 詳細設計 / PG-Plan 実装計画 / PG 実装 / UT-Plan 単体テスト計画 / UT 単体テスト。

> **フォルダ構成の注意**: `docs/templates/{PSKフェーズ}/` のフォルダ名は PSK 標準の文書分類（30_要件定義〜80_ユーザ検証）だが、原則として**物理配置も実際の生成工程に合わせている**（例: SS 工程で生成する文書は `50_詳細設計/` に置く）。旧版では PSK 分類のみを優先し `60_製造/` に SS 生成の文書を置いていたため「★ SS 工程に移管」等の注記で弁明する状態になっていたが、今回の再編で解消した。**60_製造フォルダは廃止**（PG 工程は `springer-scaffold`/`reacter-code-gen` によるコード生成のみで docs を生成しないため、実際にPG/PG-Planで生成される文書が存在しなかった）。同時に削除された `プログラムテスト仕様書.md`・`_バッチ.md` は、この「PGで生成されない」という理由とは別に「実質未使用」と判定されて削除されたが、実際にはPT工程が生成すべき単体テスト向けの正しいテンプレートであり、生成スキル側が参照していなかっただけだったため、**`70_テスト/` に復元済み**（下表参照）。復元時、テンプレ内の個別規約準拠チェック表（プログラム記述PSK観点・バッチ処理チェック）はPG工程の`/springer-review`・`/reacter-code-review`（観点台帳による規約準拠確認）と内容が重複すると判明したため削除し、backend/batch/frontend の全スタックで本テンプレを共通利用する形に統一した。

## ドキュメント格納先の原則

| 種別 | 格納先 | 工程 / 生成スキル | 対象 |
|---|---|---|---|
| 要件定義 | `docs/requirements/` | SA・`/requirement-doc-gen` | 全スタック共通（要件ID採番） |
| 基本設計 | `docs/base-design/` | UI・`/basic-design-gen` | 案件共通（要件ID→機能ID分解・採番。機能ID自体は単一スタックに閉じた単位） |
| 詳細設計 | `{スタック}/docs/detail-design/` | SS・`/detailed-design-gen`（batch は `/batch-design-gen`） | スタック別 |
| 単体テスト | `{スタック}/docs/unit-test/` | UT・`/springer-unit-test-gen` | スタック別 |
| 妥当性確認（UAT） | `{スタック}/docs/unit-test/` | PT・`/springer-unit-test-gen` Step4 | スタック別 |

`{スタック}` は `us-api` / `bs` / `frontend` / `us-mpa` / `batch` のいずれか。  
`{要件ID}` は `docs/requirements/要件一覧.md` に定義された要件 ID（`R001`・3桁ゼロ埋め・単一連番・SA で採番・業務要求単位）。  
`{機能ID}` は `docs/base-design/機能一覧.md` に定義された機能 ID（`{カテゴリ}-{3桁連番}`。例: `bs-001`。カテゴリ〔`bs`/`batch`/`us-intra`/`us-inter`/`front-intra`/`front-inter`〕ごとに独立した3桁ゼロ埋め連番・UI で採番・要件ID×スタック×API/画面/ジョブ単位。**1機能ID=1API/1画面/1ジョブ**）。既存の API ID・ジョブID は廃止せず、機能ID との対応関係を対応表（機能一覧.md 等）で管理する。

> **直接生成モデル:** 成果物は上表の `docs/` 配下へ直接生成します。`/documentation`（specs→docs 反映スキル）は**廃止**しました。設計WB・ADR・レビューレポート等の検討メモは `specs/{案件キー}/{工程dir}/{issue_id}/`（SS 以降は `{スタック}/specs/{案件キー}/{工程dir}/{issue_id}/`。docs/ と同様に git 管理・push 対象）に置きます。**実装対象クラス一覧・テストシナリオ・テーブル定義書は UI 工程の正式成果物**であり、specs への退避・検討メモ化はしません（UI issue 単位・案件横断で累積）。

## ファイル管理の区分

| 区分 | 意味 |
|---|---|
| **共通1ファイル** | `docs/requirements/`・`docs/base-design/` に案件共通で1ファイル |
| **スタック別1ファイル** | `{スタック}/docs/` に1ファイル（機能追加で縦に伸びる） |
| **スタック別・機能別** | 機能ごとに `_{機能ID}` 付きのファイルを作成 |
| **検討メモ（specs）** | `specs/{案件キー}/{工程dir}/{issue_id}/`（SS 以降は `{スタック}/specs/{案件キー}/{工程dir}/{issue_id}/`）に保存（`{スタック}/docs/` には格納しない。docs/ と同様に push 対象） |

## テンプレート一覧とワークフロー対応

> 「生成タイミング」は工程（SA/UI/SS/PT）と**直接生成スキル**を示します。各スキルが「生成先」へ直接出力します（`/documentation` は廃止）。

| PSK フェーズ | 設計書名 | 生成先 | ファイル管理 | 生成タイミング（工程 / スキル） | 最終更新タイミング | 備考 |
|---|---|---|---|---|---|---|
| **30_要件定義** | `業務概要.md` | `docs/requirements/業務概要.md` | 共通1ファイル（固定） | SA・`/requirement-doc-gen` | 業務範囲変更時（後続 SA） | |
| | `システム全体図.md` | `docs/requirements/システム全体図.md` | 共通1ファイル（固定） | SA・`/requirement-doc-gen` | アーキテクチャ変更時（後続 SA） | Mermaid 図テンプレート |
| | `システム化業務フロー.md` | `docs/requirements/システム化業務フロー.md` | 共通1ファイル（固定） | SA・`/requirement-doc-gen` | 業務フロー変更時（後続 SA） | スイムレーン図テンプレート |
| | `要件一覧.md` | `docs/requirements/要件一覧.md` | 共通1ファイル（要件追加で累積） | 案件開始 `/case-init` で初期化・SA `/requirement-doc-gen` で R### 追記 | 要件追加 SA（差分追記） | **要件IDの採番はこのファイルで管理**（スタック別・機能ID単位への分解は UI 工程） |
| | `コード定義書.md` | `docs/requirements/コード定義書.md` | 共通1ファイル（値追加で累積） | SA・`/requirement-doc-gen` | コード値追加 SA（差分追記） | |
| | `画面レイアウト.md` | `docs/requirements/画面レイアウト.md` | 共通1ファイル（画面追加で累積） | SA・`/requirement-doc-gen` | 画面変更 SA（差分追記） | |
| | `ユースケース図.md` | `docs/requirements/ユースケース図.md` | 共通1ファイル（累積） | SA・`/requirement-doc-gen` | ユースケース追加 SA（差分追記） | システム化業務フロー.mdと相互補強（ICONIX手法） |
| | `画面遷移図.md` | `docs/requirements/画面遷移図.md` | 共通1ファイル（累積） | SA・`/requirement-doc-gen` | 画面追加 SA（差分追記） | 業務要求レベルの粗い遷移。詳細な state 遷移は UI 工程の画面状態遷移図 |
| | `帳票レイアウト.md` | `docs/requirements/帳票レイアウト.md` | 共通1ファイル（帳票追加で累積） | SA・`/requirement-doc-gen` | 帳票変更 SA（差分追記） | **帳票・PDF 出力機能がある場合** |
| **40_基本設計** | `機能一覧.md` | `docs/base-design/機能一覧.md` | 案件共通1ファイル（機能追加で累積） | UI・`/basic-design-gen`（要件ID→機能ID分解・{カテゴリ}-### 追記） | 機能追加 UI（差分追記） | **機能IDの採番はこのファイルで管理**（既存 API ID・ジョブID との対応表も兼ねる） |
| | `Web_API_IF一覧表.md` | `docs/base-design/Web_API_IF一覧表.md` | 案件共通1ファイル（API追加で累積） | UI・`/basic-design-gen`（行追記） | 機能追加 UI（差分追記） | |
| | `Web_API_IF定義書.md` | `docs/base-design/Web_API_IF定義書_{機能ID}.md` | 機能別（**1機能ID=1API**） | UI・`/basic-design-gen` | 同機能変更 UI | |
| | `シーケンス図.md` | `docs/base-design/シーケンス図_{機能ID}.md` | 機能別 | UI・`/basic-design-gen` | フロー変更 UI | |
| | `機能概要.md` | `docs/base-design/機能概要_{機能ID}.md` | 機能別 | UI・`/basic-design-gen` | 機能変更 UI | |
| | `テーブル一覧.md` | `docs/base-design/テーブル一覧.md` | 案件共通1ファイル（テーブル追加で累積） | UI・`/basic-design-gen`（テーブルID採番・行追記） | テーブル追加 UI（差分追記） | **テーブルID（T###）の採番はこのファイルで管理**（bs / batch のみ。1テーブル=1テーブルID。要件ID・機能ID単位ではない） |
| | `テーブル定義書.md` | `docs/base-design/テーブル定義書_{テーブルID}_{テーブル名（論理）}.md` | テーブル別（**1テーブル=1ファイル。bs / batch のみ**） | UI・`/basic-design-gen` | 同テーブル変更 UI | **正本はここ（カラム定義・インデックス・外部キー制約）。工程4 SS は読み込み専用**（実装時の変更は手戻りでUI issueへ）。テーブルは複数の要件ID・機能IDから参照されうるため、それらの単位のファイルではない |
| | `実装対象クラス一覧.md` | `docs/base-design/実装対象クラス一覧.md` | UI issue単位・案件横断で累積 | UI・`/basic-design-gen` | UI issue ごと（差分追記） | Controller/Service/Repository等のクラス一覧・責務 |
| | `テストシナリオ.md` | `docs/base-design/テストシナリオ.md` | UI issue単位・案件横断で累積 | UI・`/basic-design-gen` | UI issue ごと（差分追記） | 受け入れ条件カバレッジ・UT/APIテスト観点・E2Eシナリオ。PT工程が入力として使う |
| | `ロバストネス図.md` | `docs/base-design/ロバストネス図_{要件ID}.md` | 要件ID別（**1要件ID=1ロバストネス図**。要件ID=ユースケース単位） | UI・`/basic-design-gen` | 同要件変更 UI | Boundary/Control/Entity分析（ICONIX手法）。シーケンス図・実装対象クラス一覧への橋渡し |
| | `クラス図.md` | `docs/base-design/クラス図.md` | 案件共通1ファイル（累積） | UI・`/basic-design-gen` | Entity追加 UI（差分追記） | Entity中心のドメインモデルに限定（実装クラスは実装対象クラス一覧の管轄） |
| | `画面状態遷移図.md` | `docs/base-design/画面状態遷移図_{機能ID}.md` | 機能別（**frontend のみ**） | UI・`/basic-design-gen` | 同機能変更 UI | 画面の state 設計軸。コンポーネント仕様書の State 定義と対応 |
| | `ファイルレイアウト一覧.md` | `docs/base-design/ファイルレイアウト一覧_{機能ID}.md` | 機能別 | UI・`/basic-design-gen` | ファイル仕様変更 UI | **ファイル入出力がある場合** |
| | `非機能共通設計.md` | `docs/base-design/非機能共通設計.md` | 案件共通1ファイル（累積・バックエンド/フロントエンド2セクション） | UI・`issue-to-design`（Step1.6の壁打ち）→ `/basic-design-gen` | 非機能決定追加 UI（差分追記。初回登場スタック区分はフル生成） | アプリ全体で1回だけ決める非機能事項（認証方式全体・SecurityConfig方針・アプリ名称・ブラウザタイトル・ルーティング全体構成等）。更新履歴に対象区分（バックエンド/フロントエンド）を持つ |
| **50_詳細設計** | `ファイル定義書.md` | `{スタック}/docs/detail-design/ファイル定義書_{機能ID}.md` | スタック別・機能別 | SS・`/detailed-design-gen` | ファイル仕様変更 SS | **ファイル入出力がある場合** |
| | `外部IF定義書.md` | `{スタック}/docs/detail-design/外部IF定義書_{機能ID}.md` | スタック別・機能別 | SS・`/detailed-design-gen` | 外部IF変更 SS | |
| | `プログラム仕様書.md` | `{スタック}/docs/detail-design/プログラム仕様書_{機能ID}.md` | スタック別・機能別 | SS・`/detailed-design-gen` | 同機能変更 SS | コード生成前に生成（実装の指針） |
| | `メッセージ一覧.md` | `{スタック}/docs/detail-design/メッセージ一覧.md` | スタック別1ファイル（メッセージ追加で累積） | SS・`/detailed-design-gen` | メッセージ追加 SS | コード生成前に生成。**全スタック共通テンプレート**（backend: messages.properties／frontend: message.ts。反映方法・ID命名規約はスタックにより異なる） |
| | `ジョブネット一覧.md` | `batch/docs/detail-design/ジョブネット一覧.md` | スタック別1ファイル（ジョブ追加で累積） | SS・`/batch-design-gen` | バッチ変更 SS | **batch/ スタックのみ** |
| | `ジョブフロー一覧.md` | `batch/docs/detail-design/ジョブフロー一覧_{機能ID}.md` | スタック別・機能別（**1機能ID=1ジョブ**） | SS・`/batch-design-gen` | バッチ変更 SS | **batch/ スタックのみ** |
| | `コンポーネント仕様書.md` | `frontend/docs/detail-design/コンポーネント仕様書_{機能ID}.md` | frontend・機能別（**1機能ID=1画面**） | SS・`/detailed-design-gen` | 同機能変更 SS | **frontend のみ・PSK 標準外（プロジェクト独自）** |
| | `画面アクション遷移図.md` | `frontend/docs/detail-design/画面アクション遷移図_{機能ID}.md` | frontend・機能別（**1機能ID=1画面**） | SS・`/detailed-design-gen` | 同機能変更 SS | **frontend のみ・PSK 標準外（プロジェクト独自）。コンポーネント仕様書の「画面遷移」「アクション定義」表を可視化する補完資料（置き換えではない）** |
| | `共通設計書.md`（テンプレは `共通設計書_backend.md`／`共通設計書_frontend.md` に分離） | `{スタック}/docs/detail-design/共通設計書.md` | スタック別1ファイル（累積・機能IDに紐付かない例外） | SS・`/detailed-design-gen`（当該スタックで最初に着手するSS issueが初版生成） | 上流`非機能共通設計.md`更新時（差分追記。Ver比較で判定） | 全backend系スタック（bs/us-api/us-mpa/batch）・frontend対象。PSK 標準外（プロジェクト独自）。`docs/base-design/非機能共通設計.md`の該当セクションを反映 |
| **70_テスト** | `プログラムテスト仕様書.md`（bs/us-api/us-mpa/frontend）・`プログラムテスト仕様書_バッチ.md`（batch） | `{スタック}/docs/unit-test/テスト仕様書_{機能ID}.md` | スタック別・機能別 | PT・`/springer-unit-test-gen`（backend/batch）・`/reacter-unit-test-gen`（frontend） | テスト実施中（手動記入） | クラス/ジョブ/コンポーネント単位。2026-07 に一度`60_製造`廃止時に誤って削除され復元。backend/frontend で同一テンプレを共通利用（個別の規約準拠チェック表は復元時に削除。PG工程のレビュースキルと重複するため） |
| | `テスト仕様書.md` | *(現時点で参照する生成スキルなし)* | — | — | — | 結合テスト・システムテスト・運用テスト向けのシナリオベーステンプレート（テスト工程欄で選択）。8工程モデルのPTでは使用しない（対象は`プログラムテスト仕様書.md`）。これらの工程を担う生成スキルが将来追加された場合に使用する想定 |
| | `テスト計画書.md` | `{スタック}/docs/unit-test/テスト計画書.md` | スタック別1ファイル（固定） | PT・`/springer-unit-test-gen`・`/reacter-unit-test-gen` | テスト計画変更時（後続 PT） | |
| | `機能要件対比表.md` | `{スタック}/docs/unit-test/機能要件対比表.md` | スタック別1ファイル（機能追加で累積） | PT・`/springer-unit-test-gen`・`/reacter-unit-test-gen` | PT（差分追記） | |
| **80_ユーザ検証** | `妥当性確認実施票.md` | `{スタック}/docs/unit-test/妥当性確認実施票_{機能ID}.md` | スタック別・機能別 | *(現時点で参照する生成スキルなし。人間が作成)* | PT 完了後、リリース前ゲートまでの任意タイミングで作成（PT PR には含めない） | リリース判定の根拠資料。PT の責務ではない |

## 生成のしくみ（直接生成モデル）

各設計書は工程ごとの**直接生成スキル**がこのテンプレート群を参照し、対応する `docs/` 配下へ**直接生成**します。手動 `cp` での初期コピーや `/documentation` による specs→docs 反映は行いません。

| 工程 | 生成スキル | 主な生成先 | 生成される設計書 |
|---|---|---|---|
| 工程1 SA（要件定義） | `/requirement-doc-gen`（案件開始は `/case-init` が `要件一覧.md`・`docs/base-design/機能一覧.md` を初期化） | `docs/requirements/` | 業務概要・システム全体図・システム化業務フロー・要件一覧・コード定義書・画面レイアウト・ユースケース図・画面遷移図・〔帳票レイアウト〕 |
| 工程2 UI（基本設計） | `/basic-design-gen`（要件ID→機能ID分解・採番） | `docs/base-design/` | 機能一覧（機能ID採番）・機能概要・Web_API_IF定義書・シーケンス図・Web_API_IF一覧表・テーブル一覧（テーブルID採番）・テーブル定義書（bs/batch・テーブル単位）・実装対象クラス一覧・テストシナリオ・ロバストネス図・クラス図・画面状態遷移図（frontend）・非機能共通設計・〔ファイルレイアウト一覧〕 |
| 工程4 SS（詳細設計） | `/detailed-design-gen`（batch は `/batch-design-gen`） | `{スタック}/docs/detail-design/` | 外部IF定義書・プログラム仕様書・メッセージ一覧・共通設計書・〔ファイル定義書〕・〔ジョブネット一覧/ジョブフロー一覧〕・〔コンポーネント仕様書（frontend）〕（テーブル一覧・テーブル定義書は UI 工程が正本のため生成しない） |
| 工程6 PG（実装） | `/springer-scaffold`（frontend は `/reacter-code-gen`） | *(docs 生成なし)* | コードのみ `{スタック}/src/main/`（プログラム仕様書・メッセージ一覧は SS 工程生成済み） |
| 工程8 PT（単体テスト） | `/springer-unit-test-gen`（backend/batch。＋`/blackbox-test-gen`）／`/reacter-unit-test-gen`（frontend） | `{スタック}/docs/unit-test/` | テスト仕様書（全スタック共通・プログラムテスト仕様書型）・テスト計画書・機能要件対比表 |

- 既存ファイルがある場合、生成スキルは**差分追記**します（他機能の記述は消しません）。
- テストシナリオ（テスト観点）は UI 工程が `docs/base-design/テストシナリオ.md` へ直接生成し、UT 工程が入力として `{スタック}/docs/unit-test/テスト仕様書_{機能ID}.md` に詳細化します。

## 生成先ディレクトリ

| ディレクトリ | 用途 | 生成スキル |
|---|---|---|
| `docs/requirements/` | 要件定義成果物（全スタック共通） | `/requirement-doc-gen` |
| `docs/base-design/` | 基本設計成果物（案件共通・機能別。テーブル一覧・テーブル定義書（テーブル単位）・実装対象クラス一覧・テストシナリオ・ロバストネス図・クラス図・画面状態遷移図・非機能共通設計を含む） | `/basic-design-gen` |
| `{スタック}/docs/detail-design/` | 詳細設計書・プログラム仕様書・メッセージ一覧・共通設計書（スタック別・機能別。共通設計書のみ機能IDに紐付かないスタック単位） | `/detailed-design-gen`・`/batch-design-gen` |
| `{スタック}/docs/unit-test/` | テスト計画・仕様・機能要件対比表（スタック別・機能別） | `/springer-unit-test-gen` |
| `{スタック}/docs/unit-test/`（妥当性確認実施票のみ） | リリース判定の根拠資料（スタック別・機能別。人間が作成・生成スキルなし） | — |

> 工程モデル・各スキルの詳細は `CLAUDE.md` §4・`README.md` §5 を参照。

## 参照元

PSK オリジナルファイル（Excel / PDF）は `../../../PSK（設計標準ドキュメント）/` を参照してください。
