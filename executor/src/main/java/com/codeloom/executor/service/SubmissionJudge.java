package com.codeloom.executor.service;

import com.codeloom.common.SubmissionKafkaEvent;
import com.codeloom.common.SubmissionState;
import com.codeloom.common.event.TestCaseResultDto;
import com.codeloom.common.language.LanguageProperties;
import com.codeloom.executor.dto.CompilationResult;
import com.codeloom.executor.dto.RunResult;
import com.codeloom.executor.dto.SubmissionContext;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.model.TestCase;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionJudge {
    private final TestCaseRepository testCaseRepository;
    private final DockerJudgeEngine dockerJudgeEngine;
    private final SubmissionStatePublisher statePublisher;
    private final LanguageProperties languageProperties;

    public void judge(SubmissionKafkaEvent event) {
        SubmissionContext context = context(event);
        try {
            context = context.withLanguage(languageProperties.require(event.language()));
            List<TestCase> testCases = testCaseRepository.findByProblemId(event.problemId());
            if (testCases.isEmpty()) {
                publish(context.withError("No test cases found"), SubmissionState.SYSTEM_ERROR);
                return;
            }

            context = context.withTestCases(testCases);
            context = publish(context, SubmissionState.COMPILING);
            CompilationResult compilation = dockerJudgeEngine.compile(context);
            if (!compilation.isSuccessful()) {
                publish(context.withError(compilation.stderr()), SubmissionState.COMPILE_ERROR);
                return;
            }

            context = publish(context.withError(null), SubmissionState.RUNNING);
            for (TestCase testCase : testCases) {
                RunResult result = dockerJudgeEngine.runTestCase(context, testCase);
                if (testCase.isPublic()) {
                    context = context.addTestResult(result(testCase, result));
                }
                if (result.isFailed()) {
                    publish(context.withError(result.stderr()), result.stateFromExitCode());
                    return;
                }
                if (isWrongAnswer(result, testCase)) {
                    publish(context, SubmissionState.WRONG_ANSWER);
                    return;
                }
            }

            publish(context, SubmissionState.ACCEPTED);
        } catch (Exception e) {
            log.error("Failed to judge submission: submissionId={}", event.submissionId(), e);
            publish(context.withError("Failed to judge submission"), SubmissionState.SYSTEM_ERROR);
        } finally {
            try {
                dockerJudgeEngine.cleanup(event.submissionId());
            } catch (Exception e) {
                log.error("Failed to clean submission resources: submissionId={}", event.submissionId(), e);
            }
        }
    }

    private SubmissionContext context(SubmissionKafkaEvent event) {
        return SubmissionContext.builder()
                .submissionId(event.submissionId())
                .problemId(event.problemId())
                .userId(event.userId())
                .code(event.code())
                .executionTimeLimitMs(event.executionTimeLimitMs())
                .memoryUsageLimitMb(event.memoryUsageLimitMb())
                .testCaseResults(List.of())
                .state(SubmissionState.PENDING)
                .build();
    }

    private SubmissionContext publish(SubmissionContext context, SubmissionState state) {
        SubmissionContext transitioned = context.withState(state);
        statePublisher.submissionStateChanged(transitioned);
        return transitioned;
    }

    private TestCaseResultDto result(TestCase testCase, RunResult result) {
        return TestCaseResultDto.builder()
                .id(testCase.getId())
                .problemId(testCase.getProblemId())
                .input(testCase.getInput())
                .expectedOutput(testCase.getExpectedOutput())
                .stderr(result.stderr())
                .stdout(result.stdout())
                .executionTimeMs(result.executionTimeMs())
                .memoryUsageBytes(result.memoryUsageBytes())
                .build();
    }

    private boolean isWrongAnswer(RunResult result, TestCase testCase) {
        return !result.stdout().trim().equals(testCase.getExpectedOutput().trim());
    }
}
