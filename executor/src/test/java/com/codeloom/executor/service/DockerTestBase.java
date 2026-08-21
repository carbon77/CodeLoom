package com.codeloom.executor.service;

import static com.codeloom.executor.engine.CodeExecutionConstants.HELPER_CONTAINER_IMAGE_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.github.dockerjava.api.DockerClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DockerTestConfiguration.class)
@TestPropertySource("classpath:application.properties")
abstract class DockerTestBase {
    @Autowired
    protected DockerJudgeEngine dockerJudgeEngine;

    @Autowired
    protected DockerClient dockerClient;

    protected UUID submissionId;

    @BeforeEach
    void generateSubmissionId() {
        submissionId = UUID.randomUUID();
    }

    @AfterEach
    void assertDockerClean() {
        for (LanguageSpec language : LanguageSpec.values()) {
            assertContainersRemoved(language.getImage());
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
