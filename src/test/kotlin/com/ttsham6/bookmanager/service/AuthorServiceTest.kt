package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.AuthorRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertFailsWith

class AuthorServiceTest {
    private val authorRepository = RecordingAuthorRepository()
    private val authorService = AuthorService(authorRepository)

    @Test
    fun `作成リクエストが正しい場合はリポジトリに作成を委譲する`() {
        val birthDate = LocalDate.of(1867, 2, 9)

        val author = authorService.create("Natsume Soseki", birthDate)

        assertThat(author.name).isEqualTo("Natsume Soseki")
        assertThat(author.birthDate).isEqualTo(birthDate)
        assertThat(authorRepository.createdRequest).isEqualTo(
            AuthorRequest(
                name = "Natsume Soseki",
                birthDate = birthDate,
            ),
        )
    }

    @Test
    fun `更新リクエストが正しい場合はリポジトリに更新を委譲する`() {
        val birthDate = LocalDate.of(1892, 3, 1)

        val author = authorService.update(10, "Akutagawa Ryunosuke", birthDate)

        assertThat(author.id).isEqualTo(10)
        assertThat(author.name).isEqualTo("Akutagawa Ryunosuke")
        assertThat(author.birthDate).isEqualTo(birthDate)
        assertThat(authorRepository.updatedRequest).isEqualTo(
            UpdateAuthorRequest(
                authorId = 10,
                name = "Akutagawa Ryunosuke",
                birthDate = birthDate,
            ),
        )
    }

    @Test
    fun `著者名が空白の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                authorService.create(" ", LocalDate.of(1867, 2, 9))
            }

        assertThat(exception.message).isEqualTo("name must not be blank")
        assertThat(authorRepository.createdRequest).isNull()
    }

    @Test
    fun `生年月日が未来日の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                authorService.create("Future Author", LocalDate.now().plusDays(1))
            }

        assertThat(exception.message).isEqualTo("birthDate must be today or earlier")
        assertThat(authorRepository.createdRequest).isNull()
    }
}

private class RecordingAuthorRepository : AuthorRepository {
    var createdRequest: AuthorRequest? = null
        private set

    var updatedRequest: UpdateAuthorRequest? = null
        private set

    override fun create(
        name: String,
        birthDate: LocalDate,
    ): Author {
        createdRequest = AuthorRequest(name, birthDate)
        return author(
            id = 1,
            name = name,
            birthDate = birthDate,
        )
    }

    override fun update(
        authorId: Long,
        name: String,
        birthDate: LocalDate,
    ): Author {
        updatedRequest = UpdateAuthorRequest(authorId, name, birthDate)
        return author(
            id = authorId,
            name = name,
            birthDate = birthDate,
        )
    }

    private fun author(
        id: Long,
        name: String,
        birthDate: LocalDate,
    ): Author =
        Author(
            id = id,
            name = name,
            birthDate = birthDate,
            createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
        )
}

private data class AuthorRequest(
    val name: String,
    val birthDate: LocalDate,
)

private data class UpdateAuthorRequest(
    val authorId: Long,
    val name: String,
    val birthDate: LocalDate,
)
