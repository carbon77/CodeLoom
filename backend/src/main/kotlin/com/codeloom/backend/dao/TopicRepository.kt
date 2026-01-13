package com.codeloom.backend.dao;

import com.codeloom.backend.model.Topic
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TopicRepository : CrudRepository<Topic, UUID> {

    @Query(
        "SELECT * FROM topics t " +
                "INNER JOIN problem_topics pt ON pt.problem_id = :problemId AND pt.topic_id = t.topic_id"
    )
    fun findByProblemId(problemId: Long): Iterable<Topic>
}