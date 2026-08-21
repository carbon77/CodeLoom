package com.codeloom.backend.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ProblemTopicRelationship(UUID topicId, long problemId) {}
