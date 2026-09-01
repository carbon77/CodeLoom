package com.codeloom.executor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DockerJudgeEngineMemoryLimitTest {
    @Test
    void convertsMegabytesToBytes() {
        assertEquals(67_108_864L, DockerJudgeEngine.megabytesToBytes(64));
    }

    @Test
    void rejectsValuesThatOverflowBytes() {
        assertThrows(ArithmeticException.class, () -> DockerJudgeEngine.megabytesToBytes(Long.MAX_VALUE));
    }
}
