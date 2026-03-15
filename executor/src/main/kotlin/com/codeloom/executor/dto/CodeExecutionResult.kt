package com.codeloom.executor.dto

data class CodeExecutionResult(
    val stdout: String,
    val stderr: String,
    val duration: Long,
    val exitCode: Int,
    val bytesUsed: Long?,
)