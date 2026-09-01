package com.codeloom.executor.dto;

import com.codeloom.common.SubmissionState;
import com.codeloom.executor.engine.CodeExecutionExitCode;
import lombok.Builder;

@Builder
public record RunResult(long exitCode, String stdout, String stderr, long executionTimeMs, long memoryUsageBytes) {
    public SubmissionState stateFromExitCode() {
        return CodeExecutionExitCode.stateFor(exitCode);
    }

    public boolean isFailed() {
        return this.exitCode != 0;
    }
}
