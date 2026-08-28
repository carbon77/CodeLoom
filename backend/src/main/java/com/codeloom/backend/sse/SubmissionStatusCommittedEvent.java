package com.codeloom.backend.sse;

import com.codeloom.common.SubmissionStatus;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubmissionStatusCommittedEvent extends ApplicationEvent {
    private final UUID userId;
    private final UUID submissionId;
    private final SubmissionStatus status;

    public SubmissionStatusCommittedEvent(Object source, UUID submissionId, SubmissionStatus status, UUID userId) {
        super(source);
        this.userId = userId;
        this.submissionId = submissionId;
        this.status = status;
    }
}
