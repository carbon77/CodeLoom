package com.codeloom.backend.dao.topic;

import static com.codeloom.backend.jooq.Tables.PROBLEM_TOPICS;
import static com.codeloom.backend.jooq.Tables.TOPICS;

import com.codeloom.backend.model.ProblemTopicRelationship;
import com.codeloom.backend.model.Topic;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {
    private final DSLContext dsl;

    public Iterable<Topic> findByProblemId(long id) {
        return dsl.select(TOPICS.asterisk())
                .from(TOPICS)
                .innerJoin(PROBLEM_TOPICS)
                .on(PROBLEM_TOPICS.PROBLEM_ID.eq((int) id))
                .and(PROBLEM_TOPICS.TOPIC_ID.eq(TOPICS.TOPIC_ID))
                .fetchInto(Topic.class);
    }

    public void saveAllProblemRelationships(Collection<ProblemTopicRelationship> relationships) {
        List<Query> queries = relationships.stream()
                .map(relationship -> dsl.insertInto(PROBLEM_TOPICS)
                        .set(PROBLEM_TOPICS.TOPIC_ID, relationship.topicId())
                        .set(PROBLEM_TOPICS.PROBLEM_ID, (int) relationship.problemId()))
                .map(Query.class::cast)
                .toList();
        dsl.batch(queries).execute();
    }

    public void deleteRelationshipsWithProblem(long id) {
        dsl.deleteFrom(PROBLEM_TOPICS)
                .where(PROBLEM_TOPICS.PROBLEM_ID.eq((int) id))
                .execute();
    }
}
