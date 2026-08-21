package com.codeloom.backend.dao.testcase;

import com.codeloom.backend.model.TestCase;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface TestCaseRepository extends CrudRepository<TestCase, UUID>, TestCaseRepositoryCustom {}
