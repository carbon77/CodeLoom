package com.codeloom.executor.engine;

public record ContainerOutcome(
    long exitCode, String stdout, String stderr, long memoryUsageBytes, long executionTimeMs) {}
