package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeloom.common.SubmissionState;
import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.dto.RunResult;
import com.codeloom.executor.dto.SubmissionContext;
import com.codeloom.executor.model.TestCase;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {"codeloom.executor.max-open-files=64", "codeloom.executor.max-file-size-bytes=1048576"})
class DockerSandboxSecurityTest extends DockerTestBase {
    private static final TestCase EMPTY_TEST_CASE = TestCase.builder()
            .id(UUID.randomUUID())
            .problemId(1)
            .input("")
            .expectedOutput("")
            .isPublic(false)
            .build();

    @AfterEach
    void removeVolume() {
        dockerJudgeEngine.cleanup(submissionId);
    }

    @Test
    void userCodeCannotReachExternalNetwork() {
        RunResult result = runPython("""
                import socket
                connection = socket.socket()
                connection.settimeout(1)
                try:
                    blocked = connection.connect_ex(("192.0.2.1", 80)) != 0
                finally:
                    connection.close()
                print("BLOCKED" if blocked else "CONNECTED", end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeCannotWriteToRootFilesystem() {
        RunResult result = runPython("""
                try:
                    with open("/constraint-probe", "w") as probe:
                        probe.write("escaped")
                    outcome = "WRITTEN"
                except OSError:
                    outcome = "BLOCKED"
                print(outcome, end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeCannotExecuteFilesFromTmpfs() {
        RunResult result = runPython("""
                import errno
                import os
                import subprocess

                path = "/tmp/constraint-probe"
                with open(path, "w") as probe:
                    probe.write("#!/bin/sh\\nexit 0\\n")
                os.chmod(path, 0o755)
                try:
                    subprocess.run([path], check=False)
                    outcome = "EXECUTED"
                except OSError as error:
                    outcome = "BLOCKED" if error.errno == errno.EACCES else f"ERROR:{error.errno}"
                print(outcome, end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeHasNoCapabilitiesAndCannotGainPrivileges() {
        RunResult result = runPython("""
                status = {}
                with open("/proc/self/status") as source:
                    for line in source:
                        if ":" in line:
                            name, value = line.split(":", 1)
                            status[name] = value.strip()
                blocked = int(status["CapEff"], 16) == 0 and status["NoNewPrivs"] == "1"
                print("BLOCKED" if blocked else f"CAPS:{status['CapEff']};NNP:{status['NoNewPrivs']}", end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeCannotExceedPidLimit() {
        RunResult result = runPython("""
                import errno
                import os
                import signal
                import time

                children = []
                blocked = False
                try:
                    for _ in range(128):
                        pid = os.fork()
                        if pid == 0:
                            time.sleep(10)
                            os._exit(0)
                        children.append(pid)
                except OSError as error:
                    blocked = error.errno == errno.EAGAIN
                finally:
                    for pid in children:
                        try:
                            os.kill(pid, signal.SIGKILL)
                        except ProcessLookupError:
                            pass
                    for pid in children:
                        try:
                            os.waitpid(pid, 0)
                        except ChildProcessError:
                            pass
                print("BLOCKED" if blocked and len(children) < 64 else f"CREATED:{len(children)}", end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeCannotExceedOpenFileLimit() {
        RunResult result = runPython("""
                import errno

                files = []
                blocked = False
                try:
                    for _ in range(128):
                        files.append(open("/dev/null"))
                except OSError as error:
                    blocked = error.errno == errno.EMFILE
                finally:
                    for opened in files:
                        opened.close()
                print("BLOCKED" if blocked and len(files) < 64 else f"OPENED:{len(files)}", end="")
                """);

        assertBlocked(result);
    }

    @Test
    void userCodeCannotExceedFileSizeLimit() {
        RunResult result = runPython("""
                import errno
                import os
                import signal

                signal.signal(signal.SIGXFSZ, signal.SIG_IGN)
                path = "/tmp/large-file"
                blocked = False
                try:
                    with open(path, "wb", buffering=0) as output:
                        for _ in range(2):
                            output.write(b"x" * 1048576)
                except OSError as error:
                    blocked = error.errno == errno.EFBIG
                size = os.path.getsize(path)
                print("BLOCKED" if blocked and size <= 1048576 else f"SIZE:{size}", end="")
                """);

        assertBlocked(result);
    }

    private RunResult runPython(String code) {
        SubmissionContext context = SubmissionContext.builder()
                .submissionId(submissionId)
                .userId(UUID.randomUUID())
                .problemId(1)
                .code(code)
                .language(LanguageSpec.PYTHON)
                .executionTimeLimitMs(10_000L)
                .memoryUsageLimitBytes(64L * 1024 * 1024)
                .state(SubmissionState.RUNNING)
                .build();
        assertTrue(dockerJudgeEngine.compile(context).isSuccessful());
        return dockerJudgeEngine.runTestCase(context, EMPTY_TEST_CASE);
    }

    private void assertBlocked(RunResult result) {
        assertEquals(0, result.exitCode(), result.stderr());
        assertEquals("BLOCKED", result.stdout(), result.stderr());
    }
}
