package com.codeloom.common.event;

import com.codeloom.common.SubmissionState;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionStateChangedEvent(
        UUID submissionId, UUID userId, long problemId, SubmissionState newState, SubmissionStatePayload payload) {}
