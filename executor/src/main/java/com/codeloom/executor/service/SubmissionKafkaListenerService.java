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
    private final ObjectMapper mapper;
    private final SubmissionProcessingService processing;

    @KafkaListener(topics = "submissions", groupId = "codeloom")
    public void listenSubmission(String message) {
        try {
            SubmissionEvent e = mapper.readValue(message, SubmissionEvent.class);
            log.info("Received submission event: problemId={} userId={}", e.problemId(), e.userId());
            processing.process(e);
        } catch (JacksonException e) {
            log.error("Failed to parse event: ", e);
        }
    }
}
