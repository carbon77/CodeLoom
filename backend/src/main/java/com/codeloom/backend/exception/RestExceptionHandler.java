package com.codeloom.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        ErrorResponse errorResponse = errorResponse(exception, request);
        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    @ExceptionHandler({
            AuthenticationException.class,
            AccessDeniedException.class,
            ResponseStatusException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleKnown(Exception exception, HttpServletRequest request) {
        return handleException(exception, request);
    }

    private ErrorResponse errorResponse(Exception exception, HttpServletRequest request) {
        log.error("Exception from {} {}", request.getMethod(), request.getRequestURI(), exception);
        int statusCode;
        String message;
        Map<String, ?> payload = null;

        switch (exception) {
            case AuthenticationException ignored -> {
                statusCode = 401;
                message = "Authentication required";
            }
            case AccessDeniedException ignored -> {
                statusCode = 403;
                message = "Access denied";
            }
            case ResponseStatusException responseStatusException -> {
                statusCode = responseStatusException.getStatusCode().value();
                message = responseStatusException.getMessage();
            }
            case HttpMessageNotReadableException ignored -> {
                statusCode = 400;
                message = "Body is malformed";
            }
            case MethodArgumentNotValidException x -> {
                statusCode = x.getStatusCode().value();
                message = "Validation failed: "
                        + (x.getFieldError() == null
                        ? "Unknown error"
                        : x.getFieldError().getDefaultMessage());
                payload = x.getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                f -> f.getDefaultMessage() == null ? "" : f.getDefaultMessage(),
                                (a, b) -> b));
            }
            default -> {
                statusCode = 500;
                message = "Something went wrong";
            }
        }
        return ErrorResponse.builder()
                .status(statusCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .payload(payload)
                .build();
    }
}
