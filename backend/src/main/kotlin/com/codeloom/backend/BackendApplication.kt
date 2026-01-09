package com.codeloom.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing

@SpringBootApplication
@EnableJdbcAuditing
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
