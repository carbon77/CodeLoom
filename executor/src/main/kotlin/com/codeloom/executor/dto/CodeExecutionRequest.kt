package com.codeloom.executor.dto

data class CodeExecutionRequest(
    val code: String,
    val language: String,
    val input: String,
    val executionTimeLimitMs: Long? = null,
    val memoryUsageLimitBytes: Long? = null,
)
