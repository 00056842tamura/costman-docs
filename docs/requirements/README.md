# docs/requirements/

要件定義（工程1 SA）の成果物を格納するディレクトリです。

各ファイルは **工程1 SA** で `/requirement-doc-gen`（PSK30 一覧系の要件定義書群を直接生成するスキル）が、このディレクトリへ**直接生成・差分追記**します（手動 `cp` は不要）。`/requirement-doc-gen` は `.claude/orchestrators/issue-to-requirement.md` の壁打ち・`ADR-SA-{n}` 起票後に実行します。詳細は `CLAUDE.md` §4・`README.md` §5.3 を参照。

> 要件一覧は案件開始時に `/case-init` が初期化し、SA で **要件ID（R###）** を採番・追記します（`R001`・3桁ゼロ埋め・単一連番・業務要求単位）。要件IDをスタック×API/画面/ジョブ単位に分解した**機能ID（`{カテゴリ}-{3桁連番}`）** は UI 工程（`/basic-design-gen`）が `docs/base-design/機能一覧.md` に採番します（本ディレクトリでは採番しない）。

## 格納対象ファイル

| ファイル | 生成元テンプレート | 生成タイミング（工程 / スキル） | 備考 |
|---|---|---|---|
| `プロダクト情報.md` | `docs/templates/プロダクト情報.md` | 案件開始 **前**・人手でコピーして記入 | プロダクト名・パッケージ名・プロダクト略称。`/case-init` が読み込み案件メタに反映 |
| `業務概要.md` | `docs/templates/30_要件定義/業務概要.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | |
| `システム全体図.md` | `docs/templates/30_要件定義/システム全体図.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | |
| `システム化業務フロー.md` | `docs/templates/30_要件定義/システム化業務フロー.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | |
| `要件一覧.md` | `docs/templates/30_要件定義/要件一覧.md` | 案件開始 `/case-init` で初期化・工程1 SA `/requirement-doc-gen` で R### 追記 | **要件IDの採番台帳** |
| `要件定義_{要件ID}.md` | `docs/templates/30_要件定義/要件定義_{要件ID}.md` | 工程1 SA・`/requirement-doc-gen` が per-要件で直接生成 | |
| `コード定義書.md` | `docs/templates/30_要件定義/コード定義書.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | |
| `画面レイアウト.md` | `docs/templates/30_要件定義/画面レイアウト.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | |
| `ユースケース図.md` | `docs/templates/30_要件定義/ユースケース図.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | システム化業務フロー.mdと相互補強（ICONIX手法） |
| `画面遷移図.md` | `docs/templates/30_要件定義/画面遷移図.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | 業務要求レベルの粗い遷移。詳細はUI工程の画面状態遷移図 |
| `帳票レイアウト.md` | `docs/templates/30_要件定義/帳票レイアウト.md` | 工程1 SA・`/requirement-doc-gen` が直接生成 | **帳票・PDF 出力機能がある場合のみ** |

## 生成手順

工程1 SA の壁打ち（`issue-to-requirement`）と `ADR-SA-{n}` 起票の後に、`/requirement-doc-gen` を実行すると上表のファイルが `docs/requirements/` へ直接生成されます。

```
/requirement-doc-gen 案件キー: {案件キー} SA issue-id: {issue_id} 対象要件ID: {R###...}
```

既存ファイルがある場合は差分追記します（他要件の記述は消しません）。生成後に `docs-to-pr`（工程=SA・ベース `feature/{案件キー}`）で工程ゲート PR を作成します。

## ⚠️ 並行案件時の注意

② 案件リポはプロダクトにつき1つを複数案件で使い回す設計です。案件を**順番に進める限り問題は発生しません**が、**2案件が同時進行する場合**は以下のリスクがあります。

### R### 採番の競合

`要件一覧.md` はプロダクト横断の採番台帳（1ファイル）です。並行する2案件がそれぞれ SA 工程で同じ最大番号を読み取ると、同じ R### を採番してしまいます（機能ID（`{カテゴリ}-{3桁連番}`）の採番競合は UI 工程の `docs/base-design/機能一覧.md` が対象。`docs/base-design/README.md` を参照）。

**対処:**
- SA工程は1案件ずつ完了させてから次案件のSAを開始する（推奨）
- 並行が避けられない場合は採番範囲をあらかじめ分割する
  ```
  例: 案件A = R001〜R020、案件B = R021〜R040
  ```

### `業務概要.md`・`システム全体図.md`・`システム化業務フロー.md` の同時更新

これらはセクション単位で更新するファイルです。並行ブランチで同一セクションを編集すると、`feature/{案件キー}` へのマージ時にコンフリクトが発生します。

**対処:** 同じセクションを複数案件が同時に更新しないよう調整する。コンフリクトが発生した場合は両方の変更内容を保持してマージする。

## 関連ドキュメント

- テンプレート運用: `docs/templates/README.md`
- 工程1 SA の詳細フロー: `README.md` §5.3
- 工程モデル・必須手順: `CLAUDE.md` §4
- 並行案件の運用ガイドライン: `.claude/rules/github-ops.md` §1
