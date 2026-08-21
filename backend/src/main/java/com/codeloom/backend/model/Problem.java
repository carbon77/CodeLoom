package com.codeloom.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Table("problems")
@RequiredArgsConstructor
@Builder
@With
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

    public boolean isDraft() {
        return publishedAt == null;
    }
}
