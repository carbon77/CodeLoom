package com.codeloom.backend.dao.topic;

import static com.codeloom.backend.jooq.Tables.*;

import com.codeloom.backend.model.*;
import java.util.*;
import org.jooq.*;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {
  private final DSLContext dsl;

  public TopicRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  public Iterable<Topic> findByProblemId(long id) {
    return dsl.select(TOPICS.asterisk())
        .from(TOPICS)
        .innerJoin(PROBLEM_TOPICS)
        .on(PROBLEM_TOPICS.PROBLEM_ID.eq((int) id))
        .and(PROBLEM_TOPICS.TOPIC_ID.eq(TOPICS.TOPIC_ID))
        .fetchInto(Topic.class);
  }

  public void saveAllProblemRelationships(Collection<ProblemTopicRelationship> rs) {
    List<Query> q =
        rs.stream()
            .map(
                r ->
                    dsl.insertInto(PROBLEM_TOPICS)
                        .set(PROBLEM_TOPICS.TOPIC_ID, r.topicId())
                        .set(PROBLEM_TOPICS.PROBLEM_ID, (int) r.problemId()))
            .map(Query.class::cast)
            .toList();
    dsl.batch(q).execute();
  }

  public void deleteRelationshipsWithProblem(long id) {
    dsl.deleteFrom(PROBLEM_TOPICS).where(PROBLEM_TOPICS.PROBLEM_ID.eq((int) id)).execute();
  }
}
