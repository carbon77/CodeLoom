package com.codeloom.executor.engine

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.output.ByteArrayOutputStream
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

@Component
class DockerVolumeFileIO(
    private val dockerClient: DockerClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)
    }

    fun writeFile(volumeName: String, fileName: String, content: ByteArray) {
        withHelperContainer(volumeName) { containerId ->
            dockerClient.copyArchiveToContainerCmd(containerId)
                .withRemotePath(WORKSPACE_DIR)
                .withTarInputStream(ByteArrayInputStream(createTar(fileName, content)))
                .exec()
        }
    }

    private fun withHelperContainer(volumeName: String, block: (containerID: String) -> Unit) {
        pullImageIfAbsent(HELPER_CONTAINER_IMAGE_NAME)

        var containerId: String? = null
        try {
            containerId = dockerClient.createContainerCmd(HELPER_CONTAINER_IMAGE_NAME)
                .withHostConfig(
                    HostConfig()
                        .withMounts(
                            listOf(
                                Mount()
                                    .withType(MountType.VOLUME)
                                    .withSource(volumeName)
                                    .withTarget(WORKSPACE_DIR)
                            )
                        )
                )
                .withWorkingDir(WORKSPACE_DIR)
                .withCmd("sleep", "30")
                .exec()
                .id

            block(containerId)
        } finally {
            if (containerId != null) {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec()
            }
        }
    }

    private fun createTar(fileName: String, content: ByteArray): ByteArray {
        val buffer = ByteArrayOutputStream()
        TarArchiveOutputStream(buffer).use { tar ->
            val entry = TarArchiveEntry(fileName)
            entry.size = content.size.toLong()
            tar.putArchiveEntry(entry)
            tar.write(content)
            tar.closeArchiveEntry()
        }
        return buffer.toByteArray()
    }

    private fun pullImageIfAbsent(image: String) {
        try {
            dockerClient.inspectImageCmd(image).exec()
        } catch (_: NotFoundException) {
            logger.info("Image $image not found. Pulling...")
            dockerClient.pullImageCmd(image)
                .start()
                .awaitCompletion(120, TimeUnit.SECONDS)
            logger.info("Image $image is pulled!")
        }
    }
}