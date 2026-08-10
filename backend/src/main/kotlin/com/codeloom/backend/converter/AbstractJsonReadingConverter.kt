package com.codeloom.backend.converter

import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import tools.jackson.databind.ObjectMapper

abstract class AbstractJsonReadingConverter<T>(
    private val objectMapper: ObjectMapper,
    private val clazz: Class<T>,
) : Converter<PGobject, T> {
    override fun convert(source: PGobject): T {
        return objectMapper.readValue(source.value, clazz)
    }
}
