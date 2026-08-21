package com.codeloom.backend.model;

import java.util.UUID;

public record ProblemTopicRelationship(UUID topicId, long problemId) {}
