package com.codeloom.backend.dao.testcase;

import static com.codeloom.backend.jooq.Tables.TEST_CASES;

import com.codeloom.backend.model.TestCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TestCaseRepositoryCustomImpl implements TestCaseRepositoryCustom {
    private final DSLContext dsl;

    private List<Condition> conditions(long problemId, Boolean publishedOnly) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(TEST_CASES.PROBLEM_ID.eq((int) problemId));

        if (publishedOnly != null) {
            conditions.add(TEST_CASES.IS_PUBLIC.eq(publishedOnly));
        }

        return conditions;
    }

    public Collection<TestCase> findAllByProblemId(long problemId, Boolean publishedOnly) {
        return dsl.select(TEST_CASES.asterisk())
                .from(TEST_CASES)
                .where(conditions(problemId, publishedOnly))
                .fetchInto(TestCase.class);
    }

    public int countAllByProblemId(long problemId, Boolean publishedOnly) {
        Integer n = dsl.selectCount()
                .from(TEST_CASES)
                .where(conditions(problemId, publishedOnly))
                .fetchOne(0, Integer.class);
        return n == null ? 0 : n;
    }
}
