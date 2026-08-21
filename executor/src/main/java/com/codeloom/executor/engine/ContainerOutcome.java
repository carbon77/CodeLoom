package com.codeloom.executor.engine;

import lombok.Builder;

@Builder
public record ContainerOutcome(
        long exitCode, String stdout, String stderr, long memoryUsageBytes, long executionTimeMs) {}
