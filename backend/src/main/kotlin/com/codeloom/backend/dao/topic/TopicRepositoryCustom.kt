package com.codeloom.backend.dao.topic

import com.codeloom.backend.model.ProblemTopicRelationship
import com.codeloom.backend.model.Topic

interface TopicRepositoryCustom {
    fun findByProblemId(problemId: Long): Iterable<Topic>
    fun saveAllProblemRelationships(problemTopicRelationships: Collection<ProblemTopicRelationship>)
    fun deleteRelationshipsWithProblem(problemId: Long)
}