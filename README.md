# book-manager

書籍と著者を管理する Spring Boot アプリケーションです。

開発環境のセットアップ、ビルド、テスト方法は [開発者向けドキュメント](doc/development.md) を参照してください。

## API リクエスト例

以下の例は、アプリケーションが `http://localhost:8080` で起動している前提です。

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
