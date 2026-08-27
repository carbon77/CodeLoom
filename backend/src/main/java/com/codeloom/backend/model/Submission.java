package com.codeloom.backend.model;

import com.codeloom.common.SubmissionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @PersistenceCreator)
@With
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

    @Column("error_message")
    private final String errorMessage;

    @CreatedDate
    @Column("created_at")
    private final Instant createdAt;
}
