package com.codeloom.backend.model;

public record ProblemConstraints(Long executionTimeLimitMs, Long memoryUsageLimitBytes) {
  public ProblemConstraints() {
    this(null, null);
  }
}
