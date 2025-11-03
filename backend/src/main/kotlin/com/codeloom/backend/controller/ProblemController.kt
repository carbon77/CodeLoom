package com.codeloom.backend.controller

import com.codeloom.backend.model.Problem
import com.codeloom.backend.services.ProblemService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/v1/problems")
class ProblemController(
    private val problemService: ProblemService,
) {

    @GetMapping
    fun findAll(): Iterable<Problem> {
        return problemService.findAll()
    }

    @GetMapping("{problemId}")
    fun findById(@PathVariable("problemId") problemId: UUID): Problem {
        val problem = problemService.findById(problemId)
        if (problem == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id: $problemId not found")
        }
        return problem
    }

    @DeleteMapping("{problemId}")
    fun delete(@PathVariable("problemId") problemId: UUID) {
        problemService.deleteById(problemId)
    }
}