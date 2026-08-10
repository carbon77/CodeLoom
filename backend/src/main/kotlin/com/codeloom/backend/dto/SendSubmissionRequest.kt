package com.codeloom.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SendSubmissionRequest(
    @NotNull
    val problemId: Long,
    @NotBlank
    val code: String,
    @NotBlank
    val language: String,
)
