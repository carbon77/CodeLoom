package com.codeloom.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTopicRequest(
    @NotBlank(message = "Can't create topic without name") String name) {}
