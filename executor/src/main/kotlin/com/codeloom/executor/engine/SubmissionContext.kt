package com.codeloom.executor.engine

import com.codeloom.common.language.LanguageSpec
import java.util.*

data class SubmissionContext(
    val submissionId: UUID,
    val userId: UUID,
    val problemId: Long,
    val code: String,
    val language: LanguageSpec,
    val executionTimeLimitMs: Long? = null,
    val memoryUsageLimitBytes: Long? = null,
)
