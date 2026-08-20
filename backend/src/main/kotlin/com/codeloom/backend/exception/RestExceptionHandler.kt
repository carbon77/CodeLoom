package com.codeloom.backend.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(AuthenticationException::class)
    fun handleException(
        e: AuthenticationException,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleException(
        e: AccessDeniedException,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleException(
        e: ResponseStatusException,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(
        e: MethodArgumentNotValidException,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleException(
        e: HttpMessageNotReadableException,
        servlet: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = errorResponse(e, servlet)
        return ResponseEntity.status(error.status).body(error)
    }

    private fun errorResponse(
        e: Exception,
        servlet: HttpServletRequest,
    ): ErrorResponse {
        logger.error(e) {
            "Exception from ${servlet.method} ${servlet.requestURI}"
        }
        var errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Something went wrong",
            timestamp = LocalDateTime.now(),
            path = servlet.requestURI,
        )
        errorResponse = when (e) {
            is AuthenticationException ->
                errorResponse.copy(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    message = "Authentication required",
                )

            is AccessDeniedException ->
                errorResponse.copy(
                    status = HttpStatus.FORBIDDEN.value(),
                    message = "Access denied",
                )

            is ResponseStatusException ->
                errorResponse.copy(
                    status = e.statusCode.value(),
                    message = e.message,
                )

            is HttpMessageNotReadableException ->
                errorResponse.copy(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Body is malformed",
                )

            is MethodArgumentNotValidException -> {
                errorResponse.copy(
                    status = e.statusCode.value(),
                    message = "Validation failed: ${e.fieldError?.defaultMessage ?: "Unknown error"}",
                    payload =
                        e.fieldErrors.associate { fieldError ->
                            fieldError.field to fieldError.defaultMessage
                        },
                )
            }

            else -> errorResponse
        }

        return errorResponse
    }
}
