package com.codeloom.backend.model;

import java.util.List;

public record ProblemExamples(List<ProblemExample> examples) {
  public ProblemExamples() {
    this(List.of());
  }
}
