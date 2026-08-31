package com.codeloom.executor.dto;

import lombok.Builder;

@Builder
public record CompilationResult(boolean isSuccessful, String stderr) {}
