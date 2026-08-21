package com.codeloom.executor.engine;

import com.codeloom.common.language.LanguageSpec;
import java.util.UUID;

public record SubmissionContext(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        LanguageSpec language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes) {}
