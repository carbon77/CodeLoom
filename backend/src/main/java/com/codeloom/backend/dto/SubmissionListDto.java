package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionState;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionListDto(UUID submissionId, SubmissionState state, String language, Instant createdAt) {}
