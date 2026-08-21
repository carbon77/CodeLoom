package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.service.TestCaseService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/v1/testCases")
public class TestCaseController {
    private final TestCaseService service;

    public TestCaseController(TestCaseService s) {
        service = s;
    }

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
            @PathVariable long problemId, @RequestParam(required = false) Boolean isPublic) {
        return service.findAllByProblemId(problemId, isPublic);
    }

    @PostMapping
    public TestCase create(@Valid @RequestBody CreateTestCaseRequest q) {
        return service.create(q);
    }

    @PatchMapping("/{id}")
    public TestCase patch(@PathVariable UUID id, @RequestBody JsonNode n) {
        return service.patch(id, n);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
