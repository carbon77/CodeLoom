package com.codeloom.backend.controller;

import com.codeloom.backend.dto.*;
import com.codeloom.backend.model.*;
import com.codeloom.backend.service.ProblemService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/v1/problems")
public class ProblemController {
    private final ProblemService service;

    public ProblemController(ProblemService s) {
        service = s;
    }

    @GetMapping("items")
    public List<ProblemListDto> findAllItems(
            Authentication a,
            @RequestParam(required = false) Set<ProblemDifficulty> difficulties,
            @RequestParam(required = false) Set<String> topics,
            @RequestParam(defaultValue = "true") boolean publishedOnly) {
        return service.findItemsByFilters(a, new ProblemFilters(difficulties, publishedOnly, topics));
    }

    @GetMapping("{problemId}")
    public Problem findById(Authentication a, @PathVariable long problemId) {
        return service.findById(a, problemId);
    }

    @GetMapping("slug/{problemSlug}")
    public ProblemDto findDtoBySlug(Authentication a, @PathVariable String problemSlug) {
        return service.findDtoBySlug(a, problemSlug);
    }

    @DeleteMapping("{problemId}")
    public void delete(@PathVariable long problemId) {
        service.deleteById(problemId);
    }

    @PostMapping
    public Problem create(@Valid @RequestBody CreateProblemRequest q) {
        return service.create(q);
    }

    @PutMapping("/{problemId}")
    public Problem update(@PathVariable long problemId, @RequestBody JsonNode n) {
        return service.update(problemId, n);
    }

    @PatchMapping("/{problemId}/publish")
    public void publish(@PathVariable long problemId) {
        service.publish(problemId);
    }

    @PatchMapping("/{problemId}/unpublish")
    public void unpublish(@PathVariable long problemId) {
        service.unpublish(problemId);
    }
}
