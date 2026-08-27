package com.codeloom.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class SubmissionNotFoundException extends ResponseStatusException {
    public SubmissionNotFoundException(UUID submissionId) {
        super(
                HttpStatus.NOT_FOUND,
                "Submission with id=" + submissionId + " not found"
        );
    }
}
