# 工程issue メタ情報（工程issue レベル）

> 各工程の着手（`/issue-init`）で `specs/{案件キー}/{工程}/{issue_id}/meta.md` として生成する。
> その工程issue 固有の状態を管理する（案件横断情報は `specs/{案件キー}/meta.md`）。

## 基本情報
- 案件キー: costman-2026-001
- 工程:        SS-Plan
- issue種別:   phase
- issue-id: 1
- issueタイトル: 詳細設計計画
- issueURL: https://github.com/00056842tamura/costman-docs/issues/1
- 対象要件ID:  R001〜R007（front-intra-001〜007 に対応。UI工程で分解済み）
- 対象機能ID:  front-intra-001〜007（frontendスタックのみ。bs系は未着手のため対象外）
- 担当者: Mai Tamura
- ブランチ:    feature/costman-2026-001-ssplan
- 起票日: 2026-08-21

## 影響スタック
- 直接変更: frontend/
- 間接影響: なし
- 循環依存チェック: 問題なし

## ステータス
- [x] 着手（issue起票・ブランチ作成・作業ディレクトリ準備）
- [x] 検討・壁打ち中
- [x] 成果物作成完了（plan.md・specs配下）
- [ ] 工程ゲート PR 作成済み・レビュー待ち
- [ ] マージ完了

## PR 情報
- PR URL:
- PR番号:
- マージ先: feature/costman-2026-001

## チケット連携
- 外部チケット管理ツール:
- チケット起票日:
- チケット URL:
