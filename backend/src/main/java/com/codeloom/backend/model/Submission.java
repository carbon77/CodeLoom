package com.codeloom.backend.model;

import com.codeloom.common.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

@Table("submissions")
public class Submission {
  @Id
  @Column("submission_id")
  private final UUID id;

  @Column("user_id")
  private final UUID userId;

  @Column("problem_id")
  private final long problemId;

  @Column("code")
  private final String code;

  @Column("status")
  private final SubmissionStatus status;

  @Column("language")
  private final String language;

  @CreatedDate
  @Column("created_at")
  private final Instant createdAt;

  public Submission(
      UUID id,
      UUID userId,
      long problemId,
      String code,
      SubmissionStatus status,
      String language,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.problemId = problemId;
    this.code = code;
    this.status = status;
    this.language = language;
    this.createdAt = createdAt;
  }

  public Submission(
      UUID userId, long problemId, String code, SubmissionStatus status, String language) {
    this(null, userId, problemId, code, status, language, Instant.now());
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public long getProblemId() {
    return problemId;
  }

  public String getCode() {
    return code;
  }

  public SubmissionStatus getStatus() {
    return status;
  }

  public String getLanguage() {
    return language;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Submission withStatus(SubmissionStatus s) {
    return new Submission(id, userId, problemId, code, s, language, createdAt);
  }
}
