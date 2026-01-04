package com.codeloom.backend.config

import com.codeloom.backend.model.Problem
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ProblemTimestampFieldsGenerator : BeforeConvertCallback<Problem> {
    override fun onBeforeConvert(aggregate: Problem): Problem {
        val now = Instant.now()
        return when (aggregate.id) {
            null -> aggregate.copy(
                createdAt = now,
                updatedAt = now,
            )

            else -> aggregate.copy(
                updatedAt = now,
            )
        }
    }
}