package com.codeloom.executor.dto;

import lombok.Builder;

@Builder
public record CodeExecutionResult(
        String stdout, String stderr, long exitCode, long executionTimeMs, long memoryUsageBytes) {}
