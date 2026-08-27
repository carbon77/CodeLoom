package com.codeloom.backend.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SubmissionNotFoundException extends ResponseStatusException {
    public SubmissionNotFoundException(UUID submissionId) {
        super(HttpStatus.NOT_FOUND, "Submission with id=" + submissionId + " not found");
    }
}
