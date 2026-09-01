package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.AuthorRepository
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertFailsWith

class AuthorServiceTest {
    private val authorRepository = mockk<AuthorRepository>()
    private val authorService = AuthorService(authorRepository)

    @Test
    fun `作成リクエストが正しい場合はリポジトリに作成を委譲する`() {
        val birthDate = LocalDate.of(1867, 2, 9)
        every {
            authorRepository.create("Natsume Soseki", birthDate)
        } returns
            Author(
                id = 1,
                name = "Natsume Soseki",
                birthDate = birthDate,
                createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
                updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            )

        val author = authorService.create("Natsume Soseki", birthDate)

        assertThat(author.name).isEqualTo("Natsume Soseki")
        assertThat(author.birthDate).isEqualTo(birthDate)
        verify { authorRepository.create("Natsume Soseki", birthDate) }
    }

    @Test
    fun `更新リクエストが正しい場合はリポジトリに更新を委譲する`() {
        val birthDate = LocalDate.of(1892, 3, 1)
        every {
            authorRepository.update(10, "Akutagawa Ryunosuke", birthDate)
        } returns
            Author(
                id = 10,
                name = "Akutagawa Ryunosuke",
                birthDate = birthDate,
                createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
                updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            )

        val author = authorService.update(10, "Akutagawa Ryunosuke", birthDate)

        assertThat(author.id).isEqualTo(10)
        assertThat(author.name).isEqualTo("Akutagawa Ryunosuke")
        assertThat(author.birthDate).isEqualTo(birthDate)
        verify { authorRepository.update(10, "Akutagawa Ryunosuke", birthDate) }
    }

    @Test
    fun `著者名が空白の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                authorService.create(" ", LocalDate.of(1867, 2, 9))
            }

        assertThat(exception.message).isEqualTo("name must not be blank")
        verify { authorRepository wasNot Called }
    }

    @Test
    fun `生年月日が未来日の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                authorService.create("Future Author", LocalDate.now().plusDays(1))
            }

        assertThat(exception.message).isEqualTo("birthDate must be today or earlier")
        verify { authorRepository wasNot Called }
    }
}
