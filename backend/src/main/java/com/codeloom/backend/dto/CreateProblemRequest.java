package com.codeloom.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateProblemRequest(
        @NotNull(message = "Title is mandatory") String title) {}
