# 工程issue メタ情報（工程issue レベル）

> 各工程の着手（`/issue-init`）で `specs/{案件キー}/{工程}/{issue_id}/meta.md` として生成する。
> その工程issue 固有の状態を管理する（案件横断情報は `specs/{案件キー}/meta.md`）。

## 基本情報
- 案件キー:
- 工程:        SA / UI / SS-Plan / SS / PG-Plan / PG / PT-Plan / PT
- issue種別:   phase / task
- issue-id:
- issueタイトル:
- issueURL:
- 対象要件ID:  （SA/UI 工程。複数可）
- 対象機能ID:  （UI 工程で採番後・SS-Plan/計画工程は複数可。SS/PG/PT は {カテゴリ}-{3桁連番} 単位）
- 担当者:
- ブランチ:    feature/{案件キー}-{工程}[-{機能ID}]
- 起票日:

## 影響スタック
- 直接変更: frontend/ / bs/ / us-mpa/ / us-api/ / batch/
- 間接影響:
- 循環依存チェック: 問題なし / 要確認

## ステータス
- [ ] 着手（issue起票・ブランチ作成・作業ディレクトリ準備）
- [ ] 検討・壁打ち中
- [ ] 成果物作成完了（docs/ へ直接）
- [ ] 工程ゲート PR 作成済み・レビュー待ち
- [ ] マージ完了

## PR 情報
- PR URL:
- PR番号:
- マージ先: feature/{案件キー}

## チケット連携
- 外部チケット管理ツール:
- チケット起票日:
- チケット URL:
