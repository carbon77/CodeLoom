package com.codeloom.executor.engine;

import static com.codeloom.executor.engine.CodeExecutionConstants.*;

import com.codeloom.executor.engine.callbacks.SimpleLogCallback;
import com.codeloom.executor.model.TestCase;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.slf4j.*;
import org.springframework.stereotype.Component;

@Component
public class DockerJudgeEngine {
    private static final Logger log = LoggerFactory.getLogger(DockerJudgeEngine.class);
    private final DockerClient docker;
    private final DockerVolumeFileIO files;
    private final DockerImageManager images;

    public DockerJudgeEngine(DockerClient d, DockerVolumeFileIO f, DockerImageManager i) {
        docker = d;
        files = f;
        images = i;
    }

    public CompilationResult compile(SubmissionContext c) {
        String volume = volumeName(c.submissionId());
        docker.createVolumeCmd().withName(volume).exec();
        try {
            files.writeFile(volume, c.language().getSourceFileName(), c.code().getBytes(StandardCharsets.UTF_8));
            if (c.language().getCompileCommand() == null) return new CompilationResult(true, "");
            ContainerOutcome o = runContainer(c, createContainer(c, c.language().getCompileCommand()));
            return new CompilationResult(o.exitCode() == 0, message(o));
        } catch (DockerException e) {
            cleanup(c.submissionId());
            throw e;
        }
    }

    public RunResult runTestCase(SubmissionContext c, TestCase t) {
        files.writeFile(volumeName(c.submissionId()), "input.txt", t.getInput().getBytes(StandardCharsets.UTF_8));
        ContainerOutcome o = runContainer(c, createContainer(c, c.language().getRunCommand()));
        return new RunResult(o.exitCode(), o.stdout(), message(o), o.executionTimeMs(), o.memoryUsageBytes());
    }

    private String message(ContainerOutcome o) {
        return o.exitCode() == MEMORY_LIMIT_EXCEEDED_EXIT_CODE
                ? MEMORY_LIMIT_EXCEEDED_MESSAGE
                : o.exitCode() == TIMEOUT_EXIT_CODE ? TIMEOUT_MESSAGE : o.stderr();
    }

    private ContainerOutcome runContainer(SubmissionContext c, String id) {
        docker.startContainerCmd(id).exec();
        long start = System.nanoTime(), exit;
        try {
            exit = docker.waitContainerCmd(id)
                    .start()
                    .awaitStatusCode(
                            c.executionTimeLimitMs() == null ? DEFAULT_TIMEOUT_MS : c.executionTimeLimitMs(),
                            TimeUnit.MILLISECONDS)
                    .longValue();
        } catch (Exception e) {
            try {
                docker.killContainerCmd(id).exec();
            } catch (Exception ignored) {
            }
            exit = TIMEOUT_EXIT_CODE;
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        boolean oom = false;
        try {
            oom = Boolean.TRUE.equals(
                    docker.inspectContainerCmd(id).exec().getState().getOOMKilled());
        } catch (Exception ignored) {
        }
        StringBuilder out = new StringBuilder(), err = new StringBuilder();
        try {
            docker.logContainerCmd(id)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTimestamps(false)
                    .exec(new SimpleLogCallback(out, err))
                    .awaitCompletion(30, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        try {
            docker.removeContainerCmd(id).withForce(true).exec();
        } catch (Exception ignored) {
        }
        long code = oom ? MEMORY_LIMIT_EXCEEDED_EXIT_CODE : exit;
        return new ContainerOutcome(code, out.toString(), err.toString(), 0, elapsed);
    }

    private String createContainer(SubmissionContext c, String command) {
        boolean compiling = c.language().getCompileCommand() != null
                && c.language().getCompileCommand().equals(command);
        long memory =
                compiling || c.memoryUsageLimitBytes() == null ? DEFAULT_MEMORY_LIMIT_BYTES : c.memoryUsageLimitBytes();
        HostConfig h = HostConfig.newHostConfig()
                .withBinds(new Bind(volumeName(c.submissionId()), new Volume(WORKSPACE_DIR)))
                .withMemory(memory)
                .withMemorySwap(memory)
                .withNetworkMode("none")
                .withReadonlyRootfs(false);
        images.pullImageIfAbsent(c.language().getImage(), 300);
        return docker.createContainerCmd(c.language().getImage())
                .withHostConfig(h)
                .withWorkingDir(WORKSPACE_DIR)
                .withCmd("sh", "-c", command)
                .exec()
                .getId();
    }

    private String volumeName(UUID id) {
        return "submission-" + id;
    }

    public void cleanup(UUID id) {
        var v = docker.listVolumesCmd()
                .withFilter("name", List.of(volumeName(id)))
                .exec()
                .getVolumes();
        if (v != null && !v.isEmpty()) docker.removeVolumeCmd(volumeName(id)).exec();
    }
}
