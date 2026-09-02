package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.codeloom.common.SubmissionKafkaEvent;
import com.codeloom.common.SubmissionState;
import com.codeloom.common.language.LanguageProperties;
import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.dto.CompilationResult;
import com.codeloom.executor.dto.RunResult;
import com.codeloom.executor.dto.SubmissionContext;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.model.TestCase;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class SubmissionJudgeTest {
    private final TestCaseRepository testCases = mock(TestCaseRepository.class);
    private final DockerJudgeEngine engine = mock(DockerJudgeEngine.class);
    private final SubmissionStatePublisher publisher = mock(SubmissionStatePublisher.class);
    private final LanguageProperties languages = new LanguageProperties(
            Map.of("python", new LanguageSpec("python:3.14-slim", "main.py", null, "python3 main.py")));
    private final SubmissionJudge judge = new SubmissionJudge(testCases, engine, publisher, languages);
    private final UUID submissionId = UUID.randomUUID();
    private final TestCase publicCase = testCase(true, "1", "1");
    private final TestCase hiddenCase = testCase(false, "2", "2");
    private final SubmissionKafkaEvent event = SubmissionKafkaEvent.builder()
            .submissionId(submissionId)
            .userId(UUID.randomUUID())
            .problemId(1)
            .code("print(input())")
            .language("python")
            .executionTimeLimitMs(1000L)
            .memoryUsageLimitMb(1L)
            .build();

    @BeforeEach
    void setUp() {
        when(testCases.findByProblemId(1)).thenReturn(List.of(publicCase, hiddenCase));
        when(engine.compile(any())).thenReturn(new CompilationResult(true, ""));
        when(engine.runTestCase(any(), any())).thenAnswer(invocation -> {
            TestCase testCase = invocation.getArgument(1);
            return run(0, testCase.getExpectedOutput(), "");
        });
    }

    @Test
    void acceptedSubmissionPublishesProgressAndPublicResults() {
        judge.judge(event);

        List<SubmissionContext> events = published(3);
        assertEquals(
                List.of(SubmissionState.COMPILING, SubmissionState.RUNNING, SubmissionState.ACCEPTED),
                events.stream().map(SubmissionContext::state).toList());
        assertEquals(1L, events.getFirst().memoryUsageLimitMb());
        assertEquals(1, events.getLast().testCaseResults().size());
        assertEquals(
                publicCase.getId(),
                events.getLast().testCaseResults().getFirst().id());
        verify(engine).cleanup(submissionId);
    }

    @Test
    void compilationFailurePublishesCompilerErrorAndCleansUp() {
        when(engine.compile(any())).thenReturn(new CompilationResult(false, "compiler failed"));

        judge.judge(event);

        List<SubmissionContext> events = published(2);
        assertEquals(SubmissionState.COMPILE_ERROR, events.getLast().state());
        assertEquals("compiler failed", events.getLast().error());
        verify(engine, never()).runTestCase(any(), any());
        verify(engine).cleanup(submissionId);
    }

    @Test
    void wrongAnswerIncludesFailingPublicResult() {
        doReturn(run(0, "wrong", "")).when(engine).runTestCase(any(), eq(publicCase));

        judge.judge(event);

        SubmissionContext terminal = published(3).getLast();
        assertEquals(SubmissionState.WRONG_ANSWER, terminal.state());
        assertEquals("wrong", terminal.testCaseResults().getFirst().stdout());
    }

    @ParameterizedTest
    @MethodSource("executionFailures")
    void executionFailureUsesExitCodeState(long exitCode, SubmissionState expectedState) {
        doReturn(run(exitCode, "", "execution failed")).when(engine).runTestCase(any(), eq(publicCase));

        judge.judge(event);

        assertEquals(expectedState, published(3).getLast().state());
    }

    @Test
    void hiddenResultsAreNotPublished() {
        doReturn(run(0, "wrong", "")).when(engine).runTestCase(any(), eq(hiddenCase));

        judge.judge(event);

        SubmissionContext terminal = published(3).getLast();
        assertEquals(SubmissionState.WRONG_ANSWER, terminal.state());
        assertEquals(1, terminal.testCaseResults().size());
        assertEquals(publicCase.getId(), terminal.testCaseResults().getFirst().id());
    }

    @Test
    void missingTestCasesPublishesSystemError() {
        when(testCases.findByProblemId(1)).thenReturn(List.of());

        judge.judge(event);

        SubmissionContext terminal = published(1).getFirst();
        assertEquals(SubmissionState.SYSTEM_ERROR, terminal.state());
        assertEquals("No test cases found", terminal.error());
        verify(engine, never()).compile(any());
        verify(engine, never()).runTestCase(any(), any());
        verify(engine).cleanup(submissionId);
    }

    @Test
    void unexpectedFailurePublishesSystemErrorAndCleansUp() {
        when(engine.compile(any())).thenThrow(new IllegalStateException("docker unavailable"));

        judge.judge(event);

        List<SubmissionContext> events = published(2);
        assertEquals(SubmissionState.SYSTEM_ERROR, events.getLast().state());
        assertEquals("Failed to judge submission", events.getLast().error());
        verify(engine).cleanup(submissionId);
    }

    @Test
    void invalidLanguagePublishesSystemErrorAndCleansUp() {
        SubmissionKafkaEvent invalidEvent = SubmissionKafkaEvent.builder()
                .submissionId(submissionId)
                .userId(event.userId())
                .problemId(event.problemId())
                .code(event.code())
                .language("unsupported")
                .build();

        judge.judge(invalidEvent);

        assertEquals(SubmissionState.SYSTEM_ERROR, published(1).getFirst().state());
        verify(engine).cleanup(submissionId);
    }

    @Test
    void cleanupFailureDoesNotPublishAnotherVerdict() {
        doThrow(new IllegalStateException("cleanup failed")).when(engine).cleanup(submissionId);

        judge.judge(event);

        List<SubmissionContext> events = published(3);
        assertEquals(SubmissionState.ACCEPTED, events.getLast().state());
        assertTrue(events.getLast().state().isTerminal());
    }

    private List<SubmissionContext> published(int count) {
        ArgumentCaptor<SubmissionContext> captor = ArgumentCaptor.forClass(SubmissionContext.class);
        verify(publisher, times(count)).submissionStateChanged(captor.capture());
        return captor.getAllValues();
    }

    private TestCase testCase(boolean isPublic, String input, String expectedOutput) {
        return TestCase.builder()
                .id(UUID.randomUUID())
                .problemId(1)
                .input(input)
                .expectedOutput(expectedOutput)
                .isPublic(isPublic)
                .build();
    }

    private RunResult run(long exitCode, String stdout, String stderr) {
        return new RunResult(exitCode, stdout, stderr, 10, 100);
    }

    private static Stream<Arguments> executionFailures() {
        return Stream.of(
                Arguments.of(1L, SubmissionState.RUNTIME_ERROR),
                Arguments.of(124L, SubmissionState.TIME_LIMIT_EXCEEDED),
                Arguments.of(137L, SubmissionState.MEMORY_LIMIT_EXCEEDED));
    }
}
