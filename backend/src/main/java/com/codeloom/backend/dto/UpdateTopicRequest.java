package com.codeloom.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTopicRequest(
        @NotBlank(message = "Topic name must not be blank") String name) {}
