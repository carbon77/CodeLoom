package com.codeloom.executor.service;

import static com.codeloom.executor.engine.DockerExecutionDefaults.HELPER_CONTAINER_IMAGE_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeloom.common.language.LanguageProperties;
import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.config.DockerTestConfiguration;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.github.dockerjava.api.DockerClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = DockerTestConfiguration.class,
        initializers = ConfigDataApplicationContextInitializer.class)
abstract class DockerTestBase {
    @Autowired
    protected DockerJudgeEngine dockerJudgeEngine;

    @Autowired
    protected DockerClient dockerClient;

    @Autowired
    protected LanguageProperties languageProperties;

    protected UUID submissionId;

    @DynamicPropertySource
    static void dockerProperties(DynamicPropertyRegistry registry) {
        registry.add("codeloom.docker.host", DockerTestBase::dockerHost);
    }

    private static String dockerHost() {
        String configuredHost = System.getenv("CODELOOM_EXECUTOR_DOCKER_HOST");
        if (configuredHost != null && !configuredHost.isBlank()) {
            return configuredHost;
        }
        return System.getProperty("os.name").startsWith("Windows")
                ? "tcp://localhost:2375"
                : "unix:///var/run/docker.sock";
    }

    @BeforeEach
    void generateSubmissionId() {
        submissionId = UUID.randomUUID();
    }

    @AfterEach
    void assertDockerClean() {
        for (LanguageSpec language : languageProperties.languages().values()) {
            assertContainersRemoved(language.image());
        }
        assertContainersRemoved(HELPER_CONTAINER_IMAGE_NAME);
        assertVolumesRemoved(submissionId);
    }

    void assertVolumesRemoved(UUID id) {
        var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of("submission-" + id))
                .exec()
                .getVolumes();
        assertFalse(volumes != null && !volumes.isEmpty());
    }

    void assertContainersRemoved(String image) {
        assertTrue(dockerClient
                .listContainersCmd()
                .withAncestorFilter(List.of(image))
                .exec()
                .isEmpty());
    }
}
