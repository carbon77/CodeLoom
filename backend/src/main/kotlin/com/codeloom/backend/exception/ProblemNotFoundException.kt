package com.codeloom.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ProblemNotFoundException(problemId: Long) : ResponseStatusException(
    HttpStatus.NOT_FOUND,
    "Problem with id=$problemId not found",
)
