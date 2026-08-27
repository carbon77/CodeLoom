package com.codeloom.backend.dto;

import com.codeloom.backend.model.ProblemConstraints;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.model.Topic;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemDto(
        Long id,
        String slug,
        String title,
        String description,
        ProblemDifficulty difficulty,
        ProblemConstraints constraints,
        List<TestCaseDto> examples,
        List<String> hints,
        Iterable<Topic> topics) {}
