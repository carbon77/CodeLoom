package com.codeloom.backend

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
abstract class BaseTest {

    companion object {
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:18.1-alpine3.23").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("test")
        }

        @DynamicPropertySource
        fun registerDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("logging.level.sql") { "DEBUG" }
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.datasource.driveer-class-name") {
                "org.postgresql.Driver"
            }
        }
    }

}