package com.ttsham6.bookmanager.service

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.AuthorRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
) {
    fun create(
        name: String,
        birthDate: LocalDate,
    ): Author {
        validate(name, birthDate)
        return authorRepository.create(name, birthDate)
    }

    fun update(
        authorId: Long,
        name: String,
        birthDate: LocalDate,
    ): Author {
        validate(name, birthDate)
        return authorRepository.update(authorId, name, birthDate)
    }

    private fun validate(
        name: String,
        birthDate: LocalDate,
    ) {
        require(name.isNotBlank()) { "name must not be blank" }
        require(!birthDate.isAfter(LocalDate.now())) {
            "birthDate must be today or earlier"
        }
    }
}
