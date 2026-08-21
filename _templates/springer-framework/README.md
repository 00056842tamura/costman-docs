# springer-framework（Springer フレームワーク jar/pom 受入用）

Springer フレームワークのビルド済みアーティファクト（jar/pom）は、個々のアプリ種別
（`bs`/`us-mpa`/`us-api`/`batch`）に属さない共有資材です。
4つの Springer 系スタックすべてが本ディレクトリの受領 jar/pom を再利用してローカル `.m2` に登録します。
archive（社内 Maven リポジトリ等）から依存を取得できない環境向けに、受領した jar/pom をローカル `.m2` へ登録する回避策です。

## 資材の受け皿 `_received/`

標準チームから配布された Springer フレームワークの**ビルド済み jar/pom**（例: `springer-core-3.0.3.jar` と
`springer-core-3.0.3.pom`）を、このディレクトリ配下の `_received/` に配置してください（フレームワークのソースプロジェクトではありません）。

```
_templates/springer-framework/
├── README.md       … 本ファイル（型のひな形。追跡対象）
└── _received/       … 【受領資材の配置先。.gitignore 対象・リモートにpushしない】
    ├── springer-core-{version}.jar   … ビルド済み jar（配布物は単一 jar+pom を想定）
    └── springer-core-{version}.pom   … 対応する pom（座標・依存情報を保持）
```

## 配置後の手順

1. Springer フレームワークのビルド済み jar/pom を入手する（入手経路は標準チームに確認する）。
2. jar/pom を `_templates/springer-framework/_received/` にコピーする。
3. jar/pom をローカル `.m2` へ登録する（`/stack-init` はフレームワークの登録を行わないため、実施者が `/stack-init` 実行前に行う）。
   - `mvn install:install-file -Dfile={jar} -DpomFile={pom}` を実行する。座標（groupId/artifactId/version/packaging）は pom から自動取得されるため個別指定は不要。
4. その後 `/stack-init` を実行する（Springer 系のスタック種別を指定）。`/stack-init` はフレームワークのインストールは行わず、ワイヤーフレームの実スタックへの反映とビルド確認のみを行う。

## 注意事項

- `_received/` に配置した資材そのものはリモートにpushしない（常に標準チームの最新版・確定版を都度受領する運用のため）。
- 本ディレクトリはアプリ種別非依存の共有資材専用であり、`{type}-template/_received/`（各スタック固有のワイヤーフレーム）とは別管理とする。
- 本 README（`_received/` 以外）は通常のひな形ファイルとして追跡・管理する。改善要望がある場合は標準チームに相談すること（直接編集しない）。
