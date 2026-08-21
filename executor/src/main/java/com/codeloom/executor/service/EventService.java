package com.codeloom.executor.service;

import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusChangedEvent;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.executor.engine.SubmissionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class EventService {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    @Value("${codeloom.kafka.topics.submission-status}")
    private final String topic;

    public void submissionStatusChanged(SubmissionContext c, SubmissionStatus s) {
        submissionStatusChanged(c, s, null);
    }

    public void submissionStatusChanged(
            SubmissionContext context, SubmissionStatus newStatus, SubmissionStatusPayload payload) {
        var event = SubmissionStatusChangedEvent.builder()
                .submissionId(context.submissionId())
                .userId(context.userId())
                .problemId(context.problemId())
                .newStatus(newStatus)
                .payload(payload)
                .build();
        kafka.send(topic, context.submissionId().toString(), mapper.writeValueAsString(event));
    }
}
