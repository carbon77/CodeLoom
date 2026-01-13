package com.codeloom.backend.controller

import com.codeloom.backend.model.TestCase
import com.codeloom.backend.service.TestCaseService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*

@RestController
@RequestMapping("/v1/testCases")
class TestCaseController(private val testCaseService: TestCaseService) {

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID): TestCase = testCaseService.getOne(id)

    @GetMapping("/by-ids")
    fun getMany(@RequestParam ids: List<UUID>): Iterable<TestCase> = testCaseService.getMany(ids)

    @GetMapping("/by-problem-id/{problemId}")
    fun getByProblemId(
        @PathVariable problemId: Long,
        @RequestParam(required = false) publicOnly: Boolean = true,
    ): Iterable<TestCase> = testCaseService.getByProblemId(problemId, publicOnly)

    @PostMapping
    fun create(@RequestBody testCase: TestCase): TestCase = testCaseService.create(testCase)

    @PatchMapping("/{id}")
    @Throws(IOException::class)
    fun patch(@PathVariable id: UUID, @RequestBody patchNode: JsonNode): TestCase = testCaseService.patch(id, patchNode)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = testCaseService.delete(id)
}
