package com.codeloom.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class NoTestCasesException(val problemId: Long) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    "Problem id=$problemId does not have any test cases"
)