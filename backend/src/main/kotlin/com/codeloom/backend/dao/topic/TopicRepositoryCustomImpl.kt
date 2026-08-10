package com.codeloom.backend.dao.topic

import com.codeloom.backend.jooq.tables.references.PROBLEM_TOPICS
import com.codeloom.backend.jooq.tables.references.TOPICS
import com.codeloom.backend.model.ProblemTopicRelationship
import com.codeloom.backend.model.Topic
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class TopicRepositoryCustomImpl(
    private val dsl: DSLContext,
) : TopicRepositoryCustom {
    override fun findByProblemId(problemId: Long): Iterable<Topic> {
        val topics =
            dsl.select(TOPICS.asterisk())
                .from(TOPICS)
                .innerJoin(PROBLEM_TOPICS)
                .on(PROBLEM_TOPICS.PROBLEM_ID.eq(problemId.toInt()))
                .and(PROBLEM_TOPICS.TOPIC_ID.eq(TOPICS.TOPIC_ID))
                .fetchInto(Topic::class.java)
        return topics
    }

    override fun saveAllProblemRelationships(problemTopicRelationships: Collection<ProblemTopicRelationship>) {
        val inserts =
            problemTopicRelationships.map {
                dsl.insertInto(PROBLEM_TOPICS)
                    .set(PROBLEM_TOPICS.TOPIC_ID, it.topicId)
                    .set(PROBLEM_TOPICS.PROBLEM_ID, it.problemId.toInt())
            }
        dsl.batch(inserts).execute()
    }

    override fun deleteRelationshipsWithProblem(problemId: Long) {
        dsl.deleteFrom(PROBLEM_TOPICS)
            .where(PROBLEM_TOPICS.PROBLEM_ID.eq(problemId.toInt()))
            .execute()
    }
}
