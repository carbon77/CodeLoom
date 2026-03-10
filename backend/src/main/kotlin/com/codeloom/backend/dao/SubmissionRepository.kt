package com.codeloom.backend.dao

import com.codeloom.backend.model.Submission
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SubmissionRepository : CrudRepository<Submission, UUID> {
    fun findByUserIdAndProblemId(userId: UUID, problemId: Long): Collection<Submission>
}