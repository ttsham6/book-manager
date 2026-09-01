package com.ttsham6.bookmanager.presentation

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.service.AuthorService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: AuthorRequest,
    ): AuthorResponse {
        val author = authorService.create(request.name, request.birthDate)
        return AuthorResponse.from(author)
    }

    @PutMapping("/{authorId}")
    fun update(
        @PathVariable authorId: Long,
        @RequestBody request: AuthorRequest,
    ): AuthorResponse {
        val author = authorService.update(authorId, request.name, request.birthDate)
        return AuthorResponse.from(author)
    }
}

data class AuthorRequest(
    val name: String,
    val birthDate: LocalDate,
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
) {
    companion object {
        fun from(author: Author): AuthorResponse =
            AuthorResponse(
                id = author.id,
                name = author.name,
                birthDate = author.birthDate,
            )
    }
}
