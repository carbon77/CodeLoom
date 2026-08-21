package com.codeloom.executor.service;

import com.codeloom.executor.engine.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.*;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

@TestConfiguration
@Import({DockerImageManager.class, DockerVolumeFileIO.class, DockerJudgeEngine.class})
class DockerTestConfiguration {
    @Bean
    DockerClient dockerClient(@Value("${codeloom.docker.host:tcp://localhost:2375}") String host) {
        var c = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .build();
        var h = new ApacheDockerHttpClient.Builder()
                .dockerHost(c.getDockerHost())
                .sslConfig(c.getSSLConfig())
                .maxConnections(10)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        return DockerClientBuilder.getInstance(c).withDockerHttpClient(h).build();
    }
}
