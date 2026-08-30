package com.codeloom.backend.controller;

import static com.codeloom.backend.config.OpenApiConfig.BAD_REQUEST_RESPONSE_REF;
import static com.codeloom.backend.config.OpenApiConfig.NOT_FOUND_RESPONSE_REF;
import static com.codeloom.backend.security.AuthenticationUtils.getUserId;

import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.dto.SubmissionDto;
import com.codeloom.backend.dto.SubmissionListDto;
import com.codeloom.backend.dto.SubmissionStatusDto;
import com.codeloom.backend.service.SubmissionService;
import com.codeloom.backend.sse.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Authenticated user's code submissions")
public class SubmissionController {
    private final SubmissionService service;
    private final SseService sseService;

    @GetMapping
    @Operation(operationId = "listSubmissions", summary = "List submissions for a problem")
    @ApiResponse(responseCode = "200", description = "Submissions returned")
    public Collection<SubmissionListDto> findSubmissions(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Problem identifier") @RequestParam long problemId) {
        return service.findSubmissions(problemId, authentication);
    }

    @GetMapping("/{submissionId}")
    @Operation(operationId = "getSubmission", summary = "Get submission details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Submission returned"),
        @ApiResponse(responseCode = "404", description = "Submission not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public SubmissionDto findSubmissionDetails(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Submission identifier") @PathVariable("submissionId") UUID submissionId) {
        return service.findSubmissionDetails(authentication, submissionId);
    }

    @PostMapping
    @Operation(operationId = "createSubmission", summary = "Submit code for judging")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Submission accepted"),
        @ApiResponse(responseCode = "400", description = "Submission cannot be judged", ref = BAD_REQUEST_RESPONSE_REF),
        @ApiResponse(responseCode = "404", description = "Problem not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public SubmissionStatusDto sendSubmission(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody SendSubmissionRequest request) {
        return service.sendSubmission(request, authentication);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(operationId = "streamSubmissionStatuses", summary = "Stream submission status changes")
    @ApiResponse(
            responseCode = "200",
            description = "SSE stream established",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(type = "string")))
    public SseEmitter getSseConnection(@Parameter(hidden = true) Authentication authentication) {
        var userId = getUserId(authentication);
        return sseService.createConnection(userId);
    }
}
