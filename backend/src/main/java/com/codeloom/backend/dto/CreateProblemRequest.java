package com.codeloom.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateProblemRequest(
        @NotNull(message = "Title is mandatory") String title) {}
