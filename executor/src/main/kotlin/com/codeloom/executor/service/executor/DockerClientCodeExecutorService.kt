package com.codeloom.executor.service.executor

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.dto.CodeExecutionResult
import com.codeloom.executor.languages.LanguageSpec
import com.codeloom.executor.service.executor.callbacks.PeakMemoryUsageCallback
import com.codeloom.executor.service.executor.callbacks.SimpleLogCallback
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.exception.DockerClientException
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.time.measureTimedValue

@Service
class DockerClientCodeExecutorService(
    private val dockerClient: DockerClient,
) : CodeExecutorService {

    companion object {
        private val logger = LoggerFactory.getLogger(DockerClientCodeExecutorService::class.java)
    }

    override fun run(request: CodeExecutionRequest): CodeExecutionResult {
        var containerId: String? = null
        var memoryUsageBytes: Long = 0
        var executionTimeMs: Long = 0

        val tempDir = Files.createTempDirectory("submission")
        val peakMemoryUsageCallback = PeakMemoryUsageCallback()
        val memoryUsageLimitBytes = request.memoryUsageLimitBytes?.times(1024L * 1024L)
            ?: DEFAULT_MEMORY_LIMIT_BYTES

        logger.info("Starting execution: language={}", request.language)
        return try {
            containerId = createContainer(request, tempDir, memoryUsageLimitBytes)

            val (exitCode, executionTimeMs) = runContainerWithMeasureTime(
                containerId = containerId,
                executionTimeLimitMs = request.executionTimeLimitMs ?: DEFAULT_TIMEOUT_MS,
                peakMemoryUsageCallback = peakMemoryUsageCallback,
            )

            runCatching { peakMemoryUsageCallback.close() }
            memoryUsageBytes = peakMemoryUsageCallback.peak()

            val (stdout, stderr) = getContainerOutput(containerId)
            var result = CodeExecutionResult(
                stdout = stdout,
                stderr = stderr,
                exitCode = exitCode,
                executionTimeMs = executionTimeMs,
                memoryUsageBytes = memoryUsageBytes,
            )

            if (inspectOutOfMemoryException(containerId)) {
                result = result.copy(
                    stderr = MEMORY_LIMIT_EXCEEDED_MESSAGE,
                    exitCode = MEMORY_LIMIT_EXCEEDED_EXIT_CODE,
                )
            }

            if (exitCode == TIMEOUT_EXIT_CODE) {
                result = result.copy(stderr = TIMEOUT_MESSAGE)
            }

            logger.info(
                "Execution completed: containerId={}, exitCode={}, executionTimeMs={}, memoryUsageBytes={}",
                containerId,
                exitCode,
                executionTimeMs,
                memoryUsageBytes,
            )
            result
        } catch (e: Exception) {
            logger.error("Execution failed: language={}", request.language, e)
            CodeExecutionResult(
                stderr = e.message ?: "Execution failed",
                exitCode = 1,
                executionTimeMs = executionTimeMs,
                memoryUsageBytes = memoryUsageBytes,
            )
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

    private fun runContainerWithMeasureTime(
        containerId: String,
        executionTimeLimitMs: Long,
        peakMemoryUsageCallback: PeakMemoryUsageCallback,
    ): Pair<Long, Long> {
        val (exitCode, executionTimeDuration) = measureTimedValue {
            try {
                dockerClient.startContainerCmd(containerId).exec()

                dockerClient
                    .statsCmd(containerId)
                    .withNoStream(false)
                    .exec(peakMemoryUsageCallback)

                dockerClient
                    .waitContainerCmd(containerId)
                    .start()
                    .awaitStatusCode(executionTimeLimitMs, TimeUnit.MILLISECONDS)
                    .toLong()
            } catch (e: DockerClientException) {
                if (e.message == "Awaiting status code timeout.") {
                    TIMEOUT_EXIT_CODE
                } else {
                    ERROR_EXIT_CODE
                }
            }
        }
        return Pair(exitCode, executionTimeDuration.inWholeMilliseconds)
    }

    private fun getContainerOutput(containerId: String): Pair<String, String> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        dockerClient.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withTimestamps(false)
            .exec(SimpleLogCallback(stdout, stderr))
            .awaitCompletion(2, TimeUnit.SECONDS)
        return Pair(stdout.toString(), stderr.toString())
    }

    private fun inspectOutOfMemoryException(containerId: String): Boolean {
        return runCatching {
            dockerClient
                .inspectContainerCmd(containerId)
                .exec().state?.oomKilled == true
        }.getOrDefault(false)
    }


    private fun createContainer(
        request: CodeExecutionRequest,
        tempDir: Path,
        memoryUsageLimitBytes: Long,
    ): String {
        val langSpec = LanguageSpec.fromLanguage(request.language)

        Files.writeString(tempDir.resolve("input.txt"), request.input, StandardCharsets.UTF_8)
        Files.writeString(tempDir.resolve(langSpec.sourceFileName), request.code, StandardCharsets.UTF_8)

        return dockerClient.createContainerCmd(langSpec.image)
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withBinds(
                        Bind(tempDir.toAbsolutePath().toString(), Volume("/workspace")),
                    )
                    .withMemory(memoryUsageLimitBytes)
                    .withMemorySwap(memoryUsageLimitBytes),
            )
            .withWorkingDir("/workspace")
            .withCmd("sh", "-c", langSpec.script())
            .exec()
            .id
    }
}
