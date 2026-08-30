package com.ttsham6.bookmanager.domain

import java.time.LocalDate
import java.time.OffsetDateTime

data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
