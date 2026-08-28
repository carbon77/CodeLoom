package com.codeloom.backend.controller;

import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.dto.SubmissionDto;
import com.codeloom.backend.dto.SubmissionListDto;
import com.codeloom.backend.dto.SubmissionStatusDto;
import com.codeloom.backend.sse.SseService;
import com.codeloom.backend.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.UUID;

import static com.codeloom.backend.security.AuthenticationUtils.getUserId;

@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService service;
    private final SseService sseService;

    @GetMapping
    public Collection<SubmissionListDto> findSubmissions(Authentication authentication, @RequestParam long problemId) {
        return service.findSubmissions(problemId, authentication);
    }

    @GetMapping("/{submissionId}")
    public SubmissionDto findSubmissionDetails(
            Authentication authentication, @PathVariable("submissionId") UUID submissionId) {
        return service.findSubmissionDetails(authentication, submissionId);
    }

    @PostMapping
    public SubmissionStatusDto sendSubmission(
            Authentication authentication, @Valid @RequestBody SendSubmissionRequest request) {
        return service.sendSubmission(request, authentication);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getSseConnection(Authentication authentication) {
        var userId = getUserId(authentication);
        return sseService.createConnection(userId);
    }
}
