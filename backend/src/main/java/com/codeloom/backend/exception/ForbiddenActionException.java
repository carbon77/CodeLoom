package com.codeloom.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ForbiddenActionException extends ResponseStatusException {
  public ForbiddenActionException() {
    super(HttpStatus.FORBIDDEN, "This actions is forbidden");
  }
}
