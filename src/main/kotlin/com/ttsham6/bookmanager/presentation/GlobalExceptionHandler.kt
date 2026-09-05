package com.ttsham6.bookmanager.presentation

import com.ttsham6.bookmanager.domain.AuthorNotFoundException
import com.ttsham6.bookmanager.domain.BookNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(message = exception.message ?: "Bad request"))

    @ExceptionHandler(AuthorNotFoundException::class)
    fun handleAuthorNotFound(exception: AuthorNotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(message = exception.message ?: "Author not found"))

    @ExceptionHandler(BookNotFoundException::class)
    fun handleBookNotFound(exception: BookNotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(message = exception.message ?: "Book not found"))
}
