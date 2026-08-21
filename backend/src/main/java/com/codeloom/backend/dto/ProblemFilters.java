package com.codeloom.backend.dto;

import com.codeloom.backend.model.ProblemDifficulty;
import java.util.Set;

public record ProblemFilters(
    Set<ProblemDifficulty> difficulties, boolean publishedOnly, Set<String> topics) {}
