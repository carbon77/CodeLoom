package com.codeloom.executor.service;

import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.*;
import com.codeloom.executor.engine.SubmissionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class EventService {
  private final KafkaTemplate<String, String> kafka;
  private final String topic;
  private final ObjectMapper mapper;

  public EventService(
      KafkaTemplate<String, String> k,
      @Value("${codeloom.kafka.topics.submission-status}") String t,
      ObjectMapper m) {
    kafka = k;
    topic = t;
    mapper = m;
  }

  public void submissionStatusChanged(SubmissionContext c, SubmissionStatus s) {
    submissionStatusChanged(c, s, null);
  }

  public void submissionStatusChanged(
      SubmissionContext c, SubmissionStatus s, SubmissionStatusPayload p) {
    var e = new SubmissionStatusChangedEvent(c.submissionId(), c.userId(), c.problemId(), s, p);
    kafka.send(topic, c.submissionId().toString(), mapper.writeValueAsString(e));
  }
}
