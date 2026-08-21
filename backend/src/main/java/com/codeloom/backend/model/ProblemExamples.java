package com.codeloom.backend.model;

import java.util.List;
import lombok.Builder;

@Builder
public record ProblemExamples(List<ProblemExample> examples) {
    public ProblemExamples() {
        this(List.of());
    }
}
