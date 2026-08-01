package com.codeloom.executor.engine

data class RunResult(
    val exitCode: Long,
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long,
    val memoryUsageBytes: Long,
)