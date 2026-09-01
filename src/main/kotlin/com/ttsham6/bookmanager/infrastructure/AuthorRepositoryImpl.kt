package com.ttsham6.bookmanager.infrastructure

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.AuthorNotFoundException
import com.ttsham6.bookmanager.domain.AuthorRepository
import com.ttsham6.bookmanager.jooq.Tables.AUTHORS
import com.ttsham6.bookmanager.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext,
) : AuthorRepository {
    override fun create(
        name: String,
        birthDate: LocalDate,
    ): Author =
        dslContext
            .insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returning()
            .fetchOne(::toAuthor)
            ?: error("Failed to insert author")

    override fun update(
        authorId: Long,
        name: String,
        birthDate: LocalDate,
    ): Author =
        dslContext
            .update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .set(AUTHORS.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(AUTHORS.ID.eq(authorId))
            .returning()
            .fetchOne(::toAuthor)
            ?: throw AuthorNotFoundException(authorId)

    private fun toAuthor(record: AuthorsRecord): Author =
        Author(
            id = record.id,
            name = record.name,
            birthDate = record.birthDate,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
        )
}
