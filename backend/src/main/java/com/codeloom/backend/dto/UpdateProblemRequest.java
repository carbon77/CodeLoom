package com.codeloom.backend.dto;

import com.codeloom.backend.model.ProblemConstraints;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.model.ProblemExamples;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateProblemRequest(
        @NotBlank(message = "Title must not be blank") String title,
        @NotBlank(message = "Slug must not be blank") String slug,
        @NotNull(message = "Description must not be null") String description,
        @NotNull(message = "Difficulty must not be null") ProblemDifficulty difficulty,
        @NotNull(message = "Constraints must not be null") ProblemConstraints constraints,
        @NotNull(message = "Examples must not be null") ProblemExamples examples,
        @NotNull(message = "Hints must not be null") List<String> hints,
        @Valid @NotNull(message = "Topics must not be null") List<UpdateProblemTopicRequest> topics) {}
