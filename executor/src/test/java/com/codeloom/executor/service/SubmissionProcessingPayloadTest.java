package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.executor.engine.CompilationResult;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.engine.RunResult;
import com.codeloom.executor.model.TestCase;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SubmissionProcessingPayloadTest {
    private final TestCaseRepository repo = mock(TestCaseRepository.class);
    private final DockerJudgeEngine judge = mock(DockerJudgeEngine.class);
    private final EventService events = mock(EventService.class);

    private final SubmissionProcessingService service = new SubmissionProcessingService(repo, judge, events);

    private final UUID submissionId = UUID.randomUUID();
    private final long problemId = 1;

    private final TestCase publicCase = TestCase.builder()
            .id(UUID.randomUUID())
            .problemId(problemId)
            .input("1")
            .expectedOutput("1")
            .isPublic(true)
            .build();
    private final TestCase hiddenCase = TestCase.builder()
            .id(UUID.randomUUID())
            .problemId(problemId)
            .input("2")
            .expectedOutput("2")
            .isPublic(false)
            .build();
    private final SubmissionEvent event = SubmissionEvent.builder()
            .submissionId(submissionId)
            .userId(UUID.randomUUID())
            .problemId(problemId)
            .code("print(input())")
            .language("python")
            .build();

    @BeforeEach
    void setup() {
        reset(repo, judge, events);
        when(judge.compile(any()))
                .thenReturn(CompilationResult.builder()
                        .isSuccessful(true)
                        .stderr("")
                        .build());
    }

    @Test
    void acceptedContainsOnlyPublicResults() {
        when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase, hiddenCase));
        when(judge.runTestCase(any(), any())).thenAnswer(invocation -> {
            TestCase testCase = invocation.getArgument(1);
            return RunResult.builder()
                    .exitCode(0)
                    .stdout(testCase.getExpectedOutput())
                    .stderr("")
                    .executionTimeMs(10)
                    .memoryUsageBytes(100)
                    .build();
        });
        service.process(event);
        SubmissionStatusPayload payload = payload(SubmissionStatus.ACCEPTED);
        assertEquals(1, payload.testCaseResults().size());
        assertEquals(publicCase.getId(), payload.testCaseResults().getFirst().id());
    }

    @Test
    void wrongAnswerContainsFailingResult() {
        when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
        var result = RunResult.builder()
                .exitCode(0)
                .stdout("0")
                .stderr("")
                .executionTimeMs(10)
                .memoryUsageBytes(100)
                .build();
        when(judge.runTestCase(any(), any())).thenReturn(result);
        service.process(event);
        assertEquals(
                "0",
                payload(SubmissionStatus.WRONG_ANSWER)
                        .testCaseResults()
                        .getFirst()
                        .stdout());
    }

    @Test
    void comparisonIgnoresWhitespace() {
        when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
        var result = RunResult.builder()
                .exitCode(0)
                .stdout("\t1  \r\n")
                .stderr("")
                .executionTimeMs(10)
                .memoryUsageBytes(100)
                .build();
        when(judge.runTestCase(any(), any())).thenReturn(result);
        service.process(event);
        assertEquals(
                "\t1  \r\n",
                payload(SubmissionStatus.ACCEPTED).testCaseResults().getFirst().stdout());
    }

    @Test
    void compileErrorHasNoPayload() {
        when(repo.findByProblemId(problemId)).thenReturn(List.of(publicCase));
        when(judge.compile(any()))
                .thenReturn(CompilationResult.builder()
                        .isSuccessful(false)
                        .stderr("compile error")
                        .build());
        service.process(event);
        verify(events).submissionStatusChanged(any(), eq(SubmissionStatus.COMPILE_ERROR), isNull());
    }

    private SubmissionStatusPayload payload(SubmissionStatus s) {
        ArgumentCaptor<SubmissionStatusPayload> captor = ArgumentCaptor.forClass(SubmissionStatusPayload.class);
        verify(events).submissionStatusChanged(any(), eq(s), captor.capture());
        return captor.getValue();
    }
}
