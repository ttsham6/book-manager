package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Book
import com.ttsham6.bookmanager.domain.BookNotFoundException
import com.ttsham6.bookmanager.domain.BookRepository
import com.ttsham6.bookmanager.domain.PublicationStatus
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
) {
    fun create(
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book {
        validate(title, price, authorIds)
        return bookRepository.create(title, price, authorIds, publicationStatus)
    }

    fun update(
        bookId: Long,
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book {
        validate(title, price, authorIds)
        val currentBook = bookRepository.findById(bookId) ?: throw BookNotFoundException(bookId)
        require(
            currentBook.publicationStatus != PublicationStatus.PUBLISHED ||
                publicationStatus != PublicationStatus.UNPUBLISHED,
        ) {
            "published book cannot be changed to unpublished"
        }
        return bookRepository.update(bookId, title, price, authorIds, publicationStatus)
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
