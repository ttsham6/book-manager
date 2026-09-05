# 開発者向けドキュメント

## 前提

- JDK 21
- Docker
- ローカルの Flyway migration と jOOQ code generation 用 PostgreSQL

## ビルド

ビルド前にローカル PostgreSQL を起動します。Gradle のビルドでは、Kotlin のコンパイル前に Flyway
migration と jOOQ code generation が実行されます。

```bash
docker compose up -d postgres
./gradlew build
```

## アプリケーション起動

ローカルでアプリケーションを起動する場合も、事前に PostgreSQL を起動します。

```bash
docker compose up -d postgres
./gradlew bootRun
```

アプリケーションは Spring Boot のデフォルト設定で起動します。

## テスト

テストは次のコマンドで実行します。

```bash
./gradlew test
```

テストでは PostgreSQL の起動に Testcontainers を使います。

Rancher Desktop を使う場合は、`/var/run/docker.sock` が Rancher Desktop の socket
を指すようにします。

```bash
ln -s ~/.rd/docker.sock /var/run/docker.sock
```

すでに symlink が存在する場合や管理者権限が必要な場合は、ローカル環境に合わせて調整してください。
