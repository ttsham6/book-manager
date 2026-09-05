package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Book
import com.ttsham6.bookmanager.domain.BookNotFoundException
import com.ttsham6.bookmanager.domain.BookRepository
import com.ttsham6.bookmanager.domain.PublicationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
) {
    fun searchByAuthorName(authorName: String): List<Book> {
        require(authorName.isNotBlank()) { "authorName must not be blank" }
        return bookRepository.findByAuthorName(authorName)
    }

    fun create(
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book {
        validate(title, price, authorIds)
        return bookRepository.create(title, price, authorIds, publicationStatus)
    }

    @Transactional
    fun update(
        bookId: Long,
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book {
        validate(title, price, authorIds)

        val currentBook = bookRepository.findByIdForUpdate(bookId) ?: throw BookNotFoundException(bookId)
        requireCanUpdate(currentBook.publicationStatus, publicationStatus)

        return bookRepository.update(bookId, title, price, authorIds, publicationStatus)
    }

    private fun requireCanUpdate(
        currentPublicationStatus: PublicationStatus,
        nextPublicationStatus: PublicationStatus,
    ) {
        require(
            currentPublicationStatus != PublicationStatus.PUBLISHED ||
                nextPublicationStatus != PublicationStatus.UNPUBLISHED,
        ) {
            "published book cannot be changed to unpublished"
        }
    }

    private fun validate(
        title: String,
        price: Long,
        authorIds: List<Long>,
    ) {
        require(title.isNotBlank()) { "title must not be blank" }
        require(price >= 0) { "price must be greater than or equal to 0" }
        require(authorIds.isNotEmpty()) { "authorIds must not be empty" }
        require(authorIds.distinct().size == authorIds.size) { "authorIds must not contain duplicates" }
    }
}
