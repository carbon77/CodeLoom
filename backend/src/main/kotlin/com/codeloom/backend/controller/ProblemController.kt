package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.services.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/v1/problems")
@Tag(name = "Problems API")
class ProblemController(
    private val problemService: ProblemService,
) {

    @Operation(summary = "Get all problems")
    @GetMapping
    fun findAll(
        @RequestParam("difficulties", required = false) difficulties: List<ProblemDifficulty>?,
    ): Iterable<ProblemListItem> {
        print(difficulties)
        return problemService.findByFilters(difficulties)
    }

    @Operation(summary = "Get problem by id")
    @GetMapping("{problemId}")
    fun findById(@PathVariable("problemId") problemId: UUID): Problem {
        val problem = problemService.findById(problemId)
        if (problem == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id: $problemId not found")
        }
        return problem
    }

    @Operation(summary = "Delete problem by id")
    @DeleteMapping("{problemId}")
    fun delete(@PathVariable("problemId") problemId: UUID) {
        problemService.deleteById(problemId)
    }

    @Operation(summary = "Create new problem")
    @PostMapping
    fun create(@Valid @RequestBody request: CreateProblemRequest): Problem {
        return problemService.create(request)
    }

    @Operation(summary = "Update problem")
    @PutMapping
    fun update(@RequestBody updated: Problem): Problem {
        return problemService.update(updated)
    }
}