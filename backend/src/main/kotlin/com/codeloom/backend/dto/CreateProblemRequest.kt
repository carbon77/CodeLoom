package com.codeloom.backend.dto

import jakarta.validation.constraints.NotNull

/**
 * DTO for [com.codeloom.backend.model.Problem]
 */
data class CreateProblemRequest(
    @field:NotNull(message = "Name is mandatory") val name: String
)