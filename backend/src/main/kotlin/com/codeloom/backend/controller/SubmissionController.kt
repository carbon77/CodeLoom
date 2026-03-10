package com.codeloom.backend.controller

import com.codeloom.backend.dao.SendSubmissionRequest
import com.codeloom.backend.model.Submission
import com.codeloom.backend.service.SubmissionService
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
    fun sendSubmission(
        @RequestBody request: SendSubmissionRequest,
        principal: Principal,
    ) {
        submissionService.sendSubmission(request, principal)
    }
}