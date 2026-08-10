package com.codeloom.executor.engine

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.output.ByteArrayOutputStream
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class DockerVolumeFileIO(
    private val dockerClient: DockerClient,
    private val imageManager: DockerImageManager,
) {
    fun writeFile(
        volumeName: String,
        fileName: String,
        content: ByteArray,
    ) {
        withHelperContainer(volumeName) { containerId ->
            dockerClient.copyArchiveToContainerCmd(containerId)
                .withRemotePath(WORKSPACE_DIR)
                .withTarInputStream(ByteArrayInputStream(createTar(fileName, content)))
                .exec()
        }
    }

    private fun withHelperContainer(
        volumeName: String,
        block: (containerID: String) -> Unit,
    ) {
        imageManager.pullImageIfAbsent(HELPER_CONTAINER_IMAGE_NAME)

        var containerId: String? = null
        try {
            containerId =
                dockerClient.createContainerCmd(HELPER_CONTAINER_IMAGE_NAME)
                    .withHostConfig(
                        HostConfig()
                            .withMounts(
                                listOf(
                                    Mount()
                                        .withType(MountType.VOLUME)
                                        .withSource(volumeName)
                                        .withTarget(WORKSPACE_DIR),
                                ),
                            ),
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

    private fun createTar(
        fileName: String,
        content: ByteArray,
    ): ByteArray {
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
}
