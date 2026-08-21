package com.codeloom.executor.service;

import com.codeloom.executor.engine.DockerImageManager;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.codeloom.executor.engine.DockerVolumeFileIO;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({DockerImageManager.class, DockerVolumeFileIO.class, DockerJudgeEngine.class})
class DockerTestConfiguration {
    @Bean
    DockerClient dockerClient(@Value("${codeloom.docker.host:tcp://localhost:2375}") String host) {
        var clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.getDockerHost())
                .sslConfig(clientConfig.getSSLConfig())
                .maxConnections(10)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        return DockerClientBuilder.getInstance(clientConfig)
                .withDockerHttpClient(httpClient)
                .build();
    }
}
