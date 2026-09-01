package com.codeloom.backend.sse;

import com.codeloom.common.SubmissionState;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubmissionStateCommittedEvent extends ApplicationEvent {
    private final UUID userId;
    private final UUID submissionId;
    private final SubmissionState state;

    public SubmissionStateCommittedEvent(Object source, UUID submissionId, SubmissionState state, UUID userId) {
        super(source);
        this.userId = userId;
        this.submissionId = submissionId;
        this.state = state;
    }
}
