package com.ttsham6.bookmanager.infrastructure

import com.ttsham6.bookmanager.domain.AuthorNotFoundException
import com.ttsham6.bookmanager.domain.BookNotFoundException
import com.ttsham6.bookmanager.domain.BookRepository
import com.ttsham6.bookmanager.domain.PublicationStatus
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
import java.time.LocalDate
import kotlin.test.assertFailsWith

@SpringBootTest
class BookRepositoryImplTest
    @Autowired
    constructor(
        private val bookRepository: BookRepository,
        private val dslContext: DSLContext,
    ) : PostgresContainerTestBase() {
        @BeforeEach
        fun setUp() {
            dslContext
                .truncate(BOOK_AUTHORS, BOOKS, AUTHORS)
                .restartIdentity()
                .cascade()
                .execute()
        }

        @Test
        fun `複数著者に紐づく書籍を作成して保存できる`() {
            val authorIds = createAuthors()

            val book =
                bookRepository.create(
                    title = "Kokoro",
                    price = 1200,
                    authorIds = authorIds,
                    publicationStatus = PublicationStatus.PUBLISHED,
                )

            assertThat(book.id).isEqualTo(1)
            assertThat(book.title).isEqualTo("Kokoro")
            assertThat(book.price).isEqualTo(1200)
            assertThat(book.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
            assertThat(book.authors.map { it.id }).containsExactly(1, 2)
            assertThat(book.createdAt).isNotNull()
            assertThat(book.updatedAt).isNotNull()

            val persistedBook = dslContext.selectFrom(BOOKS).fetchSingle()
            assertThat(persistedBook.id).isEqualTo(book.id)
            assertThat(persistedBook.title).isEqualTo("Kokoro")
            assertThat(persistedBook.price).isEqualTo(1200)
            assertThat(persistedBook.publicationStatus).isEqualTo("PUBLISHED")

            val persistedBookAuthors = dslContext.selectFrom(BOOK_AUTHORS).orderBy(BOOK_AUTHORS.AUTHOR_ID).fetch()
            assertThat(persistedBookAuthors).hasSize(2)
            assertThat(persistedBookAuthors.map { it.bookId }).containsExactly(1, 1)
            assertThat(persistedBookAuthors.map { it.authorId }).containsExactly(1, 2)
        }

        @Test
        fun `存在しない著者IDを含む場合は例外を投げて書籍を作成しない`() {
            createAuthors()

            val exception =
                assertFailsWith<AuthorNotFoundException> {
                    bookRepository.create(
                        title = "Kokoro",
                        price = 1200,
                        authorIds = listOf(1, 99),
                        publicationStatus = PublicationStatus.UNPUBLISHED,
                    )
                }

            assertThat(exception.message).isEqualTo("Authors 99 were not found")
            assertThat(dslContext.selectCount().from(BOOKS).fetchSingle(0, Int::class.java)).isEqualTo(0)
            assertThat(dslContext.selectCount().from(BOOK_AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        @Test
        fun `存在する書籍IDを指定した場合は書籍と紐づく著者を更新する`() {
            val authorIds = createAuthors()
            val book =
                bookRepository.create(
                    title = "Before",
                    price = 1000,
                    authorIds = listOf(authorIds.first()),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                )

            val updatedBook =
                bookRepository.update(
                    bookId = book.id,
                    title = "Kokoro",
                    price = 1200,
                    authorIds = listOf(authorIds.last()),
                    publicationStatus = PublicationStatus.PUBLISHED,
                )

            assertThat(updatedBook.id).isEqualTo(book.id)
            assertThat(updatedBook.title).isEqualTo("Kokoro")
            assertThat(updatedBook.price).isEqualTo(1200)
            assertThat(updatedBook.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
            assertThat(updatedBook.authors.map { it.id }).containsExactly(authorIds.last())
            assertThat(updatedBook.createdAt).isEqualTo(book.createdAt)
            assertThat(updatedBook.updatedAt).isAfterOrEqualTo(book.updatedAt)

            val persistedBook = dslContext.selectFrom(BOOKS).fetchSingle()
            assertThat(persistedBook.id).isEqualTo(book.id)
            assertThat(persistedBook.title).isEqualTo("Kokoro")
            assertThat(persistedBook.price).isEqualTo(1200)
            assertThat(persistedBook.publicationStatus).isEqualTo("PUBLISHED")

            val persistedBookAuthors = dslContext.selectFrom(BOOK_AUTHORS).fetch()
            assertThat(persistedBookAuthors).hasSize(1)
            assertThat(persistedBookAuthors.first().bookId).isEqualTo(book.id)
            assertThat(persistedBookAuthors.first().authorId).isEqualTo(authorIds.last())
        }

        @Test
        fun `存在しない書籍IDを指定した場合は例外を投げて書籍を作成しない`() {
            val authorIds = createAuthors()

            val exception =
                assertFailsWith<BookNotFoundException> {
                    bookRepository.update(
                        bookId = 99,
                        title = "Kokoro",
                        price = 1200,
                        authorIds = authorIds,
                        publicationStatus = PublicationStatus.UNPUBLISHED,
                    )
                }

            assertThat(exception.message).isEqualTo("Book 99 was not found")
            assertThat(dslContext.selectCount().from(BOOKS).fetchSingle(0, Int::class.java)).isEqualTo(0)
            assertThat(dslContext.selectCount().from(BOOK_AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        private fun createAuthors(): List<Long> {
            val author1 =
                dslContext
                    .insertInto(AUTHORS)
                    .set(AUTHORS.NAME, "Natsume Soseki")
                    .set(AUTHORS.BIRTH_DATE, LocalDate.of(1867, 2, 9))
                    .returning()
                    .fetchOne()
                    ?: error("Failed to insert author")
            val author2 =
                dslContext
                    .insertInto(AUTHORS)
                    .set(AUTHORS.NAME, "Co Author")
                    .set(AUTHORS.BIRTH_DATE, LocalDate.of(1900, 1, 1))
                    .returning()
                    .fetchOne()
                    ?: error("Failed to insert author")
            return listOf(author1.id, author2.id)
        }
    }
