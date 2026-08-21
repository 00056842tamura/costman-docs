---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 01 アーキテクチャ・レイヤー

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン

## 🚫 禁止 (NEVER)

### R-01-01 US アプリから DB へ直接アクセスしない
US アプリ（BFF）から DB への直接アクセスは禁止。データアクセスは必ず BS アプリの API を介する。
- 理由 / 背景: US 層はアプリケーション外部から BS/DS 層を隔離するセキュリティ上の役割を持つ。
- 条件 / 例外: US 層の業務サービス（BS 層）呼び出しは Web API コールに限る。
- 補足: US 層（BFF）は View/API の提供に加え、社外向け API の提供も担う（外部公開は US 層から行う）。

### R-01-02 レイヤーをまたぐ呼び出しをしない
レイヤーをまたぐ呼び出しは禁止（例: Controller から Repository を直接呼び出す）。
- 条件 / 例外: US 層＝層をまたぐ DS 層との直接連携を禁止 / BS 層＝他業務 DB への直接操作（他業務 DB への JDBC 接続）を禁止 / DS 層＝他業務サービスからの JDBC による DB 直接操作を禁止。

### R-01-03 Controller・Repository にビジネスロジックを書かない
ビジネスロジックは Service（BL 層）に記述し、Controller（PL 層）・Repository（DL 層）には記述しない。
- 理由 / 背景: PL 層は入出力（リクエスト/レスポンス）、DL 層はデータアクセスの担当。業務処理（判定・データ加工）は BL 層の責務。

### R-01-04 Form / Request クラスを Service で使用しない
Controller のリクエスト受付で使う Form クラス・Request クラスを Service で使用しない。
- 理由 / 背景: 各層はその層が必要とする型のクラスでデータを伝搬する（PL 受信データクラス → BL へはモデルクラス）。

### R-01-09 BS層から他業務のデータソースへ直接操作しない
BS層（Repository/DL層）から、自身が担当しない業務のデータソース（他業務 DB 等）への直接操作（JDBC 接続等）を禁止する。他業務のデータが必要な場合は、当該データを所有する BS層が提供する Web API を呼び出す。
- 理由 / 背景: 業務サービス（BS）単位でデータの所有権を明確にし、業務境界を越えた密結合を防ぐため。DS層（データ永続化）は業務データのオーナーである BS層を必ず介して連携する。

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-01-05 レイヤー間データは DTO（モデルクラス）で受け渡す
レイヤー間のデータはモデルクラス（DTO）を使用し、メソッドの引数・戻り値で受け渡す。
- 補足: 受信データクラス（PL 受信）／モデルクラス（PL→BL／BL→DL）／送信データクラス（DL 送信）を各層で定義して使用する。

### R-01-06 オンライン処理は US 層 / BS 層 / DS 層 の 3 層で分離する
オンライン処理のサーバ側アプリは US 層・BS 層・DS 層の 3 層で構成し、各層間は API 呼出・DB 接続で連携する。

### R-01-07 US 層・BS 層の内部は PL / BL / DL の 3 層アーキテクチャとする
US 層・BS 層の内部は Spring Boot による 3 層レイヤーアーキテクチャ（PL 層=Controller / BL 層=Service / DL 層=Repository）に準拠する。

### R-01-08 バッチ処理は BS 層 / DS 層 の 2 層で構成する
バッチ処理の Java アプリケーションは BS 層・DS 層の 2 層で構成し、US 層を持たない。
- 関連: バッチ設計の詳細（OS スケジューラ禁止 R-18-01・1 ジョブ 1 シェル R-18-03 等）は [springer-batch.md](springer-batch.md)。

## ✨ 推奨 (PREFER)

（なし）

## 条件付き事項

（なし）

## 参考・推奨実装パターン

### アプリケーションパターン（選択基準）

| パターン | 特徴・採用基準 |
|---|---|
| MPA | SSR(Thymeleaf)。ページ遷移ごとにサーバが新 HTML を返却。US 層セッションでデータ管理（ステートフル） |
| SPA | 初回に構成要素(HTML/JS/CSS)取得、差分データのみ取得しクライアントレンダリング。US 層セッション管理なし（ステートレス） |
| Native | カメラ/GPS/プッシュ通知などデバイス機能が必要な場合 |
| Web API | RESTful・疎結合・ステートレス。HTTP ステータス＋JSON/XML で応答 |
| バッチ | 集計/DB 連携/大量データ処理。詳細は [springer-batch.md](springer-batch.md) |

### 各層の役割（US / BS / DS）

| 層 | 役割 |
|---|---|
| US層 | ブラウザ (MPA/SPA)・ネイティブアプリ向けの View/API 提供、社外向け API 提供（[springer-api-style.md](springer-api-style.md)）、外部からの BS/DS層隔離（セキュリティ境界。R-01-01 の背景） |
| BS層 | US層・社内システム向けの業務サービス API 提供、業務サービスの一括処理（バッチ。[springer-batch.md](springer-batch.md)）提供 |
| DS層 | データ永続化（主に RDBMS）。本プロダクトでは独立アプリを持たず、BS層の Repository（MyBatis）に内包される（[springer-repository-mybatis.md](springer-repository-mybatis.md)）。他業務データへのアクセスは R-01-09 のとおり当該データ所有 BS層の Web API 経由に限る |

### レイヤー定義（US / BS）

| レイヤー | US アプリ (BFF) | BS アプリ (業務API) |
|---|---|---|
| Controller(PL) | `@Controller`(MPA) / `@RestController`(SPA/REST API)。ユーザー認証・エンドポイントのロールベース権限制御・入力値検証・セッション管理・画面遷移・レスポンス組み立てを担う | `@RestController`(REST API)。入力値検証・レスポンス組み立てを担う（認証・セッション管理は US層が担当済みのため対象外） |
| Service(BL) | ビジネスロジック（トランザクション制御 **なし**。[springer-service-transaction.md](springer-service-transaction.md) R-07-C1） | ビジネスロジック（トランザクション制御 **あり**。R-07-01） |
| Repository(DL) | API 呼び出し（主に BS への通信）・外部システム（社内/社外）連携 | データアクセス（主に DB アクセス＝DS層の業務データ）・外部システム（社内/社外）連携 |

> 機密情報フィールドのマスキング（`@Sensitive`）は [springer-logging-masking.md](springer-logging-masking.md)。
