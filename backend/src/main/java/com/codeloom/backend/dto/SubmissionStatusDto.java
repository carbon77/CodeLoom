package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SubmissionStatusDto(
        UUID submissionId,
        SubmissionStatus status
) {
}
