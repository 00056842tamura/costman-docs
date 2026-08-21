# パッケージ依存関係（Springer 版）

> **本表の更新責務**: 「許可される依存関係」表の内容更新は、SA工程（`.claude/orchestrators/issue-to-requirement.md` Step4）またはUI工程（`.claude/orchestrators/issue-to-design.md` Step1.5）で新しい依存パターンが決定された時点で直接行う。物理的なスタック構築（`/stack-init`）はUI工程完了後・SS-Planゲートで別途行われ、本表の更新タイミングとは独立している。

## 依存関係の全体像

```
[ブラウザ / Reacter（frontend/）]
        │
        ▼
[{product}-us-mpa]   [{product}-us-api]
        │                    │
        └────────┬───────────┘
                 │
                 ▼
            [{product}-bs]
                 │
                 ▼
              [DB]
```

## 許可される依存関係

| 依存元 | 依存先 | 通信手段 | 理由 |
|---|---|---|---|
| `{product}-us-mpa` | `{product}-bs` | HTTP（REST） | MPA から同プロダクトの業務処理を呼び出す |
| `{product}-us-api` | `{product}-bs` | HTTP（REST） | SPA 経由のリクエストを業務処理に橋渡し（一般ユーザー向け・管理者向けの両方を含む。物理スタックは分けない） |
| `frontend/`（Reacter） | `{product}-us-api` | HTTP（REST） | SPA から API を呼ぶ（一般ユーザー向け・管理者向けの両方を含む） |

> ✅ **共通ライブラリの Java モジュール参照**（`{product}-shared` 等）も将来的に追加予定。
> 現時点では検討段階のため、各パッケージで必要な型は個別に定義する。

## 禁止される依存関係

| 禁止パターン | 理由 |
|---|---|
| `{product}-us-*` → DB（直接アクセス） | アーキテクチャ違反（必ず BS を経由する） |
| `{product}-bs` → `{product}-us-*` | 逆方向依存（BS は US の存在を知ってはならない） |
| `{productA}-*` → `{productB}-*`（直接 import） | プロダクト間の直接依存禁止（API 経由で連携） |
| 循環依存（A→B→A） | ビルドエラーの原因 |

## パッケージ別の責務

| パッケージ | 責務 | 含めるもの |
|---|---|---|
| `{product}-bs` | DB アクセス・業務ロジックの中心 | Controller / Service（`@Transactional`）/ Repository（MyBatis Mapper） |
| `{product}-us-mpa` | MPA 画面描画 | Controller（`@Controller`）/ Service / Repository（BS API クライアント）/ Thymeleaf テンプレート |
| `{product}-us-api` | SPA 向け JSON API（一般ユーザー向け・管理者向けの両方を含む。認証・認可はロール・権限ベースで同一スタック内で分ける） | Controller（`@RestController`）/ Service / Repository（BS API クライアント） |

## 新たな依存関係を追加する場合

1. このドキュメントの「許可される依存関係」表に該当するか確認する
2. 該当しない場合は標準チームに相談する
3. 承認を得たらこのドキュメントを更新する
4. `CLAUDE.md` の禁止事項と矛盾しないか確認する

## 関連ドキュメント

- US/BS 連携の詳細: `docs/architecture/us-bs-relationship.md`
- プロダクトルール: `.claude/rules/product-rules.md`
