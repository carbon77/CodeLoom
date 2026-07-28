package com.codeloom.backend.model

data class ProblemConstraints(
    val executionTimeLimitMs: Long? = null,
    val memoryUsageLimitBytes: Long? = null,
)