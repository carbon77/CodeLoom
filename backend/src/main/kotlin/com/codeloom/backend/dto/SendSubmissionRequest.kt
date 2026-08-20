package com.codeloom.backend.dto

import com.codeloom.common.language.ValidLanguage
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SendSubmissionRequest(
    @NotNull
    val problemId: Long,
    @NotBlank
    val code: String,
    @ValidLanguage
    val language: String,
)
