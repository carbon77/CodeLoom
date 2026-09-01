package com.codeloom.executor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeloom.common.SubmissionState;
import org.junit.jupiter.api.Test;

class CodeExecutionExitCodeTest {
    @Test
    void exposesCodesMessagesAndStates() {
        assertEquals(124, CodeExecutionExitCode.TIMEOUT.code());
        assertEquals(
                "Execution timed out", CodeExecutionExitCode.TIMEOUT.message().orElseThrow());
        assertEquals(SubmissionState.TIME_LIMIT_EXCEEDED, CodeExecutionExitCode.TIMEOUT.state());

        assertEquals(137, CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.code());
        assertEquals(
                "Memory limit exceeded",
                CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.message().orElseThrow());
        assertEquals(SubmissionState.MEMORY_LIMIT_EXCEEDED, CodeExecutionExitCode.MEMORY_LIMIT_EXCEEDED.state());

        assertEquals(SubmissionState.RUNTIME_ERROR, CodeExecutionExitCode.OUTPUT_LIMIT.state());
        assertTrue(CodeExecutionExitCode.OUTPUT_LIMIT.message().isEmpty());
    }

    @Test
    void unknownCodeFallsBackToRuntimeError() {
        assertTrue(CodeExecutionExitCode.fromCode(42).isEmpty());
        assertEquals(SubmissionState.RUNTIME_ERROR, CodeExecutionExitCode.stateFor(42));
    }
}
