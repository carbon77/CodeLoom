package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("topics")
data class Topic(
    @Id @Column("topic_id") var id: UUID? = null,
    @Column("name") var name: String
)