package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionDto(
        UUID submissionId,
        SubmissionState state,
        String language,
        String code,
        String errorMessage,
        Instant createdAt,
        List<TestCaseResultListDto> results) {}
