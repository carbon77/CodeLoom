package com.codeloom.executor.repository;

import com.codeloom.executor.model.TestCase;
import java.util.*;
import org.springframework.data.repository.CrudRepository;

public interface TestCaseRepository extends CrudRepository<TestCase, UUID> {
  List<TestCase> findByProblemId(long id);
}
