package com.codeloom.backend.dto;

import com.codeloom.backend.model.TestCase;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TestCaseDto(UUID id, String input, String expectedOutput, boolean isPublic, String explanation) {

    public static TestCaseDto fromEntity(TestCase testCase) {
        return TestCaseDto.builder()
                .id(testCase.getId())
                .expectedOutput(testCase.getExpectedOutput())
                .input(testCase.getInput())
                .explanation(testCase.getExplanation())
                .isPublic(testCase.getIsPublic())
                .build();
    }
}
