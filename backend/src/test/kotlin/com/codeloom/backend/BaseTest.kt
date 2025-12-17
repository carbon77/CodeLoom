package com.codeloom.backend

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class BaseTest {

    companion object {
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:17-alpine").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("test")
        }
    }
}