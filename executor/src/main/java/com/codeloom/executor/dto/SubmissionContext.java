package com.codeloom.executor.dto;

import com.codeloom.common.SubmissionState;
import com.codeloom.common.event.TestCaseResultDto;
import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.model.TestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record SubmissionContext(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        LanguageSpec language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes,
        List<TestCase> testCases,
        String error,
        List<TestCaseResultDto> testCaseResults,
        SubmissionState state) {

    public SubmissionContext addTestResult(TestCaseResultDto result) {
        var newTestResults = new ArrayList<>(testCaseResults == null ? List.of() : testCaseResults);
        newTestResults.add(result);
        return withTestCaseResults(newTestResults);
    }
}
