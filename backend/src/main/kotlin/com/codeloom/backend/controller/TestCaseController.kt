package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateTestCaseRequest
import com.codeloom.backend.model.TestCase
import com.codeloom.backend.service.TestCaseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode
import java.util.*

@RestController
@RequestMapping("/v1/testCases")
class TestCaseController(private val testCaseService: TestCaseService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): TestCase = testCaseService.findById(id)

    @GetMapping("/by-ids")
    fun findAllByIds(@RequestParam ids: List<UUID>): Iterable<TestCase> = testCaseService.findAllByIds(ids)

    @GetMapping("/by-problem-id/{problemId}")
    fun findByProblemId(
        @PathVariable problemId: Long,
        @RequestParam(required = false) isPublic: Boolean?,
    ): Iterable<TestCase> = testCaseService.findAllByProblemId(problemId, isPublic)

    @PostMapping
    fun create(@Valid @RequestBody request: CreateTestCaseRequest): TestCase = testCaseService.create(request)

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: UUID, @RequestBody patchNode: JsonNode): TestCase = testCaseService.patch(id, patchNode)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = testCaseService.delete(id)
}
