package com.codeloom.backend.controller

import com.codeloom.backend.dao.SendSubmissionRequest
import com.codeloom.backend.model.Submission
import com.codeloom.backend.service.SubmissionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    ): Collection<Submission> = submissionService.findSubmissions(problemId, principal)

    @PostMapping
    fun sendSubmission(
        @RequestBody request: SendSubmissionRequest,
        principal: Principal,
    ) {
        submissionService.sendSubmission(request, principal)
    }
}
