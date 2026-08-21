package com.codeloom.executor.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfig {

    @Bean
    DockerClient dockerClient(@Value("${codeloom.docker.host}") String host) {
        var c = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .build();
        var http = new ApacheDockerHttpClient.Builder()
                .dockerHost(c.getDockerHost())
                .sslConfig(c.getSSLConfig())
                .maxConnections(10)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        return DockerClientBuilder.getInstance(c).withDockerHttpClient(http).build();
    }
}
