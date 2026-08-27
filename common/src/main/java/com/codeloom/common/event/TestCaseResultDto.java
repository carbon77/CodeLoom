package com.codeloom.common.event;

import java.util.UUID;
import lombok.Builder;

@Builder
public record TestCaseResultDto(
        UUID id,
        long problemId,
        String input,
        String expectedOutput,
        String stdout,
        String stderr,
        long executionTimeMs,
        long memoryUsageBytes) {}
