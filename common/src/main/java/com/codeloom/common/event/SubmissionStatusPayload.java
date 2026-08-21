package com.codeloom.common.event;

import java.util.List;

public record SubmissionStatusPayload(String error, List<TestCaseResult> testCaseResults) {
  public SubmissionStatusPayload() {
    this(null, null);
  }
}
