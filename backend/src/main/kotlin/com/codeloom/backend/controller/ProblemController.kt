package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.services.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/v1/problems")
@Tag(name = "Problems API")
@CrossOrigin(origins = ["*"])
class ProblemController(
    private val problemService: ProblemService,
) {

    @Operation(summary = "Get all problem items")
    @GetMapping("items")
    fun findAllItems(
        @RequestParam(required = false) difficulties: Set<ProblemDifficulty>?,
        @RequestParam(defaultValue = "true") publishedOnly: Boolean,
    ): List<ProblemListItem> {
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

    @Operation(summary = "Get problem by slug")
    @GetMapping("slug/{problemSlug}")
    fun findBySlug(@PathVariable("problemSlug") problemSlug: String): Problem {
        return problemService.findBySlug(problemSlug)
    }

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
        } catch (e: DbActionExecutionException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists")
        }
    }

    @Operation(summary = "Update problem")
    @PutMapping("/{problemId}")
    fun update(@PathVariable problemId: Long, @RequestBody updated: Problem): Problem {
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