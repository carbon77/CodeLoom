package com.codeloom.backend

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
abstract class BaseTest {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:18.1-alpine3.23").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("test")
        }
    }
}