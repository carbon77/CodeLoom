package com.codeloom.executor.engine;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.language.LanguageSpec;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SubmissionContext(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        LanguageSpec language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes) {

    static public SubmissionContext fromEvent(SubmissionEvent event) {
        return SubmissionContext.builder()
                .submissionId(event.submissionId())
                .userId(event.userId())
                .problemId(event.problemId())
                .language(LanguageSpec.fromLanguage(event.language()))
                .code(event.code())
                .executionTimeLimitMs(event.executionTimeLimitMs())
                .memoryUsageLimitBytes(event.memoryUsageLimitBytes())
                .build();
    }
}
