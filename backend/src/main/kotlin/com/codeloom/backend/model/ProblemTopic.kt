package com.codeloom.backend.model

import java.util.*

data class ProblemTopic(
    val topicId: UUID,
    val problemId: Long,
)

infix fun Topic.to(problem: Problem) = ProblemTopic(this.id!!, problem.id!!)