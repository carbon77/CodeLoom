package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.service.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode

@RestController
@RequestMapping("/v1/problems")
@Tag(name = "Problems API")
class ProblemController(
    private val problemService: ProblemService,
) {

    @Operation(summary = "Get all problem items")
    @GetMapping("items")
    fun findAllItems(
        @RequestParam(required = false) difficulties: Set<ProblemDifficulty>?,
        @RequestParam(defaultValue = "true") publishedOnly: Boolean,
    ): List<ProblemListDto> {
        return problemService.findItemsByFilters(
            filters = ProblemFilters(
                difficulties = difficulties,
                publishedOnly = publishedOnly,
            )
        )
    }

    @Operation(summary = "Get problem by id")
    @GetMapping("{problemId}")
    fun findById(@PathVariable("problemId") problemId: Long): Problem {
        return problemService.findById(problemId)
    }

    @Operation(summary = "Get problem dto by slug")
    @GetMapping("slug/{problemSlug}")
    fun findDtoBySlug(@PathVariable("problemSlug") problemSlug: String): ProblemDto =
        problemService.findDtoBySlug(problemSlug)

    @Operation(summary = "Delete problem by id")
    @DeleteMapping("{problemId}")
    fun delete(@PathVariable("problemId") problemId: Long) {
        problemService.deleteById(problemId)
    }

    @Operation(summary = "Create new problem")
    @PostMapping
    fun create(@Valid @RequestBody request: CreateProblemRequest): Problem {
        try {
            return problemService.create(request)
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists")
        }
    }

    @Operation(summary = "Update problem")
    @PutMapping("/{problemId}")
    fun update(@PathVariable problemId: Long, @RequestBody updated: JsonNode): Problem {
        return problemService.update(problemId, updated)
    }

    @Operation(summary = "Publish problem")
    @PatchMapping("/{problemId}/publish")
    fun publish(@PathVariable("problemId") problemId: Long) =
        problemService.publish(problemId)

    @Operation(summary = "Unpublish problem")
    @PatchMapping("/{problemId}/unpublish")
    fun unpublish(@PathVariable("problemId") problemId: Long) =
        problemService.unpublish(problemId)
}