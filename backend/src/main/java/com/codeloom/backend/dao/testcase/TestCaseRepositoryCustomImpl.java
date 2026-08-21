package com.codeloom.backend.dao.testcase;

import static com.codeloom.backend.jooq.Tables.TEST_CASES;

import com.codeloom.backend.model.TestCase;
import java.util.*;
import org.jooq.*;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseRepositoryCustomImpl implements TestCaseRepositoryCustom {
  private final DSLContext dsl;

  public TestCaseRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  private List<Condition> conditions(long id, Boolean p) {
    List<Condition> c = new ArrayList<>();
    c.add(TEST_CASES.PROBLEM_ID.eq((int) id));
    if (p != null) c.add(TEST_CASES.IS_PUBLIC.eq(p));
    return c;
  }

  public Collection<TestCase> findAllByProblemId(long id, Boolean p) {
    return dsl.select(TEST_CASES.asterisk())
        .from(TEST_CASES)
        .where(conditions(id, p))
        .fetchInto(TestCase.class);
  }

  public int countAllByProblemId(long id, Boolean p) {
    Integer n =
        dsl.selectCount().from(TEST_CASES).where(conditions(id, p)).fetchOne(0, Integer.class);
    return n == null ? 0 : n;
  }
}
