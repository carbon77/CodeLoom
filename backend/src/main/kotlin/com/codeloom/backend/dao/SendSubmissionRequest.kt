package com.codeloom.backend.dao

data class SendSubmissionRequest(
    val problemId: Long,
    val code: String,
    val language: String,
)
