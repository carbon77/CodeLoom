package com.codeloom.common.event;

import java.util.List;
import lombok.Builder;

@Builder
public record SubmissionStatePayload(String error, List<TestCaseResultDto> testCaseResults) {}
