package com.codeloom.executor.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@RequiredArgsConstructor
@Builder
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
}
