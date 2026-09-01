package com.codeloom.common;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionKafkaEvent(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        String language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitMb) {}
