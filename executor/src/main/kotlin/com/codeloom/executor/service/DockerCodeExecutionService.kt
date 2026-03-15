package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.dto.CodeExecutionResult
import com.codeloom.executor.dto.LanguageSpec
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Service
class DockerCodeExecutionService(
    private val dockerClient: DockerClient,
) : CodeExecutorService {
    private val logger = LoggerFactory.getLogger(DockerCodeExecutionService::class.java)

    override fun run(request: CodeExecutionRequest): CodeExecutionResult {
        var containerId: String? = null
        val tempDir = Files.createTempDirectory("submission")

        logger.info("Starting execution: language={}", request.language)
        return try {
            val langSpec = LanguageSpec.fromLanguage(request.language)

            Files.writeString(tempDir.resolve("input.txt"), request.input, StandardCharsets.UTF_8)
            Files.writeString(tempDir.resolve(langSpec.sourceFileName), request.code, StandardCharsets.UTF_8)

            val response = dockerClient.createContainerCmd(langSpec.image)
                .withHostConfig(
                    HostConfig.newHostConfig()
                        .withBinds(
                            Bind(tempDir.toAbsolutePath().toString(), Volume("/workspace")),
                        ),
                )
                .withWorkingDir("/workspace")
                .withCmd("sh", "-c", langSpec.script())
                .exec()

            containerId = response.id
            val startedAt = Instant.now()
            dockerClient.startContainerCmd(containerId).exec()

            // TODO("Move magic number into variables or enums")
            val exitCode = dockerClient.waitContainerCmd(containerId)
                .start()
                .awaitStatusCode(5, TimeUnit.SECONDS)
            val duration = Duration.between(startedAt, Instant.now()).toMillis()
            if (exitCode == null) {
                logger.warn("Exeuction time out: containerId={}", containerId)
                dockerClient.killContainerCmd(containerId).exec()
                return CodeExecutionResult("", "Execution timed out", duration, 124, null)
            }

            val stdout = StringBuilder()
            val stderr = StringBuilder()
            dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(false)
                .exec(SimpleLogCallback(stdout, stderr))
                .awaitCompletion(2, TimeUnit.SECONDS)
            val bytesUsed = extractMemoryUsage(containerId)
            logger.info(
                "Execution completed: containerId={}, exitCode={}, durationMs={}, bytesUsed={}",
                containerId,
                exitCode,
                duration,
                bytesUsed,
            )
            CodeExecutionResult(
                stdout = stdout.toString(),
                stderr = stderr.toString(),
                exitCode = exitCode,
                duration = duration,
                bytesUsed = bytesUsed,
            )
        } catch (e: Exception) {
            logger.error("Execution failed: language={}", request.language, e)
            CodeExecutionResult("", e.message ?: "Execution failed", 0, 1, null)
        } finally {
            containerId?.let {
                runCatching {
                    dockerClient.removeContainerCmd(it).withForce(true).exec()
                }
            }
            runCatching {
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(Files::deleteIfExists)
            }
        }
    }

    private fun extractMemoryUsage(containerId: String): Long? {
        val memoryUsageRef = AtomicReference<Long?>(null)
        val callback = FirstStatsCallback(memoryUsageRef)

        return runCatching {
            dockerClient.statsCmd(containerId)
                .withNoStream(false)
                .exec(callback)
            callback.awaitFirst(10, TimeUnit.SECONDS)
            callback.close()
            memoryUsageRef.get()
        }
            .onFailure {
                logger.warn("Failed to collect docker stats: containerId={}", containerId, it)
            }
            .getOrNull()
    }
}