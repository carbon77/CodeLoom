package com.codeloom.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTestCaseRequest(
    @field:NotNull(message = "problemId must not be null")
    val problemId: Long,
    @field:NotBlank(message = "input can't be blank")
    val input: String,
    @field:NotBlank(message = "expectedOutput can't be blank")
    val expectedOutput: String,
    @field:NotNull(message = "isPublic must not be null")
    val isPublic: Boolean,
)
