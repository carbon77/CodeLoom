package com.codeloom.executor.engine;

import static com.codeloom.executor.config.DockerConstraints.DEFAULT_MEMORY_BYTES;
import static com.codeloom.executor.engine.CodeExecutionExitCode.*;
import static com.codeloom.executor.engine.DockerExecutionDefaults.TIMEOUT_MS;
import static com.codeloom.executor.engine.DockerExecutionDefaults.WORKSPACE_DIR;

import com.codeloom.executor.config.ExecutorProperties;
import com.codeloom.executor.dto.CompilationResult;
import com.codeloom.executor.dto.ContainerOutcome;
import com.codeloom.executor.dto.RunResult;
import com.codeloom.executor.dto.SubmissionContext;
import com.codeloom.executor.engine.callbacks.BoundedLogCallback;
import com.codeloom.executor.engine.callbacks.PeakMemoryUsageCallback;
import com.codeloom.executor.model.TestCase;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Frame;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DockerJudgeEngine {
    private static final long BYTES_PER_MB = 1024L * 1024L;
    private static final String EXECUTION_GATE = "/tmp/.codeloom-start";

    private final DockerClient dockerClient;
    private final DockerVolumeFileIO dockerVolumeFileIO;
    private final DockerImageManager dockerImageManager;
    private final DockerContainerPolicy containerPolicy;
    private final ExecutorProperties properties;

    public CompilationResult compile(SubmissionContext context) {
        String volume = volumeName(context.submissionId());
        dockerClient.createVolumeCmd().withName(volume).exec();

        try {
            dockerVolumeFileIO.writeFile(
                    volume, context.language().sourceFile(), context.code().getBytes(StandardCharsets.UTF_8));

            if (context.language().compileCommand() == null) {
                return CompilationResult.builder().isSuccessful(true).stderr("").build();
            }

            String containerId = createContainer(context, true);
            ContainerOutcome outcome = runContainer(context, containerId);
            return CompilationResult.builder()
                    .isSuccessful(outcome.exitCode() == 0)
                    .stderr(message(outcome))
                    .build();
        } catch (DockerException e) {
            cleanup(context.submissionId());
            throw e;
        }
    }

    public RunResult runTestCase(SubmissionContext context, TestCase testCase) {
        dockerVolumeFileIO.writeFile(
                volumeName(context.submissionId()),
                "input.txt",
                testCase.getInput().getBytes(StandardCharsets.UTF_8));

        String containerId = createContainer(context, false);
        ContainerOutcome outcome = runContainer(context, containerId);
        return RunResult.builder()
                .exitCode(outcome.exitCode())
                .stdout(outcome.stdout())
                .stderr(message(outcome))
                .executionTimeMs(outcome.executionTimeMs())
                .memoryUsageBytes(outcome.memoryUsageBytes())
                .build();
    }

    private String message(ContainerOutcome outcome) {
        return CodeExecutionExitCode.fromCode(outcome.exitCode())
                .flatMap(CodeExecutionExitCode::message)
                .orElse(outcome.stderr());
    }

    @SneakyThrows
    private ContainerOutcome runContainer(SubmissionContext context, String containerId) {
        AtomicBoolean killRequested = new AtomicBoolean();
        BoundedLogCallback logs = new BoundedLogCallback(
                properties.stdoutLimitBytes(),
                properties.stderrLimitBytes(),
                () -> killAsyncOnce(containerId, killRequested));
        PeakMemoryUsageCallback memoryCallback = new PeakMemoryUsageCallback();

        long start = System.nanoTime();
        long exit = ERROR.code();
        boolean isOomKilled = false;
        try (logs;
                memoryCallback) {
            dockerClient
                    .attachContainerCmd(containerId)
                    .withLogs(true)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTimestamps(false)
                    .exec(logs);
            if (!logs.awaitStarted(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while attaching container logs");
            }
            dockerClient.startContainerCmd(containerId).exec();
            dockerClient.statsCmd(containerId).exec(memoryCallback);
            if (!memoryCallback.awaitStarted(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while subscribing to container stats");
            }
            if (!memoryCallback.awaitSample(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting for container stats");
            }
            releaseExecutionGate(containerId);
            try {
                Integer exitCode = dockerClient
                        .waitContainerCmd(containerId)
                        .start()
                        .awaitStatusCode(
                                context.executionTimeLimitMs() == null ? TIMEOUT_MS : context.executionTimeLimitMs(),
                                TimeUnit.MILLISECONDS);
                if (exitCode == null) {
                    killOnce(containerId, killRequested);
                    exit = TIMEOUT.code();
                } else {
                    exit = exitCode;
                }
            } catch (Exception e) {
                killOnce(containerId, killRequested);
                exit = logs.exceeded() ? OUTPUT_LIMIT.code() : TIMEOUT.code();
            }
            logs.awaitCompletion(5, TimeUnit.SECONDS);
            isOomKilled = Boolean.TRUE.equals(dockerClient
                    .inspectContainerCmd(containerId)
                    .exec()
                    .getState()
                    .getOOMKilled());
        } finally {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        }

        long executionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (logs.exceeded()) exit = OUTPUT_LIMIT.code();
        String stderr = logs.exceeded()
                ? boundedError(logs.stderr(), logs.exceededName() + " output limit exceeded")
                : logs.stderr();

        return ContainerOutcome.builder()
                .exitCode(logs.exceeded() ? OUTPUT_LIMIT.code() : isOomKilled ? MEMORY_LIMIT_EXCEEDED.code() : exit)
                .stdout(logs.stdout())
                .stderr(stderr)
                .memoryUsageBytes(memoryCallback.peak())
                .executionTimeMs(executionMs)
                .build();
    }

    private void killOnce(String containerId, AtomicBoolean requested) {
        if (requested.compareAndSet(false, true)) {
            kill(containerId);
        }
    }

    private void killAsyncOnce(String containerId, AtomicBoolean requested) {
        if (requested.compareAndSet(false, true)) {
            Thread.startVirtualThread(() -> kill(containerId));
        }
    }

    private void kill(String containerId) {
        try {
            dockerClient.killContainerCmd(containerId).exec();
        } catch (RuntimeException e) {
            log.debug("Container {} had already stopped or could not be killed", containerId, e);
        }
    }

    private String boundedError(String output, String message) {
        byte[] suffix = (System.lineSeparator() + message).getBytes(StandardCharsets.UTF_8);
        int prefixLimit = Math.max(0, properties.stderrLimitBytes() - suffix.length);
        byte[] prefix = output.getBytes(StandardCharsets.UTF_8);
        if (prefix.length > prefixLimit) {
            prefix = Arrays.copyOf(prefix, prefixLimit);
        }
        String safePrefix = new String(prefix, StandardCharsets.UTF_8).replace("\uFFFD", "");
        return safePrefix.isEmpty() ? message : safePrefix + new String(suffix, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private void releaseExecutionGate(String containerId) {
        String execId = dockerClient
                .execCreateCmd(containerId)
                .withCmd("touch", EXECUTION_GATE)
                .exec()
                .getId();
        try (var callback = new ResultCallback.Adapter<Frame>()) {
            if (!dockerClient.execStartCmd(execId).exec(callback).awaitCompletion(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while releasing container execution gate");
            }
        }
    }

    private String createContainer(SubmissionContext context, boolean compilation) {
        long memory = compilation || context.memoryUsageLimitMb() == null
                ? DEFAULT_MEMORY_BYTES
                : megabytesToBytes(context.memoryUsageLimitMb());

        var command = cmd(context, compilation);

        dockerImageManager.pullImageIfAbsent(context.language().image(), 300);
        return dockerClient
                .createContainerCmd(context.language().image())
                .withHostConfig(containerPolicy.judge(volumeName(context.submissionId()), memory, compilation))
                .withWorkingDir(WORKSPACE_DIR)
                .withCmd(command)
                .exec()
                .getId();
    }

    private List<String> cmd(SubmissionContext context, boolean compilation) {
        var command = compilation
                ? context.language().compileCommand()
                : context.language().runCommand() + " < input.txt";
        return List.of("sh", "-c", "until [ -f " + EXECUTION_GATE + " ]; do sleep 0.01; done; " + command);
    }

    private String volumeName(UUID id) {
        return "submission-" + id;
    }

    static long megabytesToBytes(long megabytes) {
        return Math.multiplyExact(megabytes, BYTES_PER_MB);
    }

    public void cleanup(UUID id) {
        var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName(id)))
                .exec()
                .getVolumes();

        if (volumes != null && !volumes.isEmpty()) {
            dockerClient.removeVolumeCmd(volumeName(id)).exec();
        }
    }
}
