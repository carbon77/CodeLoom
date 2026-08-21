package com.codeloom.backend.dto;

import com.codeloom.backend.model.ProblemDifficulty;
import java.time.Instant;

public record ProblemListDto(
    long problemId, String title, String slug, ProblemDifficulty difficulty, Instant publishedAt) {}
