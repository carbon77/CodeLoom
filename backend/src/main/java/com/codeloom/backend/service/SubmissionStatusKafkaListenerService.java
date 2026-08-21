package com.codeloom.backend.service;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.model.TestCaseResult;
import com.codeloom.common.event.SubmissionStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionStatusKafkaListenerService {
    private final SubmissionRepository submissionRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${codeloom.kafka.submission-status-topic}")
    public void listenSubmissionStatus(String message) {
        try {
            SubmissionStatusChangedEvent event = objectMapper.readValue(message, SubmissionStatusChangedEvent.class);
            Optional<Submission> submission = submissionRepository.findById(event.submissionId());
            if (submission.isEmpty()) {
                log.warn("Submission not found: submissionId={}", event.submissionId());
                return;
            }

            submissionRepository.save(submission.get().withStatus(event.newStatus()));
            if (event.payload() != null && event.payload().testCaseResults() != null) {
                testCaseResultRepository.deleteBySubmissionId(event.submissionId());
                testCaseResultRepository.saveAll(event.payload().testCaseResults().stream()
                        .map(result -> TestCaseResult.builder()
                                .id(result.id())
                                .submissionId(submission.get().getId())
                                .input(result.input())
                                .stdout(result.stdout())
                                .stderr(result.stderr())
                                .bytesUsed(result.memoryUsageBytes())
                                .executionTimeMs(result.executionTimeMs())
                                .expectedOutput(result.expectedOutput())
                                .build())
                        .toList());
            }
        } catch (JacksonException e) {
            log.error("Failed to parse submission status event", e);
        }
    }
}
