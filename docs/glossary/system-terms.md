# 全社共通 システム用語辞書（Springer 版）

## 読み込み指示
社内略語・システム固有名称が出てきた場合はこのファイルで確認すること。

## フレームワーク・技術

| 用語 | 正式名称・意味 |
|---|---|
| Springer | 当社製の Java フレームワーク（Spring Boot ベース） |
| Reacter | 当社製の SPA フロントフレームワーク（`frontend/` スタック。規約: `.claude/rules/reacter-*.md`） |
| Spring Boot | Spring Framework ベースの Java アプリケーションフレームワーク |
| MyBatis | XML ベースの O/R マッパー（BS で使用） |
| Thymeleaf | Java 用テンプレートエンジン（US-MPA で使用） |
| Checkstyle | Java コーディングスタイルの静的チェックツール |

## アプリ種別

| 用語 | 意味 |
|---|---|
| BS | バックエンドサービス（DB アクセス・業務ロジックの中心） |
| US | ユーザーサービス（フロントエンド側のアプリの総称） |
| US-MPA | Multi Page Application 型 US。`@Controller` + Thymeleaf |
| US-API | SPA 向け JSON API 型 US。`@RestController` |
| SPA | Single Page Application（ブラウザ側で画面遷移する構成） |
| MPA | Multi Page Application（サーバー側で HTML 生成する構成） |

## レイヤー・パターン

| 用語 | 意味 |
|---|---|
| Controller | リクエスト受け付けレイヤー（`@Controller` / `@RestController`） |
| Service | 業務ロジック実装レイヤー（インターフェース + `*Impl` のペア） |
| Repository | データアクセスレイヤー（BS: MyBatis Mapper、US: BS API クライアント） |
| Form | MPA の HTML フォームを受ける入力クラス |
| Request | JSON API のリクエストを受ける入力クラス |
| View | JSON API のレスポンスを返す出力クラス |
| Mapper | MyBatis の SQL マッピング定義（XML） |
| Validator | Spring Validator 実装クラス |

## 環境・運用

| 用語 | 意味 |
|---|---|
| DEV / LOCAL | 開発環境 |
| STG | ステージング環境 |
| PRD / PROD | 本番環境（production） |

## 認証・セキュリティ

| 用語 | 意味 |
|---|---|
| JWT | JSON Web Token（認証トークン） |
| `@Sensitive` | Springer 提供の機密フィールド注釈（マスキング対象） |
| `MaskingToStringBuilder` | Springer 提供のマスキング付き `toString` ユーティリティ |
| `ApplicationException` | Springer の独自例外基底クラス |
| `LoggerFactory.APP` | Springer の業務ログ用 Logger 取得カテゴリ |

## その他

| 用語 | 意味 |
|---|---|
| API | Application Programming Interface |
| CRUD | Create / Read / Update / Delete（基本的なデータ操作） |
| DTO | Data Transfer Object（レイヤー間データ受け渡し用クラス） |
| ER 図 | Entity Relationship Diagram（DB テーブル間関係図） |

## 更新ルール

- 新たな社内略語・正式名称が壁打ちで確定したら標準チームがここに追記する
- 既存の定義を変更する場合は全メンバーに周知する
- 最終更新日: 2026-05-11
