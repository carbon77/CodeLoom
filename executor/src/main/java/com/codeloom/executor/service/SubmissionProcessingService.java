package com.codeloom.executor.service;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.common.event.TestCaseResultDto;
import com.codeloom.executor.dto.CompilationResult;
import com.codeloom.executor.dto.RunResult;
import com.codeloom.executor.dto.SubmissionContext;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            context = context.withStatus(SubmissionStatus.ACCEPTED);
            sendChangeStatusEvent(context);
            return;
        }

        List<TestCaseResultDto> results = new ArrayList<>();
        try {
            context = context.withStatus(SubmissionStatus.COMPILING);
            sendChangeStatusEvent(context);

            CompilationResult compilationResult = dockerJudgeEngine.compile(context);
            if (!compilationResult.isSuccessful()) {
                context = context.withStatus(SubmissionStatus.COMPILE_ERROR);
                sendChangeStatusEvent(
                        context,
                        SubmissionStatusPayload.builder()
                                .error(compilationResult.stderr())
                                .build());
                return;
            }

            context = context.withStatus(SubmissionStatus.RUNNING);
            sendChangeStatusEvent(context);

            for (var testCase : testCases) {
                RunResult runResult = dockerJudgeEngine.runTestCase(context, testCase);
                if (testCase.isPublic()) {
                    results.add(TestCaseResultDto.builder()
                            .id(testCase.getId())
                            .problemId(testCase.getProblemId())
                            .input(testCase.getInput())
                            .expectedOutput(testCase.getExpectedOutput())
                            .stderr(runResult.stderr())
                            .stdout(runResult.stdout())
                            .executionTimeMs(runResult.executionTimeMs())
                            .memoryUsageBytes(runResult.memoryUsageBytes())
                            .build());
                }

                if (runResult.exitCode() != 0) {
                    SubmissionStatus newStatus = runResult.statusFromExitCode();
                    context = context.withStatus(newStatus);
                    sendChangeStatusEvent(
                            context,
                            SubmissionStatusPayload.builder()
                                    .error(runResult.stderr())
                                    .testCaseResults(results)
                                    .build());
                    return;
                }

                if (!runResult
                        .stdout()
                        .trim()
                        .equals(testCase.getExpectedOutput().trim())) {
                    context = context.withStatus(SubmissionStatus.WRONG_ANSWER);
                    sendChangeStatusEvent(
                            context,
                            SubmissionStatusPayload.builder()
                                    .testCaseResults(results)
                                    .build());
                    return;
                }
            }

            context = context.withStatus(SubmissionStatus.ACCEPTED);
            sendChangeStatusEvent(
                    context,
                    SubmissionStatusPayload.builder().testCaseResults(results).build());
        } catch (Exception x) {
            log.error("Error while processing submission={}", context, x);
            context = context.withStatus(SubmissionStatus.SYSTEM_ERROR);
            sendChangeStatusEvent(context, new SubmissionStatusPayload(null, results));
        } finally {
            dockerJudgeEngine.cleanup(context.submissionId());
        }
    }

    public void sendChangeStatusEvent(SubmissionContext context) {
        sendChangeStatusEvent(context, null);
    }

    public void sendChangeStatusEvent(SubmissionContext context, SubmissionStatusPayload payload) {
        log.info("Submission(id={}) status changed to {}", context.submissionId(), context.status());
        eventService.submissionStatusChanged(context, payload);
    }
}
