package com.codeloom.backend.model;

import java.time.Instant;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("problems")
@Builder(toBuilder = true)
@With
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Problem {

    @Id
    @Column("problem_id")
    private Long id; // must be null for new entities so Spring Data JDBC does an INSERT

    @Column("slug")
    private String slug;

    @Column("title")
    private String title;

    @Builder.Default
    @Column("description")
    private String description = "";

    @Builder.Default
    @Column("difficulty")
    private ProblemDifficulty difficulty = ProblemDifficulty.EASY;

    @Column("constraints")
    private ProblemConstraints constraints;

    @Column("examples")
    private ProblemExamples examples;

    @Builder.Default
    @Column("hints")
    private List<String> hints = List.of();

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Column("published_at")
    private Instant publishedAt;

    public boolean isDraft() {
        return publishedAt == null;
    }
}
