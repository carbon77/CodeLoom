package com.codeloom.backend.controller

import com.codeloom.backend.config.BAD_REQUEST_RESPONSE_REF
import com.codeloom.backend.config.FORBIDDEN_RESPONSE_REF
import com.codeloom.backend.config.NOT_FOUND_RESPONSE_REF
import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.service.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode

@RestController
@RequestMapping("/v1/problems")
@Tag(name = "Problems API")
class ProblemController(
    private val problemService: ProblemService,
) {
    @Operation(summary = "Get all problem items")
    @ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF)
    @GetMapping("items")
    fun findAllItems(
        auth: Authentication,
        @RequestParam(required = false) difficulties: Set<ProblemDifficulty>?,
        @RequestParam(required = false) topics: Set<String>?,
        @RequestParam(defaultValue = "true") publishedOnly: Boolean,
    ): List<ProblemListDto> {
        return problemService.findItemsByFilters(
            auth,
            ProblemFilters(
                difficulties = difficulties,
                topics = topics,
                publishedOnly = publishedOnly,
            ),
        )
    }

    @Operation(summary = "Get problem by id")
    @ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF)
    @GetMapping("{problemId}")
    fun findById(
        auth: Authentication,
        @PathVariable("problemId") problemId: Long,
    ): Problem {
        return problemService.findById(auth, problemId)
    }

    @Operation(summary = "Get problem dto by slug")
    @ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF)
    @GetMapping("slug/{problemSlug}")
    fun findDtoBySlug(
        auth: Authentication,
        @PathVariable("problemSlug") problemSlug: String,
    ): ProblemDto = problemService.findDtoBySlug(auth, problemSlug)

    @Operation(summary = "Delete problem by id")
    @DeleteMapping("{problemId}")
    fun delete(
        @PathVariable("problemId") problemId: Long,
    ) {
        problemService.deleteById(problemId)
    }

    @Operation(summary = "Create new problem")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF)
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateProblemRequest,
    ): Problem {
        return problemService.create(request)
    }

    @Operation(summary = "Update problem")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF)
    @PutMapping("/{problemId}")
    fun update(
        @PathVariable problemId: Long,
        @RequestBody updated: JsonNode,
    ): Problem {
        return problemService.update(problemId, updated)
    }

    @Operation(summary = "Publish problem")
    @ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF)
    @PatchMapping("/{problemId}/publish")
    fun publish(
        @PathVariable("problemId") problemId: Long,
    ) = problemService.publish(problemId)

    @Operation(summary = "Unpublish problem")
    @ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF)
    @PatchMapping("/{problemId}/unpublish")
    fun unpublish(
        @PathVariable("problemId") problemId: Long,
    ) = problemService.unpublish(problemId)
}
