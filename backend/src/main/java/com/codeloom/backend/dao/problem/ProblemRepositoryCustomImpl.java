package com.codeloom.backend.dao.problem;

import static com.codeloom.backend.jooq.Tables.*;

import com.codeloom.backend.dto.*;
import java.util.*;
import org.jooq.*;
import org.springframework.stereotype.Repository;

@Repository
public class ProblemRepositoryCustomImpl implements ProblemRepositoryCustom {
    private final DSLContext dsl;

    public ProblemRepositoryCustomImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ProblemListDto> findProblemListDtos(ProblemFilters f) {
        var stmt = dsl.select(
                        PROBLEMS.PROBLEM_ID, PROBLEMS.SLUG, PROBLEMS.TITLE, PROBLEMS.DIFFICULTY, PROBLEMS.PUBLISHED_AT)
                .from(PROBLEMS);
        List<Condition> c = new ArrayList<>();
        if (f.difficulties() != null && !f.difficulties().isEmpty()) c.add(PROBLEMS.DIFFICULTY.in(f.difficulties()));
        if (f.topics() != null && !f.topics().isEmpty()) {
            stmt = stmt.innerJoin(PROBLEM_TOPICS)
                    .on(PROBLEM_TOPICS.PROBLEM_ID.eq(PROBLEMS.PROBLEM_ID))
                    .innerJoin(TOPICS)
                    .on(TOPICS.TOPIC_ID.eq(PROBLEM_TOPICS.TOPIC_ID));
            c.add(TOPICS.NAME.in(f.topics()));
        }
        if (f.publishedOnly()) c.add(PROBLEMS.PUBLISHED_AT.isNotNull());
        return stmt.where(c).fetchInto(ProblemListDto.class);
    }
}
