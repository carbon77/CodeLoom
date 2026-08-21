package com.codeloom.executor.model;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

@Table("test_cases")
public class TestCase {
  @Id
  @Column("test_case_id")
  private final UUID id;

  @Column("problem_id")
  private final long problemId;

  @Column("input")
  private final String input;

  @Column("expected_output")
  private final String expectedOutput;

  @Column("is_public")
  private final boolean isPublic;

  public TestCase(UUID id, long problemId, String input, String expectedOutput, boolean isPublic) {
    this.id = id;
    this.problemId = problemId;
    this.input = input;
    this.expectedOutput = expectedOutput;
    this.isPublic = isPublic;
  }

  public UUID getId() {
    return id;
  }

  public long getProblemId() {
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
}
