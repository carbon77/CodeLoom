package com.codeloom.backend.it;

import static org.junit.jupiter.api.Assertions.*;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.*;
import com.codeloom.backend.service.SubmissionStatusKafkaListenerService;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.ObjectMapper;

@Sql(
    statements = "TRUNCATE TABLE problems CASCADE",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SubmissionStatusConsumerIT extends BackendIntegrationTestSupport {
  @Autowired ProblemRepository problems;
  @Autowired SubmissionRepository submissions;
  @Autowired TestCaseResultRepository results;
  @Autowired SubmissionStatusKafkaListenerService listener;
  @Autowired ObjectMapper mapper;

  @Test
  void updatesStatusAndPersistsResults() {
    Problem p = problems.save(new Problem("Sum", "sum"));
    UUID user = UUID.randomUUID();
    Submission s =
        submissions.save(
            new Submission(user, p.getId(), "code", SubmissionStatus.PENDING, "python"));
    var result =
        new com.codeloom.common.event.TestCaseResult(
            UUID.randomUUID(), p.getId(), "1 2", "3", "3", "", 10, 100);
    var event =
        new SubmissionStatusChangedEvent(
            s.getId(),
            user,
            p.getId(),
            SubmissionStatus.ACCEPTED,
            new SubmissionStatusPayload(null, List.of(result)));
    listener.listenSubmissionStatus(mapper.writeValueAsString(event));
    assertEquals(
        SubmissionStatus.ACCEPTED, submissions.findById(s.getId()).orElseThrow().getStatus());
    assertEquals(1, ((Collection<?>) results.findAll()).size());
  }
}
