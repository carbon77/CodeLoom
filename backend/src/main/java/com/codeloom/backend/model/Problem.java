package com.codeloom.backend.model;

import java.time.Instant;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

@Table("problems")
public class Problem {
  @Id
  @Column("problem_id")
  private final Long id;

  @Column("slug")
  private final String slug;

  @Column("title")
  private final String title;

  @Column("description")
  private final String description;

  @Column("difficulty")
  private final ProblemDifficulty difficulty;

  @Column("constraints")
  private final ProblemConstraints constraints;

  @Column("examples")
  private final ProblemExamples examples;

  @Column("hints")
  private final String[] hints;

  @CreatedDate
  @Column("created_at")
  private final Instant createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private final Instant updatedAt;

  @Column("published_at")
  private final Instant publishedAt;

  public Problem(
      Long id,
      String slug,
      String title,
      String description,
      ProblemDifficulty difficulty,
      ProblemConstraints constraints,
      ProblemExamples examples,
      String[] hints,
      Instant createdAt,
      Instant updatedAt,
      Instant publishedAt) {
    this.id = id;
    this.slug = slug;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.constraints = constraints;
    this.examples = examples;
    this.hints = hints;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.publishedAt = publishedAt;
  }

  public Problem(String title, String slug) {
    this(
        null,
        slug,
        title,
        "",
        ProblemDifficulty.EASY,
        null,
        null,
        new String[0],
        Instant.now(),
        Instant.now(),
        null);
  }

  public Long getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public ProblemDifficulty getDifficulty() {
    return difficulty;
  }

  public ProblemConstraints getConstraints() {
    return constraints;
  }

  public ProblemExamples getExamples() {
    return examples;
  }

  public String[] getHints() {
    return hints;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public boolean isPublished() {
    return publishedAt != null;
  }

  public Problem updated(
      String slug,
      String title,
      String description,
      ProblemDifficulty difficulty,
      ProblemConstraints constraints,
      ProblemExamples examples,
      String[] hints) {
    return new Problem(
        id,
        slug,
        title,
        description,
        difficulty,
        constraints,
        examples,
        hints,
        createdAt,
        updatedAt,
        publishedAt);
  }

  public Problem withPublishedAt(Instant value) {
    return new Problem(
        id,
        slug,
        title,
        description,
        difficulty,
        constraints,
        examples,
        hints,
        createdAt,
        updatedAt,
        value);
  }
}
