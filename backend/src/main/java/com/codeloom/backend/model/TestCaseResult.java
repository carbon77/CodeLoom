package com.codeloom.backend.model;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

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
            UUID id,
            UUID submissionId,
            String input,
            String expectedOutput,
            String stdout,
            String stderr,
            Long executionTimeMs,
            Long bytesUsed) {
        this.id = id;
        this.submissionId = submissionId;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executionTimeMs = executionTimeMs;
        this.bytesUsed = bytesUsed;
    }

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

    public UUID getId() {
        return id;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Long getBytesUsed() {
        return bytesUsed;
    }
}
