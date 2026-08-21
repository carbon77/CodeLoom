package com.codeloom.executor.dto;

public record CodeExecutionResult(
    String stdout, String stderr, long exitCode, long executionTimeMs, long memoryUsageBytes) {}
