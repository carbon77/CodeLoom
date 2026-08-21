package com.codeloom.executor.dto;

public record CodeExecutionRequest(
        String code, String language, String input, Long executionTimeLimitMs, Long memoryUsageLimitBytes) {}
