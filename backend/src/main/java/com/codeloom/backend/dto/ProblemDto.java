package com.codeloom.backend.dto;

import com.codeloom.backend.model.*;

public record ProblemDto(
        Long id,
        String slug,
        String title,
        String description,
        ProblemDifficulty difficulty,
        ProblemConstraints constraints,
        ProblemExamples examples,
        String[] hints,
        Iterable<TestCase> testCases,
        Iterable<Topic> topics) {}
