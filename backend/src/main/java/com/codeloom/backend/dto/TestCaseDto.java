package com.codeloom.backend.dto;

import com.codeloom.backend.model.TestCase;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TestCaseDto(
        UUID id,
        String input,
        String expectedOutput,
        boolean isPublic,
        String explanation
) {

    static public TestCaseDto fromEntity(TestCase testCase) {
        return TestCaseDto.builder()
                .id(testCase.getId())
                .expectedOutput(testCase.getExpectedOutput())
                .input(testCase.getInput())
                .explanation(testCase.getExplanation())
                .isPublic(testCase.getIsPublic())
                .build();
    }
}
