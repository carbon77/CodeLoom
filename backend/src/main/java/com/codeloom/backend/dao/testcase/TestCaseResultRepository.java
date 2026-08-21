package com.codeloom.backend.dao.testcase;

import com.codeloom.backend.model.TestCaseResult;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface TestCaseResultRepository extends CrudRepository<TestCaseResult, UUID> {
  void deleteBySubmissionId(UUID id);
}
