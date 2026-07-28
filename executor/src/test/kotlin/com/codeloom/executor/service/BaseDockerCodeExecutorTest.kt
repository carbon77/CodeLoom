package com.codeloom.executor.service

import com.codeloom.executor.service.executor.CodeExecutorService
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DockerTestConfiguration::class])
@TestPropertySource("classpath:application.properties")
abstract class BaseDockerCodeExecutorTest {
    @Autowired
    lateinit var service: CodeExecutorService
}