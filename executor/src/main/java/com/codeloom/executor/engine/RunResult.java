package com.codeloom.executor.engine;

public record RunResult(
    long exitCode, String stdout, String stderr, long executionTimeMs, long memoryUsageBytes) {}
