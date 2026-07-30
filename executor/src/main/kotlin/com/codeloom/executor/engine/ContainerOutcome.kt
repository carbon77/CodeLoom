package com.codeloom.executor.engine

data class ContainerOutcome(
    val exitCode: Long,
    val stdout: String,
    val stderr: String,
    val memoryUsageBytes: Long,
    val executionTimeMs: Long,
)