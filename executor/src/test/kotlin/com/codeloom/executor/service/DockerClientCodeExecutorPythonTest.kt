package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_EXIT_CODE
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_MESSAGE
import com.codeloom.executor.service.executor.TIMEOUT_EXIT_CODE
import com.codeloom.executor.service.executor.TIMEOUT_MESSAGE
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DockerClientCodeExecutorPythonTest : BaseDockerCodeExecutorTest() {
    @Test
    fun runTest_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = "print('Hello, World!', end='')",
            language = "python",
            input = "Igor"
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("Hello, World!", result.stdout)
    }

    @Test
    fun runTestWithInput_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                import math
                x, y = map(int, input().split())
                print(math.pow(x, y))
            """.trimIndent(),
            language = "python",
            input = "2 3"
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("8.0\n", result.stdout)
    }

    @Test
    fun runTestWithReadingInputFile_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                def sum_s(str):
                    return sum(map(int, str.split()))
                    
                with open('input.txt', 'r') as f:
                    print(sum_s(f.read()), end='')
            """.trimIndent(),
            language = "python",
            input = "2 3"
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("5", result.stdout)
    }

    @Test
    fun runTestWithError_shouldReturnStderr() {
        val req = CodeExecutionRequest(
            code = "print(1 / 0)",
            language = "python",
            input = ""
        )

        val result = service.run(req)
        assertEquals(1, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals(
            """Traceback (most recent call last):
  File "/workspace/main.py", line 1, in <module>
    print(1 / 0)
          ~~^~~
ZeroDivisionError: division by zero
""", result.stderr
        )
    }

    @Test
    fun runTestWithWhileTrue_shouldReturnTimeout() {
        val req = CodeExecutionRequest(
            code = """
                while True:
                    pass
            """.trimIndent(),
            language = "python",
            input = ""
        )

        val result = service.run(req)
        assertEquals(TIMEOUT_EXIT_CODE, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals(TIMEOUT_MESSAGE, result.stderr)
    }

    @Test
    fun runTestWithTooMuchMemory_shouldReturnOutOfMemory() {
        val req = CodeExecutionRequest(
            code = "big_list = list(range(10_000_000))",
            language = "python",
            input = "",
            memoryLimitMb = 6L,
        )

        val result = service.run(req)
        assertEquals(MEMORY_LIMIT_EXCEEDED_EXIT_CODE, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals(MEMORY_LIMIT_EXCEEDED_MESSAGE, result.stderr)
    }
}
