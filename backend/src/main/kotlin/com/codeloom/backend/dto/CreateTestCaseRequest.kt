package com.codeloom.backend.dto

data class CreateTestCaseRequest(
    val problemId: Long,
    val input: String,
    val expectedOutput: String,
    val isPublic: Boolean,
)
