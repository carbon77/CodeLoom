package com.codeloom.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record CreateTestCaseRequest(
        @NotNull(message = "problemId must not be null") Long problemId,
        @NotBlank(message = "input can't be blank") String input,
        @NotBlank(message = "expectedOutput can't be blank") String expectedOutput,
        @NotNull(message = "isPublic must not be null") Boolean isPublic) {}
