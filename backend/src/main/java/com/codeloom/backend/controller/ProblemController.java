package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateProblemRequest;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.dto.ProblemFilters;
import com.codeloom.backend.dto.ProblemListDto;
import com.codeloom.backend.dto.UpdateProblemRequest;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.service.ProblemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService service;

    @GetMapping("items")
    public List<ProblemListDto> findAllItems(
            Authentication authentication,
            @RequestParam(required = false) Set<ProblemDifficulty> difficulties,
            @RequestParam(required = false) Set<String> topics,
            @RequestParam(defaultValue = "true") boolean publishedOnly) {
        var filters = ProblemFilters.builder()
                .difficulties(difficulties)
                .publishedOnly(publishedOnly)
                .topics(topics)
                .build();
        return service.findItemsByFilters(authentication, filters);
    }

    @GetMapping("{problemId}")
    public Problem findById(Authentication authentication, @PathVariable long problemId) {
        return service.findById(authentication, problemId);
    }

    @GetMapping("slug/{problemSlug}")
    public ProblemDto findDtoBySlug(Authentication authentication, @PathVariable String problemSlug) {
        return service.findDtoBySlug(authentication, problemSlug);
    }

    @DeleteMapping("{problemId}")
    public void delete(@PathVariable long problemId) {
        service.deleteById(problemId);
    }

    @PostMapping
    public Problem create(@Valid @RequestBody CreateProblemRequest request) {
        return service.create(request);
    }

    @PutMapping("/{problemId}")
    public Problem update(@PathVariable long problemId, @Valid @RequestBody UpdateProblemRequest request) {
        return service.update(problemId, request);
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
