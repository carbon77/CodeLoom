package com.codeloom.executor.engine;

import com.codeloom.common.language.LanguageSpec;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionContext(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        LanguageSpec language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes) {}
