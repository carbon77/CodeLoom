package com.codeloom.backend.controller;

import static com.codeloom.backend.config.OpenApiConfig.BAD_REQUEST_RESPONSE_REF;
import static com.codeloom.backend.config.OpenApiConfig.NOT_FOUND_RESPONSE_REF;

import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.dto.UpdateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/testCases")
@RequiredArgsConstructor
@Tag(name = "Test cases", description = "Problem test case administration")
public class TestCaseController {
    private final TestCaseService service;

    @GetMapping("/{id}")
    @Operation(operationId = "getTestCase", summary = "Get a test case", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test case returned"),
        @ApiResponse(responseCode = "404", description = "Test case not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public TestCase findById(@Parameter(description = "Test case identifier") @PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/by-ids")
    @Operation(
            operationId = "listTestCasesByIds",
            summary = "List test cases by identifiers",
            description = "Requires the ADMIN role.")
    @ApiResponse(
            responseCode = "200",
            description = "Test cases returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TestCase.class))))
    public Iterable<TestCase> findAllByIds(
            @Parameter(description = "Test case identifiers") @RequestParam List<UUID> ids) {
        return service.findAllByIds(ids);
    }

    @GetMapping("/by-problem-id/{problemId}")
    @Operation(
            operationId = "listTestCasesByProblem",
            summary = "List test cases for a problem",
            description = "Requires the ADMIN role.")
    @ApiResponse(
            responseCode = "200",
            description = "Test cases returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TestCase.class))))
    public Iterable<TestCase> findByProblemId(
            @Parameter(description = "Problem identifier") @PathVariable long problemId,
            @Parameter(description = "Optional public/private visibility filter") @RequestParam(required = false)
                    Boolean isPublic) {
        return service.findAllByProblemId(problemId, isPublic);
    }

    @PostMapping
    @Operation(operationId = "createTestCase", summary = "Create a test case", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test case created"),
        @ApiResponse(responseCode = "400", description = "Invalid test case", ref = BAD_REQUEST_RESPONSE_REF)
    })
    public TestCase create(@Valid @RequestBody CreateTestCaseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateTestCase", summary = "Update a test case", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test case updated"),
        @ApiResponse(responseCode = "400", description = "Invalid test case", ref = BAD_REQUEST_RESPONSE_REF),
        @ApiResponse(responseCode = "404", description = "Test case not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public TestCase update(
            @Parameter(description = "Test case identifier") @PathVariable UUID id,
            @Valid @RequestBody UpdateTestCaseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteTestCase", summary = "Delete a test case", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test case deleted"),
        @ApiResponse(responseCode = "404", description = "Test case not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public void delete(@Parameter(description = "Test case identifier") @PathVariable UUID id) {
        service.delete(id);
    }
}
