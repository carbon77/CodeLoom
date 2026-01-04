package com.codeloom.backend.config

import com.codeloom.backend.converter.AbstractJsonReadingConverter
import com.codeloom.backend.converter.AbstractJsonWritingConverter
import com.codeloom.backend.model.ProblemConstraints
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.model.ProblemExamples
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.mapping.JdbcValue
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import java.sql.JDBCType

@Configuration
class JdbcConfig(
    private val objectMapper: ObjectMapper,
) : AbstractJdbcConfiguration() {

    override fun userConverters(): List<*> {
        return listOf(
            // Problem Examples
            @ReadingConverter
            object : AbstractJsonReadingConverter<ProblemExamples>(objectMapper, ProblemExamples::class.java) {},

            @WritingConverter
            object : AbstractJsonWritingConverter<ProblemExamples>(objectMapper, ProblemExamples::class.java) {},

            // Problem constraints
            @ReadingConverter
            object : AbstractJsonReadingConverter<ProblemConstraints>(objectMapper, ProblemConstraints::class.java) {},

            @WritingConverter
            object : AbstractJsonWritingConverter<ProblemConstraints>(objectMapper, ProblemConstraints::class.java) {},

            // ProblemDifficulty to PostgreSQL enum
            @WritingConverter
            object : Converter<ProblemDifficulty, JdbcValue> {
                override fun convert(source: ProblemDifficulty) = JdbcValue.of(source, JDBCType.OTHER)
            },
        )
    }
}