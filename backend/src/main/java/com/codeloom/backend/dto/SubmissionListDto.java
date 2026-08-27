package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record SubmissionListDto(
        UUID submissionId,
        SubmissionStatus status,
        String language,
        Instant createdAt
) {
}
