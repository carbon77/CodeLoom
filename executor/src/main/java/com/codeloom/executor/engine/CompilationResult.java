package com.codeloom.executor.engine;

import lombok.Builder;

@Builder
public record CompilationResult(boolean isSuccessful, String stderr) {}
