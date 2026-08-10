package com.codeloom.backend.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.stream.Collectors

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

    private fun errorResponse(
        e: Exception,
        servlet: HttpServletRequest,
    ): ErrorResponse {
        logger.error(e) {
            "Exception from ${servlet.method} ${servlet.requestURI}"
        }
        return when (e) {
            is AuthenticationException ->
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    message = "Authentication required",
                    timestamp = LocalDateTime.now(),
                    path = servlet.requestURI,
                )

            is AccessDeniedException ->
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    message = "Access denied",
                    timestamp = LocalDateTime.now(),
                    path = servlet.requestURI,
                )

            is ResponseStatusException ->
                ErrorResponse(
                    status = e.statusCode.value(),
                    message = e.message,
                    timestamp = LocalDateTime.now(),
                    path = servlet.requestURI,
                )

            is MethodArgumentNotValidException -> {
                val map =
                    e.allErrors.stream()
                        .collect(
                            Collectors.toMap(
                                ObjectError::getObjectName,
                                ObjectError::getDefaultMessage,
                            ),
                        )
                        .toMap()
                ErrorResponse(
                    status = e.statusCode.value(),
                    message = "Validation failed: ${e.fieldError?.defaultMessage ?: "Unknown error"}",
                    timestamp = LocalDateTime.now(),
                    path = servlet.requestURI,
                    payload = map,
                )
            }

            else ->
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Something went wrong",
                    timestamp = LocalDateTime.now(),
                    path = servlet.requestURI,
                )
        }
    }
}
