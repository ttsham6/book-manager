package com.ttsham6.bookmanager.domain

import java.time.LocalDate

interface AuthorRepository {
    fun create(
        name: String,
        birthDate: LocalDate,
    ): Author

    fun upsert(
        authorId: Long,
        name: String,
        birthDate: LocalDate,
    ): Author
}
