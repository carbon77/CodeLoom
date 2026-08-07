package com.codeloom.backend.dao.testcase

import com.codeloom.backend.model.TestCase
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TestCaseRepository : CrudRepository<TestCase, UUID>, TestCaseRepositoryCustom {
}