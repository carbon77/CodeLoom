package com.codeloom.executor.service

import com.codeloom.executor.service.executor.DockerClientCodeExecutorService
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Duration

@TestConfiguration
class DockerTestConfiguration{
    @Bean
    fun dockerClient(
        @Value("\${codeloom.docker.host}")
        dockerHost: String,
    ): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .build()

        val dockerClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(10)
            .connectionTimeout(Duration.ofSeconds(10))
            .responseTimeout(Duration.ofSeconds(30))
            .build()
        return DockerClientBuilder
            .getInstance(config)
            .withDockerHttpClient(dockerClient)
            .build()
    }

    @Bean
    fun service(dockerClient: DockerClient): DockerClientCodeExecutorService {
        return DockerClientCodeExecutorService(dockerClient)
    }
}