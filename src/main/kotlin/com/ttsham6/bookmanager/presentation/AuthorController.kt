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
        @RequestBody request: UpsertAuthorRequest,
    ): AuthorResponse = authorService.create(request.name, request.birthDate).toResponse()

    @PutMapping("/{authorId}")
    fun upsert(
        @PathVariable authorId: Long,
        @RequestBody request: UpsertAuthorRequest,
    ): AuthorResponse = authorService.upsert(authorId, request.name, request.birthDate).toResponse()
}

data class UpsertAuthorRequest(
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

private fun Author.toResponse(): AuthorResponse = AuthorResponse.from(this)
