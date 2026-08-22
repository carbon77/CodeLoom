package com.codeloom.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTestCaseRequest(
        @NotNull(message = "problemId must not be null") Long problemId,
        @NotBlank(message = "input can't be blank") String input,
        @NotBlank(message = "expectedOutput can't be blank") String expectedOutput,
        @NotNull(message = "isPublic must not be null") Boolean isPublic) {}
