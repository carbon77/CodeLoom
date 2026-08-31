package com.codeloom.executor.dto;

import com.codeloom.common.SubmissionStatus;
import com.codeloom.executor.engine.CodeExecutionExitCode;
import lombok.Builder;

@Builder
public record RunResult(long exitCode, String stdout, String stderr, long executionTimeMs, long memoryUsageBytes) {
    public SubmissionStatus statusFromExitCode() {
        return CodeExecutionExitCode.statusFor(exitCode);
    }
}
