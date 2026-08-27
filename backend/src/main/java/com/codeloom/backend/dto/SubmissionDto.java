package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record SubmissionDto(
        UUID submissionId,
        SubmissionStatus status,
        String language,
        String code,
        String errorMessage,
        Instant createdAt,
        List<TestCaseResultListDto> results
) {
}
