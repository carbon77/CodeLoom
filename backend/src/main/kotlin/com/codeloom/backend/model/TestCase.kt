package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("test_cases")
class TestCase(
    @Id
    @Column("test_case_id")
    val id: UUID? = null,

    @Column("problem_id")
    val problemId: Long,

    val input: String,
    @Column("expected_output")
    val expectedOutput: String,

    @Column("is_hidden")
    val isHidden: Boolean,
)