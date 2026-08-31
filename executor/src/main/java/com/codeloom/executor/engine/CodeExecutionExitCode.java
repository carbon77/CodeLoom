package com.codeloom.executor.engine;

import com.codeloom.common.SubmissionStatus;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CodeExecutionExitCode {
    ERROR(1, SubmissionStatus.RUNTIME_ERROR, null),
    TIMEOUT(124, SubmissionStatus.TIME_LIMIT_EXCEEDED, "Execution timed out"),
    OUTPUT_LIMIT(125, SubmissionStatus.RUNTIME_ERROR, null),
    MEMORY_LIMIT_EXCEEDED(137, SubmissionStatus.MEMORY_LIMIT_EXCEEDED, "Memory limit exceeded");

    private final int code;
    private final SubmissionStatus status;
    private final String message;

    public int code() {
        return code;
    }

    public SubmissionStatus status() {
        return status;
    }

    public Optional<String> message() {
        return Optional.ofNullable(message);
    }

    public static Optional<CodeExecutionExitCode> fromCode(long code) {
        return Arrays.stream(values()).filter(exitCode -> exitCode.code == code).findFirst();
    }

    public static SubmissionStatus statusFor(long code) {
        return fromCode(code).map(CodeExecutionExitCode::status).orElse(SubmissionStatus.RUNTIME_ERROR);
    }
}
