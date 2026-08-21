package com.codeloom.executor.service;

import static com.codeloom.executor.engine.CodeExecutionConstants.*;

import com.codeloom.common.*;
import com.codeloom.common.event.*;
import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.engine.*;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;

@Service
public class SubmissionProcessingService {
  private static final Logger log = LoggerFactory.getLogger(SubmissionProcessingService.class);
  private final TestCaseRepository tests;
  private final DockerJudgeEngine judge;
  private final EventService events;

  public SubmissionProcessingService(TestCaseRepository t, DockerJudgeEngine j, EventService e) {
    tests = t;
    judge = j;
    events = e;
  }

  public void process(SubmissionEvent e) {
    var cases = tests.findByProblemId(e.problemId());
    var c =
        new SubmissionContext(
            e.submissionId(),
            e.userId(),
            e.problemId(),
            e.code(),
            LanguageSpec.fromLanguage(e.language()),
            e.executionTimeLimitMs(),
            e.memoryUsageLimitBytes());
    if (cases.isEmpty()) {
      changeSubmissionStatus(c, SubmissionStatus.ACCEPTED);
      return;
    }
    List<TestCaseResult> results = new ArrayList<>();
    try {
      changeSubmissionStatus(c, SubmissionStatus.COMPILING);
      if (!judge.compile(c).isSuccessful()) {
        changeSubmissionStatus(c, SubmissionStatus.COMPILE_ERROR);
        return;
      }
      changeSubmissionStatus(c, SubmissionStatus.RUNNING);
      for (var t : cases) {
        RunResult r = judge.runTestCase(c, t);
        if (t.isPublic())
          results.add(
              new TestCaseResult(
                  t.getId(),
                  t.getProblemId(),
                  t.getInput(),
                  t.getExpectedOutput(),
                  r.stdout(),
                  r.stderr(),
                  r.executionTimeMs(),
                  r.memoryUsageBytes()));
        if (r.exitCode() != 0) {
          SubmissionStatus s =
              r.exitCode() == TIMEOUT_EXIT_CODE
                  ? SubmissionStatus.TIME_LIMIT_EXCEEDED
                  : r.exitCode() == MEMORY_LIMIT_EXCEEDED_EXIT_CODE
                      ? SubmissionStatus.MEMORY_LIMIT_EXCEEDED
                      : SubmissionStatus.RUNTIME_ERROR;
          changeSubmissionStatus(c, s, new SubmissionStatusPayload(null, results));
          return;
        }
        if (!r.stdout().trim().equals(t.getExpectedOutput().trim())) {
          changeSubmissionStatus(
              c, SubmissionStatus.WRONG_ANSWER, new SubmissionStatusPayload(null, results));
          return;
        }
      }
      changeSubmissionStatus(
          c, SubmissionStatus.ACCEPTED, new SubmissionStatusPayload(null, results));
    } catch (Exception x) {
      log.error("Error while processing submission={}", c, x);
      changeSubmissionStatus(
          c, SubmissionStatus.SYSTEM_ERROR, new SubmissionStatusPayload(null, results));
    } finally {
      judge.cleanup(c.submissionId());
    }
  }

  public void changeSubmissionStatus(SubmissionContext c, SubmissionStatus s) {
    changeSubmissionStatus(c, s, null);
  }

  public void changeSubmissionStatus(
      SubmissionContext c, SubmissionStatus s, SubmissionStatusPayload p) {
    log.info("Submission(id={}) status changed to {}", c.submissionId(), s);
    events.submissionStatusChanged(c, s, p);
  }
}
