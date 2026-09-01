package com.codeloom.executor.service;

import com.codeloom.common.event.SubmissionStateChangedEvent;
import com.codeloom.common.event.SubmissionStatePayload;
import com.codeloom.executor.dto.SubmissionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SubmissionStatePublisher {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    @Value("${codeloom.kafka.topics.submission-state}")
    private String topic;

    public void submissionStateChanged(SubmissionContext context) {
        var event = SubmissionStateChangedEvent.builder()
                .submissionId(context.submissionId())
                .userId(context.userId())
                .problemId(context.problemId())
                .newState(context.state())
                .payload(SubmissionStatePayload.builder()
                        .error(context.error())
                        .testCaseResults(context.testCaseResults())
                        .build())
                .build();
        kafka.send(topic, context.submissionId().toString(), mapper.writeValueAsString(event));
    }
}
