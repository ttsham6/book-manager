package com.ttsham6.bookmanager.presentation

import com.ttsham6.bookmanager.jooq.Tables.AUTHORS
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
class AuthorControllerIntegrationTest
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
                .truncate(AUTHORS)
                .restartIdentity()
                .cascade()
                .execute()
        }

        @Test
        fun `POST authorsで著者を作成できる`() {
            val response =
                post(
                    path = "/authors",
                    name = "Natsume Soseki",
                    birthDate = LocalDate.of(1867, 2, 9),
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
            assertThat(response.body()).contains("\"id\":1")
            assertThat(response.body()).contains("\"name\":\"Natsume Soseki\"")
            assertThat(response.body()).contains("\"birthDate\":\"1867-02-09\"")

            val persistedAuthor = dslContext.selectFrom(AUTHORS).fetchSingle()
            assertThat(persistedAuthor.id).isEqualTo(1)
            assertThat(persistedAuthor.name).isEqualTo("Natsume Soseki")
            assertThat(persistedAuthor.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
        }

        @Test
        fun `PUT authorsで著者を更新できる`() {
            dslContext
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, "Mori Ogai")
                .set(AUTHORS.BIRTH_DATE, LocalDate.of(1862, 2, 17))
                .execute()

            val response =
                put(
                    path = "/authors/1",
                    name = "Mori Rintaro",
                    birthDate = LocalDate.of(1862, 2, 19),
                )

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
            assertThat(response.body()).contains("\"id\":1")
            assertThat(response.body()).contains("\"name\":\"Mori Rintaro\"")
            assertThat(response.body()).contains("\"birthDate\":\"1862-02-19\"")

            val persistedAuthor = dslContext.selectFrom(AUTHORS).fetchSingle()
            assertThat(persistedAuthor.id).isEqualTo(1)
            assertThat(persistedAuthor.name).isEqualTo("Mori Rintaro")
            assertThat(persistedAuthor.birthDate).isEqualTo(LocalDate.of(1862, 2, 19))
        }

        private fun post(
            path: String,
            name: String,
            birthDate: LocalDate,
        ): HttpResponse<String> = sendWithBody("POST", path, name, birthDate)

        private fun put(
            path: String,
            name: String,
            birthDate: LocalDate,
        ): HttpResponse<String> = sendWithBody("PUT", path, name, birthDate)

        private fun sendWithBody(
            method: String,
            path: String,
            name: String,
            birthDate: LocalDate,
        ): HttpResponse<String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create("http://localhost:$port$path"))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(authorJson(name, birthDate)))
                    .build()

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }

        private fun authorJson(
            name: String,
            birthDate: LocalDate,
        ): String =
            """
            {
              "name": "$name",
              "birthDate": "$birthDate"
            }
            """.trimIndent()
    }
