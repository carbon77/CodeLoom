package com.codeloom.executor.service

import com.codeloom.executor.engine.DockerJudgeEngine
import com.codeloom.executor.engine.HELPER_CONTAINER_IMAGE_NAME
import com.codeloom.common.language.LanguageSpec
import com.github.dockerjava.api.DockerClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.test.assertFalse

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DockerTestConfiguration::class])
@TestPropertySource("classpath:application.properties")
abstract class DockerTestBase {
    @Autowired
    protected lateinit var dockerJudgeEngine: DockerJudgeEngine

    @Autowired
    protected lateinit var dockerClient: DockerClient

    protected lateinit var submissionId: UUID

    @BeforeEach
    fun generateSubmissionId() {
        submissionId = UUID.randomUUID()
    }

    @AfterEach
    fun assertDockerClean() {
        LanguageSpec.entries.forEach {
            assertContainersRemoved(it.image)
        }
        assertContainersRemoved(HELPER_CONTAINER_IMAGE_NAME)
        assertVolumesRemoved(submissionId)
    }

    protected fun assertVolumesRemoved(submissionId: UUID) {
        val volumeExists =
            dockerClient.listVolumesCmd()
                .withFilter("name", listOf("submission-$submissionId"))
                .exec()
                .volumes
                .isNotEmpty()
        assertFalse(volumeExists)
    }

    protected fun assertContainersRemoved(image: String) {
        val containerExists =
            dockerClient.listContainersCmd()
                .withAncestorFilter(listOf(image))
                .exec()
                .isNotEmpty()
        assertFalse(containerExists)
    }
}
