package com.codeloom.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionState {
    PENDING(false),
    COMPILING(false),
    COMPILE_ERROR(true),
    RUNNING(false),
    ACCEPTED(true),
    WRONG_ANSWER(true),
    RUNTIME_ERROR(true),
    TIME_LIMIT_EXCEEDED(true),
    MEMORY_LIMIT_EXCEEDED(true),
    SYSTEM_ERROR(true);

    private final boolean isTerminal;
}
