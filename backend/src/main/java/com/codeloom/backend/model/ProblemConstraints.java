package com.codeloom.backend.model;

import lombok.Builder;

@Builder
public record ProblemConstraints(Long executionTimeLimitMs, Long memoryUsageLimitMb) {
    public ProblemConstraints() {
        this(null, null);
    }
}
