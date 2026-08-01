package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("test_case_results")
class TestCaseResult(
    @Id
    @Column("test_case_result_id")
    val id: UUID? = null,

    @Column("submission_id")
    val submissionId: UUID,

    @Column("input")
    val input: String,

    @Column("expected_output")
    val expectedOutput: String,

    @Column("stdout")
    val stdout: String,

    @Column("stderr")
    val stderr: String? = null,

    @Column("execution_time_ms")
    val executionTimeMs: Long? = null,

    @Column("bytes_used")
    val bytesUsed: Long? = null,
)
