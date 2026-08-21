# {product}-us-api ソースコード構成

## ディレクトリ構成（推奨）

```
src/
├── main/
│   ├── java/
│   │   └── jp/co/example/{product}/usapi/
│   │       ├── Application.java
│   │       ├── {機能名}/
│   │       │   ├── controller/
│   │       │   │   ├── {機能名}Controller.java    # @RestController（JSON 応答）
│   │       │   │   ├── request/
│   │       │   │   ├── validator/
│   │       │   │   └── view/
│   │       │   ├── service/
│   │       │   │   ├── {機能名}Service.java
│   │       │   │   └── impl/                       # ※ @Transactional 不要
│   │       │   ├── repository/
│   │       │   │   ├── {機能名}Repository.java
│   │       │   │   └── impl/                       # BS API クライアント実装（RestClient）
│   │       │   └── model/
│   │       ├── config/
│   │       │   ├── BsApiClientConfig.java
│   │       │   ├── CorsConfig.java                 # CORS（Reacter から呼ばれる場合）
│   │       │   └── SecurityConfig.java             # 認証/認可
│   │       └── common/
│   └── resources/
│       ├── application.yml
│       └── messages.properties
└── test/
    └── java/
```

## 新規機能の作成

```
/springer-scaffold
アプリ種別: US-API
対象パッケージ: us-api/
ベースパッケージ: jp.co.example.{product}.usapi.{機能名}
機能名: {機能名}
...
```

## 参考

- Springer 公式の foodshop-us サンプル
- 認証基盤: `Input_Springer_GithubCopilotAgent/アプリケーション開発ガイド_20260424_markdown/04.各機能の作り方/400.認証基盤/`
