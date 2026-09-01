package com.codeloom.executor.service;

import com.codeloom.executor.config.DockerConstraints;
import com.codeloom.executor.config.ExecutorProperties;
import com.codeloom.executor.engine.DockerContainerPolicy;
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
@Import({DockerImageManager.class, DockerVolumeFileIO.class, DockerJudgeEngine.class, DockerContainerPolicy.class})
public class DockerTestConfiguration {
    @Bean
    ExecutorProperties executorProperties() {
        return new ExecutorProperties(65_536, 65_536);
    }

    @Bean
    DockerConstraints dockerConstraints(
            @Value("${codeloom.executor.tmpfs-limit-bytes:67108864}") long tmpfsLimitBytes,
            @Value("${codeloom.executor.max-open-files:1024}") long maxOpenFiles,
            @Value("${codeloom.executor.max-file-size-bytes:67108864}") long maxFileSizeBytes) {
        return new DockerConstraints(tmpfsLimitBytes, maxOpenFiles, maxFileSizeBytes);
    }

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
