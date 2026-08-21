# 開発環境構築手順

新規参画メンバーが開発を開始するための手順をまとめます。

## 1. 必要なツール

| ツール | バージョン | 確認コマンド |
|---|---|---|
| JDK | 25 | `java -version` |
| Maven | 3.9.14 | `mvn -version` |
| Gradle（採用する場合） | 8 以上 | `gradle -version` |
| Node.js | 24 | `node --version` |
| Python | 3.8 以上 | `python --version` または `python3 --version` |
| Claude Code | 最新 | `claude --version` |
| Git | 任意 | `git --version` |

詳細インストール手順はプロダクトルートの `README.md` セクション 2 を参照。

## 2. リポジトリ取得

```bash
$ git clone {リポジトリ URL}
$ cd {プロダクトディレクトリ名}
```

## 3. Claude Code の起動疎通確認

```bash
$ claude
```

```
> /help
```

`/springer-*` 系のコマンドが見えれば配置 OK。
詳細は `README.md` セクション 4.3 を参照。

## 4. プロダクトを選んで作業

このリポジトリには複数のスタックが含まれています。担当するスタックに移動してください。

```bash
$ cd {bs|us-mpa|us-api|batch}/
```

各パッケージの `CLAUDE.md` を確認し、Springer バージョン・ベースパッケージ・依存パッケージを把握します。

## 5. DB セットアップ（BS / バッチパッケージ）

```bash
$ # 例：Docker Compose の場合
$ docker compose up -d db
$ cd bs
$ mvn flyway:migrate    # またはプロジェクト指定の DDL 適用方法
```

## 6. ビルドと起動（ビルドツール確定後）

### Maven の場合

```bash
$ cd {type}
$ mvn clean install -DskipTests
$ mvn spring-boot:run
```

### Gradle の場合

```bash
$ cd {type}
$ ./gradlew clean build -x test
$ ./gradlew bootRun
```

## 7. フックの動作確認

`.claude/settings.json` のフックは Write/Edit 直後に Python スクリプトを起動して規約チェックを行います。

```bash
$ python3 --version    # または python --version
$ ls .claude/settings.json
$ ls .claude/hooks/
```

実際の動作確認は `CLAUDE.md` セクション 7 を参照。

## 8. トラブル時の連絡先

- 環境構築で詰まった場合: （Slack チャンネル名等）
- DB 接続情報: （社内 Wiki URL）
- Springer ライブラリの問題: 標準チームへエスカレーション
