# 全体アーキテクチャ概要（Springer 版）

## システム構成

```
[ブラウザ]
   │
   ├───────────────┬─────────────────────────────┐
   │               │                             
   ▼               ▼                             
[US-MPA]      [Reacter]（frontend/ 一般ユーザー向け・管理者向け）
（@Controller + Thymeleaf）  │
   │               │ REST
   │               ▼
   │           [US-API]（@RestController・一般ユーザー向け・管理者向け）
   │               │
   │ REST          │ REST
   ▼               ▼
[BS]  ←────────────┘
（@RestController + @Transactional + MyBatis）
   │                                        ▲
   │ JDBC                                   │ REST（batch → BS 経由の場合）
   ▼                                        │
[DB（RDB）] ←───────────────── JDBC ────[batch]（CommandLineRunner・cron 等から起動）
```

## アプリ種別と責務

| 種別 | 役割 | 主要技術 |
|---|---|---|
| BS | DB アクセス・ビジネスロジックの中心 | Springer + Spring Boot + MyBatis |
| US-MPA | Thymeleaf による画面描画 | Springer + Spring Boot + Thymeleaf |
| US-API | SPA 向け JSON API（一般ユーザー向け・管理者向けの両方を含む。認証・認可はロール・権限ベースで同一スタック内で分ける） | Springer + Spring Boot |
| batch | 定期実行・大量データ処理（DB 直アクセスまたは BS API 経由） | Springer + Spring Boot |
| Reacter（`frontend/`） | SPA フロント（一般ユーザー向け・管理者向けの両方を含む） | Reacter フレームワーク |

## 技術スタック

| レイヤー | 技術 |
|---|---|
| Java | 25（Spring Boot 4） |
| フレームワーク（バックエンド） | Springer 3（Spring Boot 4） |
| フレームワーク（フロントエンド） | Reacter（React 19 / TypeScript ベース） |
| O/R マッパー（BS） | MyBatis（XML Mapper） |
| テンプレートエンジン（US-MPA） | Thymeleaf |
| データベース | RDB（プロダクト要件に応じて選定） |
| ビルドツール | Maven または Gradle（プロダクトごとに選定） |
| CI/CD | プロダクト要件に応じて選定 |

> ※ 実プロダクトのバージョン・選定結果はパッケージごとの `CLAUDE.md` および `pom.xml` / `build.gradle` で宣言する。
> ※ 「データベース」行の DB 製品（DB種別・DB方言）は **SA 工程（`.claude/orchestrators/issue-to-requirement.md` Step 1.5）の壁打ちで確定し、本行を確定値で直接更新する**（案件・プロダクト全体で1回だけ決定。要件IDごとに決め直さない）。プレースホルダー「RDB（プロダクト要件に応じて選定）」のままの場合は未確定であることを示す。UI 工程（`issue-to-design.md`）・SS 工程以降は本行を読み込み専用で参照し、DB 製品を再度選定しない（ウォーターフォール）。
> ※ 「フレームワーク（バックエンド）」「フレームワーク（フロントエンド）」行も同様に **SA 工程 Step 1.5 で確定**する。本行はプロダクトのデフォルト方針（Spring Boot 4／React 19）を示すが、`.claude/rules/product-rules.md`「1 プロダクト内で Springer 2 と 3 を混在させてよい」の規定により、**スタックごとに異なるバージョンを個別の `CLAUDE.md` で上書きしてよい**（本表はデフォルト値であり全スタック一律を強制しない）。各スタックの確定状況は当該スタックの `CLAUDE.md`「フレームワーク・バージョン」節（`← TBD（確定時に更新）` の有無）を正とする。

## API 設計方針

- RESTful API（BS / US-API 共通）
- バージョニング: URL パスに含める（`/api/v1/...`）
- 認証: トークンベース（プロダクト要件に応じて方式選定）
- レスポンス形式: JSON
- 例外時の共通レスポンス形式は `docs/architecture/us-bs-relationship.md` を参照

## 環境構成

| 環境 | 用途 |
|---|---|
| local | 開発環境 |
| staging | 結合テスト・受け入れテスト |
| production | 本番環境 |

## 関連ドキュメント

- パッケージ依存関係: `docs/architecture/stack-dependency.md`
- US/BS 連携詳細: `docs/architecture/us-bs-relationship.md`
- Springer 規約: `.claude/rules/springer-*.md`
