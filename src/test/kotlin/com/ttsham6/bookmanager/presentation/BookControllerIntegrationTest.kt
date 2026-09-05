package com.ttsham6.bookmanager.presentation

import com.ttsham6.bookmanager.jooq.Tables.AUTHORS
import com.ttsham6.bookmanager.jooq.Tables.BOOKS
import com.ttsham6.bookmanager.jooq.Tables.BOOK_AUTHORS
import com.ttsham6.bookmanager.support.PostgresContainerTestBase
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIntegrationTest
    @Autowired
    constructor(
        private val dslContext: DSLContext,
    ) : PostgresContainerTestBase() {
        @LocalServerPort
        private var port: Int = 0

        private val httpClient = HttpClient.newHttpClient()

        @BeforeEach
        fun setUp() {
            dslContext
                .truncate(BOOK_AUTHORS, BOOKS, AUTHORS)
                .restartIdentity()
                .cascade()
                .execute()
        }

        @Test
        fun `POST booksで複数著者に紐づく書籍を作成できる`() {
            createAuthors()

            val response =
                post(
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(1, 2),
                    publicationStatus = "PUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
            assertThat(response.body()).contains("\"id\":1")
            assertThat(response.body()).contains("\"title\":\"Kokoro\"")
            assertThat(response.body()).contains("\"price\":1200")
            assertThat(response.body()).contains("\"publicationStatus\":\"PUBLISHED\"")
            assertThat(response.body()).contains("\"authors\":[")
            assertThat(response.body()).contains("\"id\":1")
            assertThat(response.body()).contains("\"name\":\"Natsume Soseki\"")
            assertThat(response.body()).contains("\"birthDate\":\"1867-02-09\"")
            assertThat(response.body()).contains("\"id\":2")
            assertThat(response.body()).contains("\"name\":\"Co Author\"")

            val persistedBook = dslContext.selectFrom(BOOKS).fetchSingle()
            assertThat(persistedBook.id).isEqualTo(1)
            assertThat(persistedBook.title).isEqualTo("Kokoro")
            assertThat(persistedBook.price).isEqualTo(1200)
            assertThat(persistedBook.publicationStatus).isEqualTo("PUBLISHED")
            assertThat(dslContext.selectCount().from(BOOK_AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(2)
        }

        @Test
        fun `GET booksで著者名の部分一致大文字小文字無視検索ができる`() {
            createAuthors()
            createBook(title = "Kokoro", publicationStatus = "PUBLISHED")
            createBook(title = "Sanshiro", publicationStatus = "UNPUBLISHED")

            val response = get("/books?authorName=SOSe")

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
            assertThat(response.body()).contains("\"title\":\"Kokoro\"")
            assertThat(response.body()).contains("\"title\":\"Sanshiro\"")
            assertThat(response.body()).contains("\"name\":\"Natsume Soseki\"")
            assertThat(response.body()).contains("\"publicationStatus\":\"PUBLISHED\"")
            assertThat(response.body()).contains("\"publicationStatus\":\"UNPUBLISHED\"")
        }

        @Test
        fun `GET booksで著者名が空白の場合は400を返す`() {
            val response = get("/books?authorName=%20")

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
            assertThat(response.body()).contains("authorName must not be blank")
        }

        @Test
        fun `タイトルが空白の場合は400を返す`() {
            createAuthors()

            val response =
                post(
                    title = " ",
                    price = 1200,
                    authorIds = listOf(1),
                    publicationStatus = "UNPUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
            assertThat(response.body()).contains("title must not be blank")
            assertThat(dslContext.selectCount().from(BOOKS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        @Test
        fun `存在しない著者IDを含む場合は404を返す`() {
            createAuthors()

            val response =
                post(
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(1, 99),
                    publicationStatus = "UNPUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
            assertThat(response.body()).contains("Authors 99 were not found")
            assertThat(dslContext.selectCount().from(BOOKS).fetchSingle(0, Int::class.java)).isEqualTo(0)
            assertThat(dslContext.selectCount().from(BOOK_AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        @Test
        fun `PUT booksで書籍を更新できる`() {
            createAuthors()
            createBook()

            val response =
                put(
                    path = "/books/1",
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(2),
                    publicationStatus = "PUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
            assertThat(response.body()).contains("\"id\":1")
            assertThat(response.body()).contains("\"title\":\"Kokoro\"")
            assertThat(response.body()).contains("\"price\":1200")
            assertThat(response.body()).contains("\"publicationStatus\":\"PUBLISHED\"")
            assertThat(response.body()).contains("\"id\":2")
            assertThat(response.body()).contains("\"name\":\"Co Author\"")

            val persistedBook = dslContext.selectFrom(BOOKS).fetchSingle()
            assertThat(persistedBook.id).isEqualTo(1)
            assertThat(persistedBook.title).isEqualTo("Kokoro")
            assertThat(persistedBook.price).isEqualTo(1200)
            assertThat(persistedBook.publicationStatus).isEqualTo("PUBLISHED")

            val persistedBookAuthors = dslContext.selectFrom(BOOK_AUTHORS).fetch()
            assertThat(persistedBookAuthors).hasSize(1)
            assertThat(persistedBookAuthors.first().bookId).isEqualTo(1)
            assertThat(persistedBookAuthors.first().authorId).isEqualTo(2)
        }

        @Test
        fun `存在しない書籍へのPUTは404を返して書籍を作成しない`() {
            createAuthors()

            val response =
                put(
                    path = "/books/99",
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(1),
                    publicationStatus = "UNPUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
            assertThat(response.body()).contains("Book 99 was not found")
            assertThat(dslContext.selectCount().from(BOOKS).fetchSingle(0, Int::class.java)).isEqualTo(0)
            assertThat(dslContext.selectCount().from(BOOK_AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        @Test
        fun `出版済みの書籍を未出版へ更新するPUTは400を返す`() {
            createAuthors()
            createBook(publicationStatus = "PUBLISHED")

            val response =
                put(
                    path = "/books/1",
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(1),
                    publicationStatus = "UNPUBLISHED",
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
            assertThat(response.body()).contains("published book cannot be changed to unpublished")

            val persistedBook = dslContext.selectFrom(BOOKS).fetchSingle()
            assertThat(persistedBook.publicationStatus).isEqualTo("PUBLISHED")
        }

        private fun createAuthors() {
            dslContext
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, "Natsume Soseki")
                .set(AUTHORS.BIRTH_DATE, LocalDate.of(1867, 2, 9))
                .execute()
            dslContext
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, "Co Author")
                .set(AUTHORS.BIRTH_DATE, LocalDate.of(1900, 1, 1))
                .execute()
        }

        private fun createBook(publicationStatus: String = "UNPUBLISHED") {
            createBook(title = "Before", publicationStatus = publicationStatus)
        }

        private fun createBook(
            title: String,
            publicationStatus: String = "UNPUBLISHED",
        ) {
            val book =
                dslContext
                    .insertInto(BOOKS)
                    .set(BOOKS.TITLE, title)
                    .set(BOOKS.PRICE, 1000)
                    .set(BOOKS.PUBLICATION_STATUS, publicationStatus)
                    .returning()
                    .fetchOne()
                    ?: error("Failed to insert book")
            dslContext
                .insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, book.id)
                .set(BOOK_AUTHORS.AUTHOR_ID, 1)
                .execute()
        }

        private fun get(path: String): HttpResponse<String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create("http://localhost:$port$path"))
                    .GET()
                    .build()

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }

        private fun post(
            title: String,
            price: Long,
            authorIds: List<Long>,
            publicationStatus: String,
        ): HttpResponse<String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create("http://localhost:$port/books"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bookJson(title, price, authorIds, publicationStatus)))
                    .build()

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }

        private fun put(
            path: String,
            title: String,
            price: Long,
            authorIds: List<Long>,
            publicationStatus: String,
        ): HttpResponse<String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create("http://localhost:$port$path"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(bookJson(title, price, authorIds, publicationStatus)))
                    .build()

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }

        private fun bookJson(
            title: String,
            price: Long,
            authorIds: List<Long>,
            publicationStatus: String,
        ): String =
            """
            {
              "title": "$title",
              "price": $price,
              "authorIds": ${authorIds.joinToString(prefix = "[", postfix = "]")},
              "publicationStatus": "$publicationStatus"
            }
            """.trimIndent()
    }
