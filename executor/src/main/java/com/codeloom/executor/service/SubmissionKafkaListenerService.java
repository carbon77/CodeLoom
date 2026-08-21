package com.codeloom.executor.service;

import com.codeloom.common.SubmissionEvent;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class SubmissionKafkaListenerService {
  private static final Logger log = LoggerFactory.getLogger(SubmissionKafkaListenerService.class);
  private final ObjectMapper mapper;
  private final SubmissionProcessingService processing;

  public SubmissionKafkaListenerService(ObjectMapper m, SubmissionProcessingService p) {
    mapper = m;
    processing = p;
  }

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
