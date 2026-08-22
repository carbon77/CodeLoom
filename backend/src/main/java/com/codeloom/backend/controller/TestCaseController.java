package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.dto.UpdateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.service.TestCaseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable long problemId, @RequestParam(required = false) Boolean isPublic) {
        return service.findAllByProblemId(problemId, isPublic);
    }

    @PostMapping
    public TestCase create(@Valid @RequestBody CreateTestCaseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TestCase update(@PathVariable UUID id, @Valid @RequestBody UpdateTestCaseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
