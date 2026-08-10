package com.codeloom.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.security.Principal
import java.util.*

@SpringBootApplication
@EnableJdbcAuditing
class BackendApplication {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}

val Principal.userId: UUID
    get() {
        val jwt = this as (JwtAuthenticationToken)
        return UUID.fromString(jwt.token.subject)
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
