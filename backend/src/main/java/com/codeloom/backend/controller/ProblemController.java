package com.codeloom.backend.controller;

import static com.codeloom.backend.config.OpenApiConfig.BAD_REQUEST_RESPONSE_REF;
import static com.codeloom.backend.config.OpenApiConfig.NOT_FOUND_RESPONSE_REF;

import com.codeloom.backend.dto.CreateProblemRequest;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.dto.ProblemFilters;
import com.codeloom.backend.dto.ProblemListDto;
import com.codeloom.backend.dto.UpdateProblemRequest;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/problems")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "Coding problem discovery and administration")
public class ProblemController {
    private final ProblemService service;

    @GetMapping("items")
    @Operation(
            operationId = "listProblems",
            summary = "List problems",
            description = "Returns problems matching the supplied filters.")
    @ApiResponse(responseCode = "200", description = "Problems returned")
    public List<ProblemListDto> findAllItems(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Difficulty levels to include") @RequestParam(required = false)
                    Set<ProblemDifficulty> difficulties,
            @Parameter(description = "Topic names to include") @RequestParam(required = false) Set<String> topics,
            @Parameter(description = "Return only published problems") @RequestParam(defaultValue = "true")
                    boolean publishedOnly) {
        var filters = ProblemFilters.builder()
                .difficulties(difficulties)
                .publishedOnly(publishedOnly)
                .topics(topics)
                .build();
        return service.findItemsByFilters(authentication, filters);
    }

    @GetMapping("{problemId}")
    @Operation(operationId = "getProblem", summary = "Get a problem by identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem returned"),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public Problem findById(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Problem identifier") @PathVariable long problemId) {
        return service.findById(authentication, problemId);
    }

    @GetMapping("slug/{problemSlug}")
    @Operation(operationId = "getProblemBySlug", summary = "Get a problem by slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem returned"),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public ProblemDto findDtoBySlug(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Problem slug") @PathVariable String problemSlug) {
        return service.findDtoBySlug(authentication, problemSlug);
    }

    @DeleteMapping("{problemId}")
    @Operation(operationId = "deleteProblem", summary = "Delete a problem", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem deleted"),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public void delete(@Parameter(description = "Problem identifier") @PathVariable long problemId) {
        service.deleteById(problemId);
    }

    @PostMapping
    @Operation(operationId = "createProblem", summary = "Create a problem", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem created"),
        @ApiResponse(responseCode = "400", description = "Invalid or duplicate problem", ref = BAD_REQUEST_RESPONSE_REF)
    })
    public Problem create(@Valid @RequestBody CreateProblemRequest request) {
        return service.create(request);
    }

    @PutMapping("/{problemId}")
    @Operation(operationId = "updateProblem", summary = "Update a problem", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem updated"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid or duplicate problem",
                ref = BAD_REQUEST_RESPONSE_REF),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public Problem update(
            @Parameter(description = "Problem identifier") @PathVariable long problemId,
            @Valid @RequestBody UpdateProblemRequest request) {
        return service.update(problemId, request);
    }

    @PatchMapping("/{problemId}/publish")
    @Operation(operationId = "publishProblem", summary = "Publish a problem", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem published"),
        @ApiResponse(responseCode = "400", description = "Problem cannot be published", ref = BAD_REQUEST_RESPONSE_REF),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public void publish(@Parameter(description = "Problem identifier") @PathVariable long problemId) {
        service.publish(problemId);
    }

    @PatchMapping("/{problemId}/unpublish")
    @Operation(
            operationId = "unpublishProblem",
            summary = "Unpublish a problem",
            description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Problem unpublished"),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public void unpublish(@Parameter(description = "Problem identifier") @PathVariable long problemId) {
        service.unpublish(problemId);
    }
}
