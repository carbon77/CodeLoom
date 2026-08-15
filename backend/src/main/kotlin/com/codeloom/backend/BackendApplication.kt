package com.codeloom.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

@SpringBootApplication
@EnableJdbcAuditing
class BackendApplication {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}

inline fun <reified T> JsonNode.patchValue(
    name: String,
    objectMapper: ObjectMapper,
    current: T,
): T {
    val value = get(name) ?: return current
    if (value.isNull) return current
    return objectMapper.treeToValue(value, T::class.java)
}
