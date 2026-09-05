package com.ttsham6.bookmanager.domain

class AuthorNotFoundException : RuntimeException {
    constructor(authorId: Long) : super("Author $authorId was not found")

    constructor(authorIds: List<Long>) : super("Authors ${authorIds.joinToString(", ")} were not found")
}
