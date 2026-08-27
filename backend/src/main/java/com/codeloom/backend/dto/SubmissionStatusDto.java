package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionStatusDto(UUID submissionId, SubmissionStatus status) {}
