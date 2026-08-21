package com.codeloom.common.event;

import java.util.UUID;

public record TestCaseResult(
        UUID id,
        long problemId,
        String input,
        String expectedOutput,
        String stdout,
        String stderr,
        long executionTimeMs,
        long memoryUsageBytes) {}
