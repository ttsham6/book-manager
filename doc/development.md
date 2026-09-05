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

## API リクエスト例

### 著者作成

書籍作成には著者 ID が必要なため、先に著者を作成します。

```bash
curl -i -X POST http://localhost:8080/authors \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Natsume Soseki",
    "birthDate": "1867-02-09"
  }'
```

レスポンス例:

```json
{
  "id": 1,
  "name": "Natsume Soseki",
  "birthDate": "1867-02-09"
}
```

### 著者更新

```bash
curl -i -X PUT http://localhost:8080/authors/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Natsume Soseki",
    "birthDate": "1867-02-09"
  }'
```

レスポンス例:

```json
{
  "id": 1,
  "name": "Natsume Soseki",
  "birthDate": "1867-02-09"
}
```

### 書籍作成

`authorIds` には、作成済みの著者 ID を 1 つ以上指定します。
`publicationStatus` は `UNPUBLISHED` または `PUBLISHED` を指定します。

```bash
curl -i -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Kokoro",
    "price": 1200,
    "authorIds": [1],
    "publicationStatus": "PUBLISHED"
  }'
```

レスポンス例:

```json
{
  "id": 1,
  "title": "Kokoro",
  "price": 1200,
  "publicationStatus": "PUBLISHED",
  "authors": [
    {
      "id": 1,
      "name": "Natsume Soseki",
      "birthDate": "1867-02-09"
    }
  ]
}
```

### 著者名で書籍検索

`authorName` には著者名の一部を指定できます。検索は部分一致かつ大文字小文字を区別しません。

```bash
curl -i 'http://localhost:8080/books?authorName=sose'
```

レスポンス例:

```json
[
  {
    "id": 1,
    "title": "Kokoro",
    "price": 1200,
    "publicationStatus": "PUBLISHED",
    "authors": [
      {
        "id": 1,
        "name": "Natsume Soseki",
        "birthDate": "1867-02-09"
      }
    ]
  }
]
```

### 書籍更新

`PUT /books/{bookId}` は既存書籍の更新のみを行います。存在しない書籍 ID を指定しても新規作成はされず、
`404 Not Found` を返します。

```bash
curl -i -X PUT http://localhost:8080/books/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Kokoro",
    "price": 1400,
    "authorIds": [1],
    "publicationStatus": "PUBLISHED"
  }'
```

レスポンス例:

```json
{
  "id": 1,
  "title": "Kokoro",
  "price": 1400,
  "publicationStatus": "PUBLISHED",
  "authors": [
    {
      "id": 1,
      "name": "Natsume Soseki",
      "birthDate": "1867-02-09"
    }
  ]
}
```

出版済みの書籍を未出版へ戻す更新はできません。

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
