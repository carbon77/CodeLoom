package com.codeloom.executor.engine;

import com.codeloom.common.SubmissionState;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CodeExecutionExitCode {
    ERROR(1, SubmissionState.RUNTIME_ERROR, null),
    TIMEOUT(124, SubmissionState.TIME_LIMIT_EXCEEDED, "Execution timed out"),
    OUTPUT_LIMIT(125, SubmissionState.RUNTIME_ERROR, null),
    MEMORY_LIMIT_EXCEEDED(137, SubmissionState.MEMORY_LIMIT_EXCEEDED, "Memory limit exceeded");

    private final int code;
    private final SubmissionState state;
    private final String message;

    public int code() {
        return code;
    }

    public SubmissionState state() {
        return state;
    }

    public Optional<String> message() {
        return Optional.ofNullable(message);
    }

    public static Optional<CodeExecutionExitCode> fromCode(long code) {
        return Arrays.stream(values()).filter(exitCode -> exitCode.code == code).findFirst();
    }

    public static SubmissionState stateFor(long code) {
        return fromCode(code).map(CodeExecutionExitCode::state).orElse(SubmissionState.RUNTIME_ERROR);
    }
}
