package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.codeloom.common.*;
import com.codeloom.common.event.*;
import com.codeloom.executor.engine.*;
import com.codeloom.executor.model.TestCase;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

class SubmissionProcessingPayloadTest {
  TestCaseRepository repo = mock(TestCaseRepository.class);
  DockerJudgeEngine judge = mock(DockerJudgeEngine.class);
  EventService events = mock(EventService.class);
  SubmissionProcessingService service = new SubmissionProcessingService(repo, judge, events);
  UUID submissionId = UUID.randomUUID();
  long problemId = 1;
  TestCase publicCase = new TestCase(UUID.randomUUID(), problemId, "1", "1", true),
      hiddenCase = new TestCase(UUID.randomUUID(), problemId, "2", "2", false);
  SubmissionEvent event =
      new SubmissionEvent(submissionId, UUID.randomUUID(), problemId, "print(input())", "python");

  @BeforeEach
  void setup() {
    reset(repo, judge, events);
    when(judge.compile(any())).thenReturn(new CompilationResult(true, ""));
  }

  @Test
  void acceptedContainsOnlyPublicResults() {
    when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase, hiddenCase));
    when(judge.runTestCase(any(), any()))
        .thenAnswer(
            i -> {
              TestCase t = i.getArgument(1);
              return new RunResult(0, t.getExpectedOutput(), "", 10, 100);
            });
    service.process(event);
    SubmissionStatusPayload p = payload(SubmissionStatus.ACCEPTED);
    assertEquals(1, p.testCaseResults().size());
    assertEquals(publicCase.getId(), p.testCaseResults().getFirst().id());
  }

  @Test
  void wrongAnswerContainsFailingResult() {
    when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
    when(judge.runTestCase(any(), any())).thenReturn(new RunResult(0, "0", "", 10, 100));
    service.process(event);
    assertEquals("0", payload(SubmissionStatus.WRONG_ANSWER).testCaseResults().getFirst().stdout());
  }

  @Test
  void comparisonIgnoresWhitespace() {
    when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
    when(judge.runTestCase(any(), any())).thenReturn(new RunResult(0, "\t1  \r\n", "", 10, 100));
    service.process(event);
    assertEquals(
        "\t1  \r\n", payload(SubmissionStatus.ACCEPTED).testCaseResults().getFirst().stdout());
  }

  @Test
  void compileErrorHasNoPayload() {
    when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
    when(judge.compile(any())).thenReturn(new CompilationResult(false, "compile error"));
    service.process(event);
    verify(events).submissionStatusChanged(any(), eq(SubmissionStatus.COMPILE_ERROR), isNull());
  }

  private SubmissionStatusPayload payload(SubmissionStatus s) {
    ArgumentCaptor<SubmissionStatusPayload> c =
        ArgumentCaptor.forClass(SubmissionStatusPayload.class);
    verify(events).submissionStatusChanged(any(), eq(s), c.capture());
    return c.getValue();
  }
}
