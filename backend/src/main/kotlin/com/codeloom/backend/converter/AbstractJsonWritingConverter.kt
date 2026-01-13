package com.codeloom.backend.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.mapping.JdbcValue
import java.sql.JDBCType

abstract class AbstractJsonWritingConverter<T : Any>(
    protected val objectMapper: ObjectMapper,
    protected val clazz: Class<T>,
) : Converter<T, JdbcValue> {

    override fun convert(source: T): JdbcValue {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = objectMapper.writeValueAsString(source)
        return JdbcValue.of(obj, JDBCType.OTHER)
    }
}