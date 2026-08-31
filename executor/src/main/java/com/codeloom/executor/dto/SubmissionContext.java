package com.codeloom.executor.dto;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.language.LanguageSpec;
import java.util.UUID;
import lombok.Builder;
import lombok.With;

@Builder
public record SubmissionContext(
        UUID submissionId,
        UUID userId,
        long problemId,
        String code,
        LanguageSpec language,
        Long executionTimeLimitMs,
        Long memoryUsageLimitBytes,
        @With SubmissionStatus status) {

    public static SubmissionContext fromEvent(SubmissionEvent event) {
        return SubmissionContext.builder()
                .submissionId(event.submissionId())
                .userId(event.userId())
                .problemId(event.problemId())
                .language(LanguageSpec.fromLanguage(event.language()))
                .code(event.code())
                .executionTimeLimitMs(event.executionTimeLimitMs())
                .memoryUsageLimitBytes(event.memoryUsageLimitBytes())
                .status(SubmissionStatus.PENDING)
                .build();
    }
}
