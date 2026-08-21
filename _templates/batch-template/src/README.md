# {product}-batch ソースコード構成

## ディレクトリ構成（推奨）

```
src/
├── main/
│   ├── java/
│   │   └── jp/co/example/{product}/batch/
│   │       ├── Application.java
│   │       ├── {jobnet名}/                         # ジョブネット単位
│   │       │   ├── {job名}/                        # ジョブ単位
│   │       │   │   ├── runner/                     # CommandLineRunner 実装
│   │       │   │   ├── service/
│   │       │   │   │   ├── {Job}Service.java
│   │       │   │   │   └── impl/                   # @Transactional
│   │       │   │   ├── repository/
│   │       │   │   │   ├── impl/                   # DB or BS API
│   │       │   │   │   └── mapper/                 # MyBatis Mapper
│   │       │   │   └── model/
│   │       │   └── ...
│   │       └── common/
│   └── resources/
│       ├── application.yml
│       ├── messages.properties
│       └── jp/co/example/{product}/batch/...       # Mapper XML
└── test/
    └── java/
```

## 起動形態

- `CommandLineRunner` または `ApplicationRunner` を実装したクラスを `@Component` で登録
- `java -jar {product}-batch.jar --job=jobnet1.job1` のようにジョブ識別子を引数で受け取る運用が一般的
- 終了時に明示的に `System.exit(code)` で Exit Code を返す

## 大量データ処理

MyBatis の `Cursor<T>` を使って 1 件ずつ処理してください（オンメモリ全件読み込み禁止）。

```java
try (Cursor<Item> cursor = this.itemMapper.selectListCursor(condition)) {
    for (Item item : cursor) {
        // 1 件ずつ処理
    }
}
```

## 新規ジョブの作成

```
/springer-scaffold
アプリ種別: batch
対象パッケージ: batch/
ベースパッケージ: jp.co.example.{product}.batch.{jobnet}.{job}
機能名: {Job名}
...
```

## 参考

Springer 公式の foodshop-batch サンプル（`Input_Springer_GithubCopilotAgent/springer-samples-v3.0.2.0/foodshop-batch/`）に
ジョブネット構成の完全な例があります。
