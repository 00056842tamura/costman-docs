---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/*Mapper.xml"
  - "**/application*.yml"
  - "**/application*.yaml"
---

# 23 クラウドストレージ・シークレット（Azure Blob Storage / Key Vault）

> 分類凡例: 🚫 禁止 / ⚠ 非推奨 / ✅ 必須 / ✨ 推奨 / 条件付き事項 / 参考・推奨実装パターン
> 関連: 一時ファイル削除は [springer-file-prevention.md](springer-file-prevention.md)、例外（`SystemException` 変換）は [springer-exception.md](springer-exception.md)、DI/Bean は [springer-di-bean.md](springer-di-bean.md)。
> 適用範囲: Azure Blob Storage / Azure Key Vault を利用するアプリ（環境依存トピック。利用時のみ適用）。

## 🚫 禁止 (NEVER)

（なし）

## ⚠ 非推奨 (AVOID)

（なし）

## ✅ 必須 (ALWAYS)

### R-23-01 Blob アクセスは Repository で実装し BlobContainerClient を注入する
Azure Blob Storage へのアクセスは `@Repository`（インターフェイス＋実装）で実装し、`BlobContainerClient` をコンストラクターインジェクションで受け取り `private final` で保持する。操作は `getBlobClient(name)` で `BlobClient` を取得して行う（`exists()`／`uploadFromFile(path, true)`〔true＝上書き〕／`downloadToFile(path, true)`／`delete()`）。

### R-23-02 Blob の接続情報は環境変数（ConfigMap/Secret）で注入する
`spring.cloud.azure.storage.blob.{account-name, endpoint, container-name}` を設定し、値は環境変数で注入する（環境変数は Kubernetes の ConfigMap または Secret で対応）。標準環境変数は `SPRING_CLOUD_AZURE_STORAGE_BLOB_ACCOUNT_NAME`／`_ENDPOINT`／`_CONTAINER_NAME`。
- 条件 / 例外: 認証はアクセスキー（`_ACCESS_KEY`）か SAS トークン（`_SAS_TOKEN`）の二者択一。一方を使う場合は他方を未設定にする。

### R-23-03 Blob のダウンロードは一時ファイル経由とし削除登録する
ダウンロードは `Files.createTempFile(name, ".dat")` で一時ファイルを作成し、その `Path` を `downloadToFile` の出力先にする。作成した一時ファイルは `jp.co.nekonet.springer.mvc.support.temporary.TemporaryFileService` の `register(path)` でレスポンス送信後の削除対象として登録する（[springer-file-prevention.md](springer-file-prevention.md)）。ダウンロード時の `IOException` は Repository で catch し `SystemException` に変換する（[springer-exception.md](springer-exception.md)）。

### R-23-04 Key Vault のシークレット取得は Repository で SecretClient を注入する
Key Vault のシークレット取得は `@Repository` で実装し、`com.azure.security.keyvault.secrets.SecretClient` をコンストラクターインジェクションで受け取り `private final` で保持する。取得は `secretClient.getSecret(name, version)` を呼び、戻り値 `KeyVaultSecret` の `getValue()` で値を得る。

### R-23-05 Key Vault の接続情報は環境変数（ConfigMap/Secret）で注入する
Key Vault の接続情報は環境変数で注入する（Kubernetes の ConfigMap または Secret）。サービスプリンシパル認証のため `SPRING_CLOUD_AZURE_KEYVAULT_SECRET_CREDENTIAL_CLIENTID`／`_CREDENTIAL_CLIENTSECRET`／`_ENDPOINT`／`_PROFILE_TENANTID` を設定する。

## ✨ 推奨 (PREFER)

（なし）

## 条件付き事項

### R-23-C1 Blob で複数ストレージアカウントを扱う場合の Bean 登録
Azure Blob SDK は 1 アカウント接続のみサポートするため、複数アカウントを使う場合はプロパティを個別に用意し接続を自前実装する。
- 基本アカウントの `BlobContainerClient` は `@Configuration` で `@Bean`＋`@Primary` 登録する（AutoConfiguration 済みの `BlobServiceClientBuilder`・`AzureStorageBlobProperties` から生成）。
- 追加アカウントは `@Bean("名")` で登録し（`new BlobServiceClientBuilder()` に `endpoint()`＋認証〔`sasToken(...)` または `StorageSharedKeyCredential`〕を設定して `getBlobContainerClient(container)`）、利用側 Repository は `@Qualifier("名")` で受け取る。追加プロパティクラスは `@EnableConfigurationProperties` で有効化する。

## 参考・推奨実装パターン

- 依存（Maven）: Blob＝`com.azure.spring:spring-cloud-azure-starter-storage-blob`、Key Vault＝`com.azure.spring:spring-cloud-azure-starter-keyvault-secrets`。いずれも `dependencyManagement` に BOM `com.azure.spring:spring-cloud-azure-dependencies:${spring.cloud.azure.version}`（`type: pom`／`scope: import`）を import する。Gradle は `mavenBom` で同 BOM を指定。
- `${spring.cloud.azure.version}` の値は Azure 公式「Spring Versions Mapping」（Spring Cloud Azure ↔ Spring Boot 対応）に従う。Springer 2/3（Spring Boot 3/4）ごとの差分は原典に明記なし。
- Blob SDK の主なクラス（`com.azure.storage.blob.*`）: `BlobServiceClient`（アカウント/コンテナー操作）・`BlobContainerClient`（コンテナーと BLOB 操作）・`BlobClient`（個別 BLOB 操作）。暗号化/復号が必要な場合は [springer-security-auth.md](springer-security-auth.md)（R-13-C6）を参照。
