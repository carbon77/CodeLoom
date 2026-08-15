package com.codeloom.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ForbiddenActionException : ResponseStatusException(
    HttpStatus.FORBIDDEN,
    "This actions is forbidden",
)
