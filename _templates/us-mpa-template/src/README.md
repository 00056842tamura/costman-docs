# {product}-us-mpa ソースコード構成

## ディレクトリ構成（推奨）

```
src/
├── main/
│   ├── java/
│   │   └── jp/co/example/{product}/usmpa/
│   │       ├── Application.java
│   │       ├── {機能名}/
│   │       │   ├── controller/
│   │       │   │   ├── {機能名}Controller.java    # @Controller（View 名を返す）
│   │       │   │   ├── form/                       # HTML フォーム入力クラス
│   │       │   │   └── validator/
│   │       │   ├── service/
│   │       │   │   ├── {機能名}Service.java
│   │       │   │   └── impl/                       # ※ @Transactional 不要
│   │       │   ├── repository/
│   │       │   │   ├── {機能名}Repository.java
│   │       │   │   └── impl/                       # BS API クライアント実装（RestClient）
│   │       │   └── model/
│   │       ├── config/
│   │       │   └── BsApiClientConfig.java          # RestClient Bean 登録
│   │       └── common/
│   └── resources/
│       ├── application.yml
│       ├── messages.properties
│       ├── templates/                              # Thymeleaf テンプレート
│       └── static/                                 # CSS / JS / 画像
└── test/
    └── java/
```

## 新規機能の作成

`/springer-scaffold` スキルで一括生成できます。

```
/springer-scaffold
アプリ種別: US-MPA
対象パッケージ: us-mpa/
ベースパッケージ: jp.co.example.{product}.usmpa.{機能名}
機能名: {機能名}
...
```

## 参考

Springer 公式の foodshop-us サンプル（`Input_Springer_GithubCopilotAgent/springer-samples-v3.0.2.0/foodshop-us/`）に
完全な MPA 実装例があります。
