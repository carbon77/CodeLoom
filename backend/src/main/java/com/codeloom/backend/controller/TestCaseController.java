package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/testCases")
@RequiredArgsConstructor
public class TestCaseController {
    private final TestCaseService service;

    @GetMapping("/{id}")
    public TestCase findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/by-ids")
    public Iterable<TestCase> findAllByIds(@RequestParam List<UUID> ids) {
        return service.findAllByIds(ids);
    }

    @GetMapping("/by-problem-id/{problemId}")
    public Iterable<TestCase> findByProblemId(
            @PathVariable long problemId,
            @RequestParam(required = false) Boolean isPublic) {
        return service.findAllByProblemId(problemId, isPublic);
    }

    @PostMapping
    public TestCase create(@Valid @RequestBody CreateTestCaseRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public TestCase patch(@PathVariable UUID id, @RequestBody JsonNode patchJsonNode) {
        return service.patch(id, patchJsonNode);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
