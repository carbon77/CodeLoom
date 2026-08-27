package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionListDto(UUID submissionId, SubmissionStatus status, String language, Instant createdAt) {}
