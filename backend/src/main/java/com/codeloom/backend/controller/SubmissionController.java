package com.codeloom.backend.controller;

import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/submissions")
public class SubmissionController {
    private final SubmissionService service;

    public SubmissionController(SubmissionService s) {
        service = s;
    }

    @GetMapping
    public Collection<Submission> findSubmissions(Authentication a, @RequestParam long problemId) {
        return service.findSubmissions(problemId, a);
    }

    @PostMapping
    public void sendSubmission(Authentication a, @Valid @RequestBody SendSubmissionRequest q) {
        service.sendSubmission(q, a);
    }
}
