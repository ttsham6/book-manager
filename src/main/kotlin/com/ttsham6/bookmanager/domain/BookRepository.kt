package com.ttsham6.bookmanager.domain

interface BookRepository {
    fun findById(bookId: Long): Book?

    fun create(
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book

    fun update(
        bookId: Long,
        title: String,
        price: Long,
        authorIds: List<Long>,
        publicationStatus: PublicationStatus,
    ): Book
}
