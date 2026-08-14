package com.codeloom.backend.controller

import com.codeloom.backend.config.BAD_REQUEST_RESPONSE_REF
import com.codeloom.backend.dto.SendSubmissionRequest
import com.codeloom.backend.model.Submission
import com.codeloom.backend.service.SubmissionService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/submissions")
class SubmissionController(
    private val submissionService: SubmissionService,
) {
    @GetMapping
    fun findSubmissions(
        @RequestParam("problemId") problemId: Long,
        principal: Principal,
    ): Collection<Submission> {
        return submissionService.findSubmissions(problemId, principal)
    }

    @PostMapping
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF)
    fun sendSubmission(
        @Valid @RequestBody request: SendSubmissionRequest,
        principal: Principal,
    ) {
        submissionService.sendSubmission(request, principal)
    }
}
