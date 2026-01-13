package com.codeloom.backend.dao;

import com.codeloom.backend.model.ProblemTopic
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ProblemTopicRepository(
    private val jdbc: JdbcTemplate,
) {
    fun saveAll(problemTopics: Collection<ProblemTopic>) {
        val sql = "INSERT INTO problem_topics (topic_id, problem_id) VALUES (?, ?)"
        jdbc.batchUpdate(sql, problemTopics, problemTopics.size) { ps, pt ->
            ps.setObject(1, pt.topicId)
            ps.setLong(2, pt.problemId)
        }
    }

    fun deleteByProblemId(problemId: Long) {
        val sql = "DELETE FROM problem_topics WHERE problem_id = ?"
        jdbc.update(sql, problemId)
    }
}