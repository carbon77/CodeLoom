package com.codeloom.backend.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
data class ErrorResponse(
    @field:Schema(description = "HTTP status code", example = "400")
    val status: Int,
    @field:Schema(description = "Human-readable error message", example = "Validation failed: Title is mandatory")
    val message: String,
    @field:Schema(description = "Time at which the error occurred", format = "date-time")
    val timestamp: LocalDateTime,
    @field:Schema(description = "Request path that produced the error", example = "/v1/problems")
    val path: String,
    @field:Schema(description = "Optional structured details about the error", nullable = true)
    val payload: Map<String, *>? = null,
)
