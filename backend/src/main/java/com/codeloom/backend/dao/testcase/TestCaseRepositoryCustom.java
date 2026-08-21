package com.codeloom.backend.dao.testcase;

import com.codeloom.backend.model.TestCase;
import java.util.Collection;

public interface TestCaseRepositoryCustom {
  Collection<TestCase> findAllByProblemId(long id, Boolean isPublic);

  int countAllByProblemId(long id, Boolean isPublic);

  default int countAllByProblemId(long id) {
    return countAllByProblemId(id, null);
  }
}
