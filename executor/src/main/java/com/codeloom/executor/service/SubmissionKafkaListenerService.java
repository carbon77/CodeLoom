package com.codeloom.executor.service;

import com.codeloom.common.SubmissionKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionKafkaListenerService {
    private final ObjectMapper objectMapper;
    private final SubmissionJudge submissionJudge;

    @KafkaListener(topics = "${codeloom.kafka.topics.submission}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenSubmission(String message) {
        final SubmissionKafkaEvent event;
        try {
            event = objectMapper.readValue(message, SubmissionKafkaEvent.class);
        } catch (JacksonException e) {
            log.error("Failed to parse event: ", e);
            return;
        }

        log.info("Received submission event: problemId={} userId={}", event.problemId(), event.userId());
        submissionJudge.judge(event);
    }
}
