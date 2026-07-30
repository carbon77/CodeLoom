package com.codeloom.executor.engine

import com.codeloom.executor.languages.LanguageSpec
import java.util.*

data class SubmissionContext(
    val submissionId: UUID,
    val code: String,
    val language: LanguageSpec,
    val executionTimeLimitMs: Long? = null,
    val memoryUsageLimitBytes: Long? = null,
)
