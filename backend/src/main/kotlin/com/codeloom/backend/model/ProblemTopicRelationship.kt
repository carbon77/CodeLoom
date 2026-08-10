package com.codeloom.backend.model

import java.util.*

data class ProblemTopicRelationship(
    val topicId: UUID,
    val problemId: Long,
)

infix fun Topic.to(problem: Problem) = ProblemTopicRelationship(this.id!!, problem.id!!)
