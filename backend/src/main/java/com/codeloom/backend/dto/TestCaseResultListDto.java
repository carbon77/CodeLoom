package com.codeloom.backend.dto;

import lombok.Builder;

@Builder
public record TestCaseResultListDto(
        String input,
        String expectedOutput,
        String stdout,
        String stderr,
        Long executionTimeMs,
        Long bytesUsed
) {
}
