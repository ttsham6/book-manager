package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.Book
import com.ttsham6.bookmanager.domain.BookRepository
import com.ttsham6.bookmanager.domain.PublicationStatus
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertFailsWith

class BookServiceTest {
    private val bookRepository = mockk<BookRepository>()
    private val bookService = BookService(bookRepository)

    @Test
    fun `作成リクエストが正しい場合はリポジトリに作成を委譲する`() {
        val authorIds = listOf(1L, 2L)
        every {
            bookRepository.create("Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED)
        } returns
            Book(
                id = 1,
                title = "Kokoro",
                price = 1200,
                authors =
                    listOf(
                        author(1, "Natsume Soseki"),
                        author(2, "Co Author"),
                    ),
                publicationStatus = PublicationStatus.PUBLISHED,
                createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
                updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            )

        val book = bookService.create("Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED)

        assertThat(book.title).isEqualTo("Kokoro")
        assertThat(book.price).isEqualTo(1200)
        assertThat(book.authors).hasSize(2)
        assertThat(book.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
        verify { bookRepository.create("Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED) }
    }

    @Test
    fun `更新リクエストが正しい場合はリポジトリに更新を委譲する`() {
        val authorIds = listOf(1L, 2L)
        every { bookRepository.findById(1) } returns
            book(
                id = 1,
                title = "Before",
                price = 1000,
                publicationStatus = PublicationStatus.UNPUBLISHED,
            )
        every {
            bookRepository.update(1, "Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED)
        } returns
            book(
                id = 1,
                title = "Kokoro",
                price = 1200,
                publicationStatus = PublicationStatus.PUBLISHED,
            )

        val book = bookService.update(1, "Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED)

        assertThat(book.id).isEqualTo(1)
        assertThat(book.title).isEqualTo("Kokoro")
        assertThat(book.price).isEqualTo(1200)
        assertThat(book.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
        verify { bookRepository.findById(1) }
        verify { bookRepository.update(1, "Kokoro", 1200, authorIds, PublicationStatus.PUBLISHED) }
    }

    @Test
    fun `出版済みの書籍は未出版に更新できない`() {
        every { bookRepository.findById(1) } returns
            book(
                id = 1,
                title = "Kokoro",
                price = 1200,
                publicationStatus = PublicationStatus.PUBLISHED,
            )

        val exception =
            assertFailsWith<IllegalArgumentException> {
                bookService.update(1, "Kokoro", 1200, listOf(1), PublicationStatus.UNPUBLISHED)
            }

        assertThat(exception.message).isEqualTo("published book cannot be changed to unpublished")
        verify { bookRepository.findById(1) }
        verify(exactly = 0) {
            bookRepository.update(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `タイトルが空白の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                bookService.create(" ", 1200, listOf(1), PublicationStatus.UNPUBLISHED)
            }

        assertThat(exception.message).isEqualTo("title must not be blank")
        verify { bookRepository wasNot Called }
    }

    @Test
    fun `価格が負数の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                bookService.create("Kokoro", -1, listOf(1), PublicationStatus.UNPUBLISHED)
            }

        assertThat(exception.message).isEqualTo("price must be greater than or equal to 0")
        verify { bookRepository wasNot Called }
    }

    @Test
    fun `著者IDが空の場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                bookService.create("Kokoro", 1200, emptyList(), PublicationStatus.UNPUBLISHED)
            }

        assertThat(exception.message).isEqualTo("authorIds must not be empty")
        verify { bookRepository wasNot Called }
    }

    @Test
    fun `著者IDに重複がある場合は作成できない`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                bookService.create("Kokoro", 1200, listOf(1, 1), PublicationStatus.UNPUBLISHED)
            }

        assertThat(exception.message).isEqualTo("authorIds must not contain duplicates")
        verify { bookRepository wasNot Called }
    }

    private fun author(
        id: Long,
        name: String,
    ): Author =
        Author(
            id = id,
            name = name,
            birthDate = LocalDate.of(1867, 2, 9),
            createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
        )

    private fun book(
        id: Long,
        title: String,
        price: Long,
        publicationStatus: PublicationStatus,
    ): Book =
        Book(
            id = id,
            title = title,
            price = price,
            authors = listOf(author(1, "Natsume Soseki")),
            publicationStatus = publicationStatus,
            createdAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
            updatedAt = OffsetDateTime.parse("2026-09-01T00:00:00Z"),
        )
}
