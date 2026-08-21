# デプロイ手順

## 環境一覧

| 環境 | URL 例 | 用途 | 主なブランチ |
|---|---|---|---|
| 開発（DEV） | https://dev-{product}.example.com | 開発・動作確認 | develop |
| ステージング（STG） | https://stg-{product}.example.com | リリース前検証 | release/* |
| 本番（PRD） | https://{product}.example.com | 本番運用 | main |

URL はプロダクトごとに異なるため、各プロダクトの運用ドキュメントで確認してください。

## デプロイフロー

```
develop ──> release/x.y.z ──> main
   ↓               ↓             ↓
  DEV          STG           PRD
```

## 影響スタック単位のデプロイ

PR の影響スタックに応じて、対応するアプリのみをデプロイします。

| 変更スタック | デプロイ対象 |
|---|---|
| `bs/` のみ | BS アプリのみ |
| `us-api/` のみ | US-API のみ |
| `bs/` + `us-api/` | 両方を順番にデプロイ（**BS → US の順**） |

> 🔴 BS の API インターフェース変更がある場合、必ず BS を先にデプロイしてから US/Reacter をデプロイすること。

## 各環境へのデプロイ手順

### 開発環境（DEV）

`develop` ブランチに push すると CI/CD パイプラインが自動デプロイ。

```bash
$ git checkout develop
$ git merge feature/{案件キー}
$ git push origin develop
```

CI は影響パッケージを検出して該当アプリのみビルド・デプロイします。

### ステージング環境（STG）

1. `release/x.y.z` ブランチを作成
   ```bash
   $ git checkout -b release/1.2.0 develop
   $ git push origin release/1.2.0
   ```
2. CI/CD のステージングジョブが起動
3. リリーステスト実施

### 本番環境（PRD）

1. ステージングでの検証完了後、`main` へマージ
2. リリースタグ `vX.Y.Z` を作成
   ```bash
   $ git tag -a v1.2.0 -m "Release 1.2.0"
   $ git push origin v1.2.0
   ```
3. リリースワークフローを **手動承認**

## ロールバック手順

1. 直前の安定タグを特定
   ```bash
   $ git tag --sort=-creatordate | head -10
   ```
2. リリースワークフローで該当タグを再デプロイ
3. DB 変更を伴う場合は `flyway undo` または手動 DDL を適用

## チェックリスト（本番リリース前）

- [ ] ステージングでの結合テスト完了
- [ ] DB マイグレーション SQL レビュー完了
- [ ] BS API インターフェース変更がある場合、US/Reacter 側の対応完了
- [ ] リリースノート作成済み
- [ ] 関係者への事前通知済み
- [ ] ロールバック手順確認済み
- [ ] 影響パッケージ一覧と作業者の確認完了

## Reacter（frontend/）デプロイ時の注意

Reacter は別ライフサイクルでデプロイされます（`frontend/` スタック）。
US-API の互換性破壊リリース時は Reacter 側との調整が必要です。
