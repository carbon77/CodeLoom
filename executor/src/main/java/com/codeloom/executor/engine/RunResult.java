package com.codeloom.executor.engine;

import com.codeloom.common.SubmissionStatus;
import lombok.Builder;

import static com.codeloom.executor.engine.CodeExecutionConstants.MEMORY_LIMIT_EXCEEDED_EXIT_CODE;
import static com.codeloom.executor.engine.CodeExecutionConstants.TIMEOUT_EXIT_CODE;

@Builder
public record RunResult(long exitCode, String stdout, String stderr, long executionTimeMs, long memoryUsageBytes) {
    public SubmissionStatus statusFromExitCode() {
        return switch ((int) exitCode) {
            case TIMEOUT_EXIT_CODE -> SubmissionStatus.TIME_LIMIT_EXCEEDED;
            case MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
            default -> SubmissionStatus.RUNTIME_ERROR;
        };
    }
}
