package com.codeloom.backend.controller;

import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService service;

    @GetMapping
    public Collection<Submission> findSubmissions(Authentication authentication, @RequestParam long problemId) {
        return service.findSubmissions(problemId, authentication);
    }

    @PostMapping
    public void sendSubmission(Authentication authentication, @Valid @RequestBody SendSubmissionRequest request) {
        service.sendSubmission(request, authentication);
    }
}
