package com.codeloom.executor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeloom.common.SubmissionStatus;
import org.junit.jupiter.api.Test;

class CodeExecutionExitCodeTest {
    @Test
    void exposesCodesMessagesAndStatuses() {
        assertEquals(124, CodeExecutionExitCode.TIMEOUT.code());
        assertEquals(
                "Execution timed out", CodeExecutionExitCode.TIMEOUT.message().orElseThrow());
        assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, CodeExecutionExitCode.TIMEOUT.status());

        assertEquals(137, CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.code());
        assertEquals(
                "Memory limit exceeded",
                CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.message().orElseThrow());
        assertEquals(SubmissionStatus.MEMORY_LIMIT_EXCEEDED, CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.status());

        assertEquals(SubmissionStatus.RUNTIME_ERROR, CodeExecutionExitCode.OUTPUT_LIMIT.status());
        assertTrue(CodeExecutionExitCode.OUTPUT_LIMIT.message().isEmpty());
    }

    @Test
    void unknownCodeFallsBackToRuntimeError() {
        assertTrue(CodeExecutionExitCode.fromCode(42).isEmpty());
        assertEquals(SubmissionStatus.RUNTIME_ERROR, CodeExecutionExitCode.statusFor(42));
    }
}
