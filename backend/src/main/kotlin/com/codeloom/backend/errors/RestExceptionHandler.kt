package com.codeloom.backend.errors

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleException(e: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = e.statusCode.value(),
            message = e.message,
            timestamp = LocalDateTime.now(),
        )

        return ResponseEntity.status(e.statusCode).body(error)
    }
}