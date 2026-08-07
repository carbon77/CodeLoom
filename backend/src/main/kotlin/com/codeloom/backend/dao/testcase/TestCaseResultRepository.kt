package com.codeloom.backend.dao.testcase

import com.codeloom.backend.model.TestCaseResult
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TestCaseResultRepository : CrudRepository<TestCaseResult, UUID> {
    fun deleteBySubmissionId(submissionId: UUID)
}
