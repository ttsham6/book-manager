package com.ttsham6.bookmanager.infrastructure

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.AuthorNotFoundException
import com.ttsham6.bookmanager.domain.Book
import com.ttsham6.bookmanager.domain.BookNotFoundException
import com.ttsham6.bookmanager.domain.BookRepository
import com.ttsham6.bookmanager.domain.PublicationStatus
import com.ttsham6.bookmanager.jooq.Tables.AUTHORS
import com.ttsham6.bookmanager.jooq.Tables.BOOKS
import com.ttsham6.bookmanager.jooq.Tables.BOOK_AUTHORS
import com.ttsham6.bookmanager.jooq.tables.records.AuthorsRecord
import com.ttsham6.bookmanager.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryImpl(
    private val dslContext: DSLContext,
) : BookRepository {
    override fun findByIdForUpdate(bookId: Long): Book? =
        dslContext
            .selectFrom(BOOKS)
            .where(BOOKS.ID.eq(bookId))
            .forUpdate()
            .fetchOne()
            ?.let { bookRecord ->
                toBook(bookRecord, findAuthorsByBookId(dslContext, bookId))
            }

    override fun findByAuthorName(authorName: String): List<Book> =
        dslContext
            .selectDistinct(BOOKS.fields().toList())
            .from(BOOKS)
            .join(BOOK_AUTHORS)
            .on(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.ID))
            .join(AUTHORS)
            .on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(AUTHORS.NAME.containsIgnoreCase(authorName))
            .orderBy(BOOKS.ID.asc())
            .fetchInto(BOOKS)
            .map { bookRecord ->
                toBook(bookRecord, findAuthorsByBookId(dslContext, bookRecord.id))
            }

    override fun create(
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book =
        dslContext.transactionResult { configuration ->
            val tx = DSL.using(configuration)
            val authors = findAuthorsOrThrow(tx, authorIds)
            val bookRecord =
                tx
                    .insertInto(BOOKS)
                    .set(BOOKS.TITLE, title)
                    .set(BOOKS.PRICE, price)
                    .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
                    .returning()
                    .fetchOne()
                    ?: error("Failed to insert book")

            authors.forEach { author ->
                tx
                    .insertInto(BOOK_AUTHORS)
                    .set(BOOK_AUTHORS.BOOK_ID, bookRecord.id)
                    .set(BOOK_AUTHORS.AUTHOR_ID, author.id)
                    .execute()
            }

            toBook(bookRecord, authors)
        }

    override fun update(
        bookId: Long,
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book =
        dslContext.transactionResult { configuration ->
            val tx = DSL.using(configuration)

            val bookRecord = updateBook(tx, bookId, title, price, publicationStatus)
            val authors = findAuthorsOrThrow(tx, authorIds)

            tx
                .deleteFrom(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .execute()

            authors.forEach { author ->
                tx
                    .insertInto(BOOK_AUTHORS)
                    .set(BOOK_AUTHORS.BOOK_ID, bookId)
                    .set(BOOK_AUTHORS.AUTHOR_ID, author.id)
                    .execute()
            }

            toBook(bookRecord, authors)
        }

    private fun updateBook(
        dslContext: DSLContext,
        bookId: Long,
        title: String,
        price: Long,
        publicationStatus: PublicationStatus,
    ): BooksRecord =
        dslContext
            .update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(BOOKS.ID.eq(bookId))
            .returning()
            .fetchOne()
            ?: throw BookNotFoundException(bookId)

    private fun findAuthorsOrThrow(
        dslContext: DSLContext,
        authorIds: List<Long>,
    ): List<Author> {
        val authors = findAuthors(dslContext, authorIds)
        val missingAuthorIds = authorIds.toSet() - authors.map { it.id }.toSet()
        if (missingAuthorIds.isNotEmpty()) {
            throw AuthorNotFoundException(missingAuthorIds.sorted())
        }
        return authors
    }

    private fun findAuthors(
        dslContext: DSLContext,
        authorIds: List<Long>,
    ): List<Author> =
        dslContext
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(authorIds))
            .orderBy(AUTHORS.ID.asc())
            .fetch(::toAuthor)

    private fun findAuthorsByBookId(
        dslContext: DSLContext,
        bookId: Long,
    ): List<Author> =
        dslContext
            .selectFrom(AUTHORS)
            .where(
                AUTHORS.ID.`in`(
                    dslContext
                        .select(BOOK_AUTHORS.AUTHOR_ID)
                        .from(BOOK_AUTHORS)
                        .where(BOOK_AUTHORS.BOOK_ID.eq(bookId)),
                ),
            ).orderBy(AUTHORS.ID.asc())
            .fetch(::toAuthor)

    private fun toBook(
        record: BooksRecord,
        authors: List<Author>,
    ): Book =
        Book(
            id = record.id,
            title = record.title,
            price = record.price,
            authors = authors,
            publicationStatus = PublicationStatus.valueOf(record.publicationStatus),
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
        )

    private fun toAuthor(record: AuthorsRecord): Author =
        Author(
            id = record.id,
            name = record.name,
            birthDate = record.birthDate,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
        )
}
