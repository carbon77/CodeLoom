package com.codeloom.executor.engine

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DockerImageManager(
    private val dockerClient: DockerClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)
    }

    fun pullImageIfAbsent(
        image: String,
        timeoutSeconds: Long = 120,
    ) {
        try {
            dockerClient.inspectImageCmd(image).exec()
        } catch (_: NotFoundException) {
            logger.info("Image $image not found. Pulling...")
            dockerClient.pullImageCmd(image)
                .start()
                .awaitCompletion(timeoutSeconds, TimeUnit.SECONDS)
            logger.info("Image $image is pulled!")
        }
    }
}
