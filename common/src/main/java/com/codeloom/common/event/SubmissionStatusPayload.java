package com.codeloom.common.event;

import java.util.List;
import lombok.Builder;

@Builder
public record SubmissionStatusPayload(String error, List<TestCaseResultDto> testCaseResults) {}
