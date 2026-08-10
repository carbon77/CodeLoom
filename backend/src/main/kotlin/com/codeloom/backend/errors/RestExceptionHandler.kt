package com.codeloom.backend.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handleException(e: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = e.statusCode.value(),
                message = e.message,
                timestamp = LocalDateTime.now(),
            )

        return ResponseEntity.status(e.statusCode).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = e.statusCode.value(),
                message = "Validation failed: ${e.fieldError?.defaultMessage ?: "Unknown error"}",
                timestamp = LocalDateTime.now(),
            )

        return ResponseEntity.status(e.statusCode).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = e.message ?: "something went wrong",
                timestamp = LocalDateTime.now(),
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
}
