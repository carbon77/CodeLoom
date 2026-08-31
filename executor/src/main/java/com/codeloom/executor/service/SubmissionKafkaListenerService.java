package com.codeloom.executor.service;

import com.codeloom.common.SubmissionEvent;
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
    private final SubmissionProcessingService submissionProcessingService;

    @KafkaListener(topics = "${codeloom.kafka.topics.submission}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenSubmission(String message) {
        final SubmissionEvent event;
        try {
            event = objectMapper.readValue(message, SubmissionEvent.class);
        } catch (JacksonException e) {
            log.error("Failed to parse event: ", e);
            return;
        }

        log.info("Received submission event: problemId={} userId={}", event.problemId(), event.userId());
        submissionProcessingService.process(event);
    }
}
