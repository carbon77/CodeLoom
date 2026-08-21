package com.codeloom.executor.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.*;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class DockerClientConfig {
    private final String host;

    public DockerClientConfig(@Value("${codeloom.docker.host}") String h) {
        host = h;
    }

    @Bean
    DockerClient dockerClient() {
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
