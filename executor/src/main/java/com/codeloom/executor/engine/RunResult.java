package com.codeloom.executor.engine;

import lombok.Builder;

@Builder
public record RunResult(long exitCode, String stdout, String stderr, long executionTimeMs, long memoryUsageBytes) {}
