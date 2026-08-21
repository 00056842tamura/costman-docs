# {product}-bs ソースコード構成

## ディレクトリ構成（推奨）

```
src/
├── main/
│   ├── java/
│   │   └── jp/co/example/{product}/bs/
│   │       ├── Application.java                # @SpringBootApplication エントリポイント
│   │       ├── {機能名}/                       # 機能ごとにサブパッケージを切る
│   │       │   ├── controller/
│   │       │   │   ├── {機能名}Controller.java
│   │       │   │   ├── request/
│   │       │   │   ├── validator/
│   │       │   │   └── view/
│   │       │   ├── service/
│   │       │   │   ├── {機能名}Service.java
│   │       │   │   └── impl/
│   │       │   │       └── {機能名}ServiceImpl.java
│   │       │   ├── repository/
│   │       │   │   ├── {機能名}Repository.java
│   │       │   │   ├── impl/
│   │       │   │   │   └── {機能名}RepositoryImpl.java
│   │       │   │   └── mapper/
│   │       │   │       └── {機能名}Mapper.java
│   │       │   └── model/
│   │       │       └── {機能名}.java
│   │       └── common/                         # 共通ユーティリティ
│   └── resources/
│       ├── application.yml
│       ├── messages.properties
│       └── jp/co/example/{product}/bs/...      # Mapper XML（Mapper インターフェースと同パッケージ）
└── test/
    ├── java/
    │   └── jp/co/example/{product}/bs/...      # テストクラス
    └── resources/
        └── sql/
            ├── create/                          # CREATE TABLE 文
            ├── delete/                          # DELETE 文（テスト前クリア用）
            └── data/                            # テスト初期データ INSERT 文
```

## 新規機能の作成

`/springer-scaffold` スキルで一括生成できます。

```
/springer-scaffold
アプリ種別: BS
対象パッケージ: bs/
ベースパッケージ: jp.co.example.{product}.bs.{機能名}
機能名: {機能名}
...
```

## 参考

Springer 公式の foodshop-bs サンプル（`Input_Springer_GithubCopilotAgent/springer-samples-v3.0.2.0/foodshop-bs/`）に
完全な実装例があります。
