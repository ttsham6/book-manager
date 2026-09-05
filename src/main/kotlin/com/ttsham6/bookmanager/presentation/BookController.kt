package com.ttsham6.bookmanager.presentation

import com.ttsham6.bookmanager.domain.Author
import com.ttsham6.bookmanager.domain.Book
import com.ttsham6.bookmanager.domain.PublicationStatus
import com.ttsham6.bookmanager.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
) {
    @GetMapping
    fun search(
        @RequestParam authorName: String,
    ): List<BookResponse> =
        bookService
            .searchByAuthorName(authorName)
            .map(BookResponse::from)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: BookRequest,
    ): BookResponse {
        val book =
            bookService.create(
                title = request.title,
                price = request.price,
                authorIds = request.authorIds,
                publicationStatus = request.publicationStatus,
            )
        return BookResponse.from(book)
    }

    @PutMapping("/{bookId}")
    fun update(
        @PathVariable bookId: Long,
        @RequestBody request: BookRequest,
    ): BookResponse {
        val book =
            bookService.update(
                bookId = bookId,
                title = request.title,
                price = request.price,
                authorIds = request.authorIds,
                publicationStatus = request.publicationStatus,
            )
        return BookResponse.from(book)
    }
}

data class BookRequest(
    val title: String,
    val price: Long,
    val authorIds: List<Long>,
    val publicationStatus: PublicationStatus,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Long,
    val publicationStatus: PublicationStatus,
    val authors: List<AuthorResponse>,
) {
    companion object {
        fun from(book: Book): BookResponse =
            BookResponse(
                id = book.id,
                title = book.title,
                price = book.price,
                publicationStatus = book.publicationStatus,
                authors = book.authors.map(AuthorResponse::from),
            )
    }

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
}
