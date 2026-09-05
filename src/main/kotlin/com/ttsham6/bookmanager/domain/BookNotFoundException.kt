package com.ttsham6.bookmanager.domain

class BookNotFoundException(
    bookId: Long,
) : RuntimeException("Book $bookId was not found")
