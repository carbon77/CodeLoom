package com.codeloom.executor.engine

import com.codeloom.executor.engine.callbacks.PeakMemoryUsageCallback
import com.codeloom.executor.engine.callbacks.SimpleLogCallback
import com.codeloom.executor.model.TestCase
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.exception.DockerException
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.measureTimedValue

@Component
class DockerJudgeEngine(
    private val dockerClient: DockerClient,
    private val volumeFileIO: DockerVolumeFileIO,
    private val imageManager: DockerImageManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun compile(context: SubmissionContext): CompilationResult {
        logger.info("Compiling: submissionId={} language={}", context.submissionId, context.language)

        val volumeName = volumeName(context.submissionId)
        dockerClient.createVolumeCmd().withName(volumeName).exec()

        try {
            volumeFileIO.writeFile(
                volumeName = volumeName,
                fileName = context.language.sourceFileName,
                content = context.code.toByteArray(),
            )

            if (context.language.compileCommand == null) {
                logger.info("Compilation is not needed: submissionId={}", context.submissionId)
                return CompilationResult(true, "")
            }

            val containerId = createContainer(context, context.language.compileCommand)
            val outcome = runContainer(context, containerId)

            if (outcome.exitCode == 0L) {
                logger.info(
                    "Compiled: submissionId={} timeMs={} memoryUsageBytes={}",
                    context.submissionId,
                    outcome.executionTimeMs,
                    outcome.memoryUsageBytes,
                )
            } else {
                logger.error(
                    "Compilation error: submissionId={} exitCode={}",
                    context.submissionId,
                    outcome.exitCode,
                    Exception(outcome.stderr),
                )
            }
            return CompilationResult(
                isSuccessful = outcome.exitCode == 0L,
                stderr =
                    when (outcome.exitCode) {
                        MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> MEMORY_LIMIT_EXCEEDED_MESSAGE
                        TIMEOUT_EXIT_CODE -> TIMEOUT_MESSAGE
                        else -> outcome.stderr
                    },
            )
        } catch (e: DockerException) {
            logger.error("Compilation failed: submissionId={}", context.submissionId, e)
            cleanup(context.submissionId)
            throw e
        }
    }

    fun runTestCase(
        context: SubmissionContext,
        testCase: TestCase,
    ): RunResult {
        logger.info("Running test case: submissionId={} testCaseId={}", context.submissionId, testCase.id)
        volumeFileIO.writeFile(
            volumeName = volumeName(context.submissionId),
            fileName = "input.txt",
            content = testCase.input.toByteArray(),
        )

        val containerId = createContainer(context, context.language.runCommand)
        val outcome = runContainer(context, containerId)

        logger.info(
            "Test case finished: submissionId={} testCaseId={} exitCode={} timeMs={} memoryUsageBytes={}",
            context.submissionId,
            testCase.id,
            outcome.exitCode,
            outcome.executionTimeMs,
            outcome.memoryUsageBytes,
        )
        return RunResult(
            exitCode = outcome.exitCode,
            stdout = outcome.stdout,
            stderr =
                when (outcome.exitCode) {
                    MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> MEMORY_LIMIT_EXCEEDED_MESSAGE
                    TIMEOUT_EXIT_CODE -> TIMEOUT_MESSAGE
                    else -> outcome.stderr
                },
            executionTimeMs = outcome.executionTimeMs,
            memoryUsageBytes = outcome.memoryUsageBytes,
        )
    }

    private fun runContainer(
        context: SubmissionContext,
        containerId: String,
    ): ContainerOutcome {
        val peakMemoryUsageCallback = PeakMemoryUsageCallback()
        dockerClient.startContainerCmd(containerId).exec()

        val (exitCode, executionDuration) =
            measureTimedValue {
                try {
                    dockerClient.waitContainerCmd(containerId)
                        .start()
                        .awaitStatusCode(context.executionTimeLimitMs ?: DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .toLong()
                } catch (e: Exception) {
                    logger.error("Execution timed out: {}", e.message)
                    runCatching { dockerClient.killContainerCmd(containerId).exec() }
                    TIMEOUT_EXIT_CODE
                }
            }

        runCatching { peakMemoryUsageCallback.close() }
        val memoryUsageBytes = peakMemoryUsageCallback.peak()

        val oomKilled =
            runCatching {
                dockerClient
                    .inspectContainerCmd(containerId)
                    .exec().state?.oomKilled == true
            }.getOrDefault(false)

        val stdout = StringBuilder()
        val stderr = StringBuilder()
        runCatching {
            dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(false)
                .exec(SimpleLogCallback(stdout, stderr))
                .awaitCompletion(30, TimeUnit.SECONDS)
        }

        runCatching { dockerClient.removeContainerCmd(containerId).withForce(true).exec() }

        return ContainerOutcome(
            exitCode = if (oomKilled) MEMORY_LIMIT_EXCEEDED_EXIT_CODE else exitCode,
            stdout = stdout.toString(),
            stderr =
                when (exitCode) {
                    TIMEOUT_EXIT_CODE -> TIMEOUT_MESSAGE
                    MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> MEMORY_LIMIT_EXCEEDED_MESSAGE
                    else -> stderr.toString()
                },
            executionTimeMs = executionDuration.inWholeMilliseconds,
            memoryUsageBytes = memoryUsageBytes,
        )
    }

    private fun createContainer(
        context: SubmissionContext,
        shellCommand: String,
    ): String {
        val isCompiling =
            context.language.compileCommand != null &&
                context.language.compileCommand == shellCommand
        val memoryLimit =
            if (isCompiling || context.memoryUsageLimitBytes == null) {
                DEFAULT_MEMORY_LIMIT_BYTES
            } else {
                context.memoryUsageLimitBytes
            }
        val hostConfig =
            HostConfig.newHostConfig()
                .withBinds(Bind(volumeName(context.submissionId), Volume(WORKSPACE_DIR)))
                .withMemory(memoryLimit)
                .withMemorySwap(memoryLimit)
                .withNetworkMode("none")
                .withReadonlyRootfs(false)

        imageManager.pullImageIfAbsent(context.language.image, timeoutSeconds = 300)

        val containerId =
            dockerClient.createContainerCmd(context.language.image)
                .withHostConfig(hostConfig)
                .withWorkingDir(WORKSPACE_DIR)
                .withCmd("sh", "-c", shellCommand)
                .exec().id
        return containerId
    }

    private fun volumeName(submissionId: UUID): String {
        return "submission-$submissionId"
    }

    fun cleanup(submissionId: UUID) {
        val volumeExists =
            dockerClient.listVolumesCmd()
                .withFilter("name", listOf(volumeName(submissionId)))
                .exec().volumes.isNotEmpty()

        if (volumeExists) {
            dockerClient.removeVolumeCmd(volumeName(submissionId)).exec()
        }
    }
}
