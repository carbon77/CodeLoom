package com.codeloom.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class RestExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest r) {
    ErrorResponse x = errorResponse(e, r);
    return ResponseEntity.status(x.status()).body(x);
  }

  @ExceptionHandler({
    AuthenticationException.class,
    AccessDeniedException.class,
    ResponseStatusException.class,
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorResponse> handleKnown(Exception e, HttpServletRequest r) {
    return handleException(e, r);
  }

  private ErrorResponse errorResponse(Exception e, HttpServletRequest r) {
    log.error("Exception from {} {}", r.getMethod(), r.getRequestURI(), e);
    int s = 500;
    String m = "Something went wrong";
    java.util.Map<String, ?> p = null;
    if (e instanceof AuthenticationException) {
      s = 401;
      m = "Authentication required";
    } else if (e instanceof AccessDeniedException) {
      s = 403;
      m = "Access denied";
    } else if (e instanceof ResponseStatusException x) {
      s = x.getStatusCode().value();
      m = x.getMessage();
    } else if (e instanceof HttpMessageNotReadableException) {
      s = 400;
      m = "Body is malformed";
    } else if (e instanceof MethodArgumentNotValidException x) {
      s = x.getStatusCode().value();
      m =
          "Validation failed: "
              + (x.getFieldError() == null
                  ? "Unknown error"
                  : x.getFieldError().getDefaultMessage());
      p =
          x.getFieldErrors().stream()
              .collect(
                  Collectors.toMap(
                      f -> f.getField(),
                      f -> f.getDefaultMessage() == null ? "" : f.getDefaultMessage(),
                      (a, b) -> b));
    }
    return new ErrorResponse(s, m, LocalDateTime.now(), r.getRequestURI(), p);
  }
}
