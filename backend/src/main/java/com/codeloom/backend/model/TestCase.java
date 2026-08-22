package com.codeloom.backend.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @PersistenceCreator)
@With
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

    public boolean getIsPublic() {
        return isPublic;
    }
}
