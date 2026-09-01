package com.codeloom.backend.dto;

import com.codeloom.common.SubmissionState;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionStateDto(UUID submissionId, SubmissionState state) {}
