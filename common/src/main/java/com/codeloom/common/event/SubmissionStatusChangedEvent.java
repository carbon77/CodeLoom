package com.codeloom.common.event;

import com.codeloom.common.SubmissionStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionStatusChangedEvent(
        UUID submissionId, UUID userId, long problemId, SubmissionStatus newStatus, SubmissionStatusPayload payload) {}
