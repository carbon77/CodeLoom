package com.codeloom.executor.dto

data class CodeExecutionResult(
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Long,
    val executionTimeMs: Long,
    val memoryUsageBytes: Long,
)