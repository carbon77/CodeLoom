package com.codeloom.executor.config

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class DockerClientConfig(
    @Value("\${codeloom.docker.host}")
    private val dockerHost: String,
) {
    @Bean
    fun dockerClient(): DockerClient {
        val config =
            DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build()

        val dockerClient =
            ApacheDockerHttpClient.Builder()
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
}
