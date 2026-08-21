package com.codeloom.executor.engine;

import static com.codeloom.executor.engine.CodeExecutionConstants.*;

import com.codeloom.executor.engine.callbacks.SimpleLogCallback;
import com.codeloom.executor.model.TestCase;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DockerJudgeEngine {
    private final DockerClient dockerClient;
    private final DockerVolumeFileIO dockerVolumeFileIO;
    private final DockerImageManager dockerImageManager;

    public CompilationResult compile(SubmissionContext context) {
        String volume = volumeName(context.submissionId());
        dockerClient.createVolumeCmd().withName(volume).exec();

        try {
            dockerVolumeFileIO.writeFile(
                    volume,
                    context.language().getSourceFileName(),
                    context.code().getBytes(StandardCharsets.UTF_8));

            if (context.language().getCompileCommand() == null) {
                return CompilationResult.builder().isSuccessful(true).stderr("").build();
            }

            String containerId = createContainer(context, context.language().getCompileCommand());
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

        String containerId = createContainer(context, context.language().getRunCommand());
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
        return switch ((int) outcome.exitCode()) {
            case MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> MEMORY_LIMIT_EXCEEDED_MESSAGE;
            case TIMEOUT_EXIT_CODE -> TIMEOUT_MESSAGE;
            default -> outcome.stderr();
        };
    }

    @SneakyThrows
    private ContainerOutcome runContainer(SubmissionContext context, String containerId) {
        dockerClient.startContainerCmd(containerId).exec();

        long start = System.nanoTime();
        long exit;
        try {
            exit = dockerClient
                    .waitContainerCmd(containerId)
                    .start()
                    .awaitStatusCode(
                            context.executionTimeLimitMs() == null
                                    ? DEFAULT_TIMEOUT_MS
                                    : context.executionTimeLimitMs(),
                            TimeUnit.MILLISECONDS)
                    .longValue();
        } catch (Exception e) {
            dockerClient.killContainerCmd(containerId).exec();
            exit = TIMEOUT_EXIT_CODE;
        }
        long executionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        boolean isOomKilled = Boolean.TRUE.equals(
                dockerClient.inspectContainerCmd(containerId).exec().getState().getOOMKilled());

        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        dockerClient
                .logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(false)
                .exec(new SimpleLogCallback(out, err))
                .awaitCompletion(30, TimeUnit.SECONDS);
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();

        return ContainerOutcome.builder()
                .exitCode(isOomKilled ? MEMORY_LIMIT_EXCEEDED_EXIT_CODE : exit)
                .stdout(out.toString())
                .stderr(err.toString())
                .memoryUsageBytes(0)
                .executionTimeMs(executionMs)
                .build();
    }

    private String createContainer(SubmissionContext context, String command) {
        boolean compiling = context.language().getCompileCommand() != null
                && context.language().getCompileCommand().equals(command);
        long memory = (compiling || context.memoryUsageLimitBytes() == null)
                ? DEFAULT_MEMORY_LIMIT_BYTES
                : context.memoryUsageLimitBytes();

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(volumeName(context.submissionId()), new Volume(WORKSPACE_DIR)))
                .withMemory(memory)
                .withMemorySwap(memory)
                .withNetworkMode("none")
                .withReadonlyRootfs(false);

        dockerImageManager.pullImageIfAbsent(context.language().getImage(), 300);
        return dockerClient
                .createContainerCmd(context.language().getImage())
                .withHostConfig(hostConfig)
                .withWorkingDir(WORKSPACE_DIR)
                .withCmd("sh", "-c", command)
                .exec()
                .getId();
    }

    private String volumeName(UUID id) {
        return "submission-" + id;
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
