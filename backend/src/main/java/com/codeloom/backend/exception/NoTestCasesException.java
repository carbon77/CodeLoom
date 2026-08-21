package com.codeloom.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoTestCasesException extends ResponseStatusException {
  private final long problemId;

  public NoTestCasesException(long id) {
    super(HttpStatus.BAD_REQUEST, "Problem id=" + id + " does not have any test cases");
    problemId = id;
  }

  public long getProblemId() {
    return problemId;
  }
}
