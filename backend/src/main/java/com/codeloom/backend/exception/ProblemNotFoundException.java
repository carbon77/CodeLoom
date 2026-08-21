package com.codeloom.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProblemNotFoundException extends ResponseStatusException {
    public ProblemNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Problem with id=" + id + " not found");
    }
}
