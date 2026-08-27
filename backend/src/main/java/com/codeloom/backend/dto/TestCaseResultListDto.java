package com.codeloom.backend.dto;

import com.codeloom.backend.model.TestCaseResult;
import lombok.Builder;

@Builder
public record TestCaseResultListDto(
        String input, String expectedOutput, String stdout, String stderr, Long executionTimeMs, Long bytesUsed) {
    public static TestCaseResultListDto fromEntity(TestCaseResult result) {
        return TestCaseResultListDto.builder()
                .input(result.getInput())
                .expectedOutput(result.getExpectedOutput())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .executionTimeMs(result.getExecutionTimeMs())
                .bytesUsed(result.getBytesUsed())
                .build();
    }
}
