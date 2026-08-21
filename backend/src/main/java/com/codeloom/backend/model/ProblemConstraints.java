package com.codeloom.backend.model;

import lombok.Builder;

@Builder
public record ProblemConstraints(Long executionTimeLimitMs, Long memoryUsageLimitBytes) {
    public ProblemConstraints() {
        this(null, null);
    }
}
