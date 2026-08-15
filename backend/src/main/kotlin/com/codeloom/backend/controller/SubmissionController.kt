package com.codeloom.backend.controller

import com.codeloom.backend.config.BAD_REQUEST_RESPONSE_REF
import com.codeloom.backend.dto.SendSubmissionRequest
import com.codeloom.backend.model.Submission
import com.codeloom.backend.service.SubmissionService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/submissions")
class SubmissionController(
    private val submissionService: SubmissionService,
) {
    @GetMapping
    fun findSubmissions(
        auth: Authentication,
        @RequestParam("problemId") problemId: Long,
    ): Collection<Submission> {
        return submissionService.findSubmissions(problemId, auth)
    }

    @PostMapping
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF)
    fun sendSubmission(
        auth: Authentication,
        @Valid @RequestBody request: SendSubmissionRequest,
    ) {
        submissionService.sendSubmission(request, auth)
    }
}
