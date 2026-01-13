package com.codeloom.backend.dto

import jakarta.validation.constraints.NotNull

/**
 * DTO for [com.codeloom.backend.model.Problem]
 */
data class CreateProblemRequest(
    @field:NotNull(message = "Title is mandatory") val title: String
)