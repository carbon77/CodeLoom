package com.codeloom.backend.controller

import com.codeloom.backend.model.TestCase
import com.codeloom.backend.service.TestCaseService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode
import java.io.IOException
import java.util.List
import java.util.UUID

@RestController
@RequestMapping("/v1/testCases")
class TestCaseController(
    private val testCaseService: TestCaseService,
) {
    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: UUID,
    ): TestCase = testCaseService.getOne(id)

    @GetMapping("/by-ids")
    fun getMany(
        @RequestParam ids: List<UUID>,
    ): Iterable<TestCase> = testCaseService.getMany(ids)

    @GetMapping("/by-problem-id/{problemId}")
    fun getByProblemId(
        @PathVariable problemId: Long,
        @RequestParam(required = false) publicOnly: Boolean = true,
    ): Iterable<TestCase> = testCaseService.getByProblemId(problemId, publicOnly)

    @PostMapping
    fun create(
        @RequestBody testCase: TestCase,
    ): TestCase = testCaseService.create(testCase)

    @PatchMapping("/{id}")
    @Throws(IOException::class)
    fun patch(
        @PathVariable id: UUID,
        @RequestBody patchNode: JsonNode,
    ): TestCase = testCaseService.patch(id, patchNode)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ) = testCaseService.delete(id)
}
