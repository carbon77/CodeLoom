package com.codeloom.executor.service;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.common.event.TestCaseResultDto;
import com.codeloom.executor.engine.CompilationResult;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.engine.RunResult;
import com.codeloom.executor.engine.SubmissionContext;
import com.codeloom.executor.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionProcessingService {
    private final TestCaseRepository testCaseRepository;
    private final DockerJudgeEngine dockerJudgeEngine;
    private final EventService eventService;

    public void process(SubmissionEvent event) {
        var testCases = testCaseRepository.findByProblemId(event.problemId());
        var context = SubmissionContext.fromEvent(event);
        if (testCases.isEmpty()) {
            changeSubmissionStatus(context, SubmissionStatus.ACCEPTED);
            return;
        }

        List<TestCaseResultDto> results = new ArrayList<>();
        try {
            changeSubmissionStatus(context, SubmissionStatus.COMPILING);
            CompilationResult compilationResult = dockerJudgeEngine.compile(context);
            if (!compilationResult.isSuccessful()) {
                changeSubmissionStatus(
                        context,
                        SubmissionStatus.COMPILE_ERROR,
                        SubmissionStatusPayload.builder()
                                .error(compilationResult.stderr())
                                .build()
                );
                return;
            }
            changeSubmissionStatus(context, SubmissionStatus.RUNNING);
            for (var testCase : testCases) {
                RunResult runResult = dockerJudgeEngine.runTestCase(context, testCase);
                results.add(
                        TestCaseResultDto.builder()
                                .id(testCase.getId())
                                .problemId(testCase.getProblemId())
                                .input(testCase.getInput())
                                .expectedOutput(testCase.getExpectedOutput())
                                .stderr(runResult.stderr())
                                .stdout(runResult.stdout())
                                .executionTimeMs(runResult.executionTimeMs())
                                .memoryUsageBytes(runResult.memoryUsageBytes())
                                .build()
                );

                if (runResult.exitCode() != 0) {
                    SubmissionStatus newStatus = runResult.statusFromExitCode();
                    changeSubmissionStatus(
                            context,
                            newStatus,
                            SubmissionStatusPayload.builder()
                                    .error(runResult.stderr())
                                    .testCaseResults(results)
                                    .build()
                    );
                    return;
                }
                if (!runResult.stdout().trim().equals(testCase.getExpectedOutput().trim())) {
                    changeSubmissionStatus(
                            context,
                            SubmissionStatus.WRONG_ANSWER,
                            SubmissionStatusPayload.builder().testCaseResults(results).build()
                    );
                    return;
                }
            }
            changeSubmissionStatus(
                    context,
                    SubmissionStatus.ACCEPTED,
                    SubmissionStatusPayload.builder().testCaseResults(results).build()
            );
        } catch (Exception x) {
            log.error("Error while processing submission={}", context, x);
            changeSubmissionStatus(
                    context,
                    SubmissionStatus.SYSTEM_ERROR,
                    new SubmissionStatusPayload(null, results)
            );
        } finally {
            dockerJudgeEngine.cleanup(context.submissionId());
        }
    }

    public void changeSubmissionStatus(SubmissionContext context, SubmissionStatus newStatus) {
        changeSubmissionStatus(context, newStatus, null);
    }

    public void changeSubmissionStatus(
            SubmissionContext context,
            SubmissionStatus newStatus,
            SubmissionStatusPayload payload) {
        log.info("Submission(id={}) status changed to {}", context.submissionId(), newStatus);
        eventService.submissionStatusChanged(context, newStatus, payload);
    }
}
