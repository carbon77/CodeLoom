package com.codeloom.executor.dto;

import lombok.Builder;

@Builder
public record CodeExecutionRequest(
        String code, String language, String input, Long executionTimeLimitMs, Long memoryUsageLimitBytes) {}
