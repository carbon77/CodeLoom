package com.codeloom.backend.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @PersistenceCreator)
@Table("test_case_results")
public class TestCaseResult {
    @Id
    @Column("test_case_result_id")
    private final UUID id;

    @Column("submission_id")
    private final UUID submissionId;

    @Column("input")
    private final String input;

    @Column("expected_output")
    private final String expectedOutput;

    @Column("stdout")
    private final String stdout;

    @Column("stderr")
    private final String stderr;

    @Column("execution_time_ms")
    private final Long executionTimeMs;

    @Column("bytes_used")
    private final Long bytesUsed;

    public TestCaseResult(
            UUID submissionId,
            String input,
            String expectedOutput,
            String stdout,
            String stderr,
            Long executionTimeMs,
            Long bytesUsed) {
        this(null, submissionId, input, expectedOutput, stdout, stderr, executionTimeMs, bytesUsed);
    }
}
