package com.codeloom.backend.model;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

@Table("test_cases")
public class TestCase {
    @Id
    @Column("test_case_id")
    private final UUID id;

    @Column("problem_id")
    private final Long problemId;

    @Column("input")
    private final String input;

    @Column("expected_output")
    private final String expectedOutput;

    @Column("is_public")
    private final boolean isPublic;

    public TestCase(UUID id, Long problemId, String input, String expectedOutput, boolean isPublic) {
        this.id = id;
        this.problemId = problemId;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.isPublic = isPublic;
    }

    public TestCase(Long problemId, String input, String expectedOutput, boolean isPublic) {
        this(null, problemId, input, expectedOutput, isPublic);
    }

    public UUID getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public TestCase updated(String i, String e, boolean p) {
        return new TestCase(id, problemId, i, e, p);
    }
}
