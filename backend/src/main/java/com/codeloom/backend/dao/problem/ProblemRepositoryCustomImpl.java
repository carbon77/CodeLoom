package com.codeloom.backend.dao.problem;

import static com.codeloom.backend.jooq.Tables.*;

import com.codeloom.backend.dto.*;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProblemRepositoryCustomImpl implements ProblemRepositoryCustom {
    private final DSLContext dsl;

    public List<ProblemListDto> findProblemListDtos(ProblemFilters filters) {
        var stmt = dsl.select(
                        PROBLEMS.PROBLEM_ID,
                        PROBLEMS.SLUG,
                        PROBLEMS.TITLE,
                        PROBLEMS.DIFFICULTY,
                        PROBLEMS.PUBLISHED_AT)
                .from(PROBLEMS);
        List<Condition> conditions = new ArrayList<>();

        if (filters.difficulties() != null && !filters.difficulties().isEmpty()) {
            conditions.add(PROBLEMS.DIFFICULTY.in(filters.difficulties()));
        }

        if (filters.topics() != null && !filters.topics().isEmpty()) {
            stmt = stmt.innerJoin(PROBLEM_TOPICS)
                    .on(PROBLEM_TOPICS.PROBLEM_ID.eq(PROBLEMS.PROBLEM_ID))
                    .innerJoin(TOPICS)
                    .on(TOPICS.TOPIC_ID.eq(PROBLEM_TOPICS.TOPIC_ID));
            conditions.add(TOPICS.NAME.in(filters.topics()));
        }

        if (filters.publishedOnly()) {
            conditions.add(PROBLEMS.PUBLISHED_AT.isNotNull());
        }
        return stmt.where(conditions).fetchInto(ProblemListDto.class);
    }
}
