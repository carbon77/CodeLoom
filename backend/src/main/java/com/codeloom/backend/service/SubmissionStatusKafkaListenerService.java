package com.codeloom.backend.service;

import com.codeloom.backend.dao.submission.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.model.TestCaseResult;
import com.codeloom.common.event.SubmissionStatusChangedEvent;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionStatusKafkaListenerService {
    private final SubmissionRepository submissionRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${codeloom.kafka.submission-status-topic}")
    @Transactional
    public void listenSubmissionStatus(String message) {
        try {
            SubmissionStatusChangedEvent event = objectMapper.readValue(message, SubmissionStatusChangedEvent.class);
            Optional<Submission> submissionOptional = submissionRepository.findById(event.submissionId());
            if (submissionOptional.isEmpty()) {
                log.warn("Submission not found: submissionId={}", event.submissionId());
                return;
            }

            Submission submission = submissionOptional.get().withStatus(event.newStatus());
            if (event.payload() != null && event.payload().error() != null) {
                submission = submission.withErrorMessage(event.payload().error());
            }
            submissionRepository.save(submission);

            if (event.payload() != null && event.payload().testCaseResults() != null) {
                testCaseResultRepository.deleteBySubmissionId(event.submissionId());
                testCaseResultRepository.saveAll(event.payload().testCaseResults().stream()
                        .map(result -> TestCaseResult.builder()
                                .id(result.id())
                                .submissionId(submissionOptional.get().getId())
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
