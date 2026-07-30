package com.codeloom.backend

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.security.Principal
import java.util.*

@SpringBootApplication
@EnableJdbcAuditing
class BackendApplication {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
}

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}

val Principal.userId: UUID
    get() {
        val jwt = this as (JwtAuthenticationToken)
        return UUID.fromString(jwt.token.subject)
    }