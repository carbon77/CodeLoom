package com.codeloom.backend.model;

import com.codeloom.common.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @PersistenceCreator)
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

    public Submission(UUID userId, long problemId, String code, SubmissionStatus status, String language) {
        this(null, userId, problemId, code, status, language, Instant.now());
    }

    public Submission withStatus(SubmissionStatus s) {
        return new Submission(id, userId, problemId, code, s, language, createdAt);
    }
}
