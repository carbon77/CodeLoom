package com.codeloom.executor.service;

import static com.codeloom.executor.engine.CodeExecutionConstants.HELPER_CONTAINER_IMAGE_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.engine.DockerJudgeEngine;
import com.github.dockerjava.api.DockerClient;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DockerTestConfiguration.class)
@TestPropertySource("classpath:application.properties")
abstract class DockerTestBase {
  @Autowired protected DockerJudgeEngine dockerJudgeEngine;
  @Autowired protected DockerClient dockerClient;
  protected UUID submissionId;

  @BeforeEach
  void generateSubmissionId() {
    submissionId = UUID.randomUUID();
  }

  @AfterEach
  void assertDockerClean() {
    for (LanguageSpec s : LanguageSpec.values()) assertContainersRemoved(s.getImage());
    assertContainersRemoved(HELPER_CONTAINER_IMAGE_NAME);
    assertVolumesRemoved(submissionId);
  }

  void assertVolumesRemoved(UUID id) {
    var v =
        dockerClient
            .listVolumesCmd()
            .withFilter("name", List.of("submission-" + id))
            .exec()
            .getVolumes();
    assertFalse(v != null && !v.isEmpty());
  }

  void assertContainersRemoved(String image) {
    assertFalse(
        !dockerClient.listContainersCmd().withAncestorFilter(List.of(image)).exec().isEmpty());
  }
}
