package com.codeloom.common;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionEvent(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        String language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes) {
    public SubmissionEvent(UUID submissionId, UUID userId, long problemId, String code, String language) {
        this(submissionId, userId, problemId, code, language, null, null);
    }
}
