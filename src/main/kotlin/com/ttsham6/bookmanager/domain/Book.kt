package com.ttsham6.bookmanager.domain

import java.time.OffsetDateTime

data class Book(
    val id: Long,
    val title: String,
    val price: Long,
    val authors: List<Author>,
    val publicationStatus: PublicationStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
}
