package com.codeloom.backend.model;

import lombok.Builder;

@Builder
public record ProblemExample(String input, String output, String explanation) {}
