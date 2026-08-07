package com.codeloom.backend.service

import com.codeloom.backend.dao.testcase.TestCaseRepository
import com.codeloom.backend.dto.CreateTestCaseRequest
import com.codeloom.backend.model.TestCase
import com.codeloom.backend.patchValue
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.util.*

@Service
class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val objectMapper: ObjectMapper,
) {
    fun findById(id: UUID): TestCase = findOrThrow(id)

    fun findAllByIds(ids: List<UUID>): Iterable<TestCase> = testCaseRepository.findAllById(ids)

    @Transactional
    fun create(request: CreateTestCaseRequest): TestCase {
        val testCase = TestCase(
            problemId = request.problemId,
            input = request.input,
            expectedOutput = request.expectedOutput,
            isPublic = request.isPublic,
        )
        return testCaseRepository.save(testCase)
    }

    @Transactional
    fun patch(id: UUID, patchNode: JsonNode): TestCase {
        val testCase = findById(id)
        val updated = testCase.copy(
            input = patchNode.patchValue("input", objectMapper, testCase.input),
            expectedOutput = patchNode.patchValue("expectedOutput", objectMapper, testCase.expectedOutput),
            isPublic = patchNode.patchValue("isPublic", objectMapper, testCase.isPublic),
        )
        return testCaseRepository.save(updated)
    }

    @Transactional
    fun delete(id: UUID) {
        testCaseRepository.deleteById(id)
    }

    fun findAllByProblemId(problemId: Long, isPublic: Boolean?): Iterable<TestCase> {
        return testCaseRepository.findAllByProblemId(problemId, isPublic)
    }

    private fun findOrThrow(id: UUID): TestCase {
        return testCaseRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
            }
    }
}