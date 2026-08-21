# 計画工程 plan.md（SS-Plan / PG-Plan / PT-Plan 共通）

> 計画工程（詳細設計計画 SS-Plan・実装計画 PG-Plan・単体テスト計画 PT-Plan）の作業計画。
> 配置: `specs/{案件キー}/{計画工程dir}/{issue_id}/plan.md`（`specs` は **push して通常の PR でレビュー・承認する**）。
> 粒度: **機能ID**（UI 工程で要件ID×スタック×API/画面/ジョブに分解・採番済み・並走単位）。次工程の sub-issue を先行起票する棚卸しとして使う。

## メタ情報
- 案件キー: costman-2026-001
- 計画工程: SS-Plan
- issue-id（この計画 issue）: 1
- 対象機能ID（複数可）: front-intra-001, front-intra-002, front-intra-003, front-intra-004, front-intra-005, front-intra-006, front-intra-007
- 影響スタック: frontend
- 作成日: 2026/08/21
- 最終更新日: 2026/08/21

## 分割方針
- 粒度: **機能ID**（1 行＝1 sub-issue・スタックへの分解は UI 工程で完了済み）
- 並走/順序の考え方: `front-intra-002`（ログイン画面）は認証基盤（`AuthUserContext`・`PrivateRoute`）の詳細設計を確定する起点となるため最優先で着手する。認証を前提としない `front-intra-001`（トップページ）は依存なく並走可能。ログイン以降の画面（`front-intra-003`〜`007`）はいずれも `PrivateRoute` 配下で稼働するため、認証基盤の詳細設計（`front-intra-002` の SS 成果物・共通設計書初版）確定後に着手する
- 入力: `docs/base-design/機能一覧.md`（機能ID確定済み）・`実装対象クラス一覧.md`（共通基盤クラス・画面別クラス）・`テストシナリオ.md`・各画面の `シーケンス図_front-intra-XXX.md`・`画面状態遷移図_front-intra-XXX.md`・`機能概要_front-intra-XXX.md`・`ロバストネス図_RXXX.md`・（front-intra-006/007のみ）`ファイル定義書_front-intra-XXX.md`・`ファイルレイアウト一覧.md`
- 参考情報（対象外）: `bs-001`〜`bs-009` は bs スタック未着手のため本計画の対象外（機能一覧.md 記載の通り、bs 側 SS 工程で正式化されるまでの参考情報）

## sub-issue 棚卸し（次工程の先行起票対象）

<!-- ⚠ このテーブルの列順・セクション名（## sub-issue 棚卸し）は変更禁止。
     subissue-bulk.sh --plan がこのテーブルを機械的に解析して issue body に埋め込む。
     列順: # | スタック | 機能ID | sub-issue | 成果物 | blocked-by | 優先・並走 | 備考 -->
| # | スタック | 機能ID | sub-issue（ラベル／対象リポ） | 作成/対象 成果物 | 依存（blocked-by） | 優先・並走 | 備考 |
|---|---|---|---|---|---|---|---|
| 1 | frontend | front-intra-001 | [SS-front-intra-001]／frontendリポ | コンポーネント仕様書_front-intra-001・画面アクション遷移図・共通設計書（初版） | なし | 高・並走可（パイロット対象） | 未ログインでも表示可能。認証基盤への依存なし。SS→PGパイプライン全体のパイロットとして最優先で実施 |
| 2 | frontend | front-intra-002 | [SS-front-intra-002]／frontendリポ | コンポーネント仕様書_front-intra-002・画面アクション遷移図・共通設計書（AuthUserContext等の認証基盤設計を含む） | なし | 高・並走可 | `AuthUserContext`・`PrivateRoute`・`useLogin`/`useLogout` 等、他画面が依拠する認証基盤の詳細設計を確定する起点 |
| 3 | frontend | front-intra-003 | [SS-front-intra-003]／frontendリポ | コンポーネント仕様書_front-intra-003・画面アクション遷移図 | #front-intra-002 | 中 | メニュー画面。`USER_ROLE.ADMIN`による表示制御は認証基盤設計に依拠 |
| 4 | frontend | front-intra-004 | [SS-front-intra-004]／frontendリポ | コンポーネント仕様書_front-intra-004・画面アクション遷移図 | #front-intra-002 | 中 | 工番別収支データ参照画面＋工番検索ダイアログ。`PrivateRoute`配下 |
| 5 | frontend | front-intra-005 | [SS-front-intra-005]／frontendリポ | コンポーネント仕様書_front-intra-005・画面アクション遷移図 | #front-intra-002 | 中 | コスト利用率参照画面。`front-intra-004`への遷移を含むが設計は独立可能 |
| 6 | frontend | front-intra-006 | [SS-front-intra-006]／frontendリポ | コンポーネント仕様書_front-intra-006・画面アクション遷移図 | #front-intra-002 | 低 | データ取り込み画面（一般ユーザー向け）。ファイル定義書_front-intra-006を合わせて参照 |
| 7 | frontend | front-intra-007 | [SS-front-intra-007]／frontendリポ | コンポーネント仕様書_front-intra-007・画面アクション遷移図 | #front-intra-002 | 低 | 管理者用データ取り込み画面。ファイル定義書_front-intra-007を合わせて参照。管理者権限（admin）制御は認証基盤設計に依拠 |

> 成果物の例（工程別）:
> - **SS-Plan** → `{スタック}/docs/detail-design/`（外部IF定義書・プログラム仕様書・〔ジョブネット/ジョブフロー: batch〕。テーブル定義書は UI 工程 `docs/base-design/` が正本のため対象外）
> - **PG-Plan** → `{スタック}/src/main/`（本体コードのみ。プログラム仕様書等はSS工程で生成済み）
> - **PT-Plan** → `{スタック}/docs/unit-test/` ＋ `{スタック}/src/test/`（テスト計画書・テスト仕様書・テストコード）

## 依存関係（補足）

- `front-intra-002`（ログイン画面）の SS で確定する認証基盤設計（`AuthUserContext`・`PrivateRoute`・`useLogin`/`useLogout`。`実装対象クラス一覧.md` No.14 参照）に、`front-intra-003`〜`007` の画面設計（ルートガード・権限制御の記述）が依拠するため、これら5件は `front-intra-002` を `blocked-by` とする。
- `front-intra-001`（トップページ）は未ログインでも表示可能な唯一の画面であり、認証基盤に依存しないため依存なし。ユーザー承認済みのパイロット対象として最優先で着手する。
- `front-intra-004` と `front-intra-005` は画面間遷移（`front-intra-005`→`front-intra-004`）があるが、遷移先の存在を前提とした設計自体は互いに独立して実施可能なため、`front-intra-002` 以外の相互 blocked-by は設定しない。
- 共通基盤クラス（`実装対象クラス一覧.md`「共通基盤（全画面共通）」セクション）の設計は、各SS issueが必要な範囲を都度 `共通設計書.md`（スタック単位・初版は最初に着手した issue で作成、以降差分更新）に反映する。複数issueが同じ共通クラスを扱うため、後続issueの `/detailed-design-review-frontend` で既存の共通設計書との整合（CPC-2含む）を必ず確認する。

## 先行起票の状態（gh 連動は後実装・当面は手順提示）
| sub-issue | スタックリポ | issue 番号 | ブランチ | 状態 |
|---|---|---|---|---|
| [SS-front-intra-001] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-001 | 起票待ち（本plan.mdのPRマージ後、先行起票予定・パイロット） |
| [SS-front-intra-002] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-002 | 起票待ち |
| [SS-front-intra-003] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-003 | 起票待ち |
| [SS-front-intra-004] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-004 | 起票待ち |
| [SS-front-intra-005] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-005 | 起票待ち |
| [SS-front-intra-006] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-006 | 起票待ち |
| [SS-front-intra-007] | frontend | #未起票 | feature/costman-2026-001-ss-front-intra-007 | 起票待ち |

## 承認（通常 PR でレビュー）
- [ ] 本 plan.md を含む PR を作成した（`docs-to-pr`）
- [ ] レビュワーが PR で承認した（分割・棚卸し・優先順位）
- [ ] 承認・マージ後、sub-issue を各スタックリポに先行起票し、案件 meta の対応表へ登録した

## 更新履歴
| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/21 | Mai Tamura（Claude Code） | 初版（front-intra-001〜007 の7件を棚卸し。bs系は未着手のため対象外） |
