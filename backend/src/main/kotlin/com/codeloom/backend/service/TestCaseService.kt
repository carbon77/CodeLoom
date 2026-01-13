package com.codeloom.backend.service;

import com.codeloom.backend.dao.TestCaseRepository
import com.codeloom.backend.model.TestCase
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.util.*

@Service
class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getOne(id: UUID): TestCase {
        val testCaseOptional: Optional<TestCase> = testCaseRepository.findById(id)
        return testCaseOptional.orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
    }

    fun getMany(ids: List<UUID>): Iterable<TestCase> = testCaseRepository.findAllById(ids)
    fun create(testCase: TestCase): TestCase = testCaseRepository.save(testCase)

    @Throws(IOException::class)
    fun patch(id: UUID, patchNode: JsonNode): TestCase {
        val testCase: TestCase = testCaseRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
        objectMapper.readerForUpdating(testCase).readValue<TestCase>(patchNode)
        return testCaseRepository.save(testCase)
    }

    fun delete(id: UUID) {
        val testCase: TestCase? = testCaseRepository.findById(id).orElse(null)
        if (testCase != null) {
            testCaseRepository.delete(testCase)
        }
    }

    fun getByProblemId(problemId: Long, publicOnly: Boolean): Iterable<TestCase> {
        if (publicOnly) {
            return testCaseRepository.findByProblemIdAndIsPublic(problemId, true)
        }
        return testCaseRepository.findByProblemId(problemId)
    }
}