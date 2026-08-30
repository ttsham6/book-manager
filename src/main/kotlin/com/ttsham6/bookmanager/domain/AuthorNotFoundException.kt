package com.ttsham6.bookmanager.domain

class AuthorNotFoundException(
    authorId: Long,
) : RuntimeException("Author $authorId was not found")
