package com.codeloom.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public record ErrorResponse(
    int status, String message, LocalDateTime timestamp, String path, Map<String, ?> payload) {}
