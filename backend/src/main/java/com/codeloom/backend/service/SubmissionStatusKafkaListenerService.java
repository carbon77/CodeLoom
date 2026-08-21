package com.codeloom.backend.service;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.*;
import com.codeloom.common.event.SubmissionStatusChangedEvent;
import java.util.*;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class SubmissionStatusKafkaListenerService {
    private static final Logger log = LoggerFactory.getLogger(SubmissionStatusKafkaListenerService.class);
    private final SubmissionRepository submissions;
    private final TestCaseResultRepository results;
    private final ObjectMapper mapper;

    public SubmissionStatusKafkaListenerService(SubmissionRepository s, TestCaseResultRepository r, ObjectMapper m) {
        submissions = s;
        results = r;
        mapper = m;
    }

    @KafkaListener(topics = "${codeloom.kafka.submission-status-topic}")
    public void listenSubmissionStatus(String message) {
        try {
            SubmissionStatusChangedEvent e = mapper.readValue(message, SubmissionStatusChangedEvent.class);
            Optional<Submission> s = submissions.findById(e.submissionId());
            if (s.isEmpty()) {
                log.warn("Submission not found: submissionId={}", e.submissionId());
                return;
            }
            submissions.save(s.get().withStatus(e.newStatus()));
            if (e.payload() != null && e.payload().testCaseResults() != null) {
                results.deleteBySubmissionId(e.submissionId());
                results.saveAll(e.payload().testCaseResults().stream()
                        .map(r -> new TestCaseResult(
                                e.submissionId(),
                                r.input(),
                                r.expectedOutput(),
                                r.stdout(),
                                r.stderr(),
                                r.executionTimeMs(),
                                r.memoryUsageBytes()))
                        .toList());
            }
        } catch (JacksonException ex) {
            log.error("Failed to parse submission status event", ex);
        }
    }
}
