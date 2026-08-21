package com.codeloom.backend.dto;

import com.codeloom.backend.model.ProblemDifficulty;
import java.util.Set;
import lombok.Builder;

@Builder
public record ProblemFilters(Set<ProblemDifficulty> difficulties, boolean publishedOnly, Set<String> topics) {}
