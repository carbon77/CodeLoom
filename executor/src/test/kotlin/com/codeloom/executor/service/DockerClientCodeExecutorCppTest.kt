package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_EXIT_CODE
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_MESSAGE
import com.codeloom.executor.service.executor.TIMEOUT_EXIT_CODE
import com.codeloom.executor.service.executor.TIMEOUT_MESSAGE
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DockerClientCodeExecutorCppTest : BaseDockerCodeExecutorTest() {

    @Test
    fun runTest_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                #include <iostream>

                int main() {
                    std::cout << "Hello, World!";
                }
            """.trimIndent(),
            language = "cpp",
            input = ""
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("Hello, World!", result.stdout)
    }

    @Test
    fun runTestWithInput_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                #include <iostream>
                using namespace std;

                long long powi(long long a, int b) {
                    long long ans = 1;
                    while (b--) ans *= a;
                    return ans;
                }

                int main() {
                    int x, y;
                    cin >> x >> y;
                    cout << powi(x, y) << '\n';
                }
            """.trimIndent(),
            language = "cpp",
            input = "2 3"
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("8\n", result.stdout)
    }

    @Test
    fun runTestWithReadingInputFile_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                #include <fstream>
                #include <iostream>

                using namespace std;

                int main() {
                    ifstream fin("input.txt");
                    int x, y;
                    fin >> x >> y;
                    cout << x + y;
                }
            """.trimIndent(),
            language = "cpp",
            input = "2 3"
        )

        val result = service.run(req)
        assertEquals(0, result.exitCode)
        assertEquals("5", result.stdout)
    }

    @Test
    fun runTestWithError_shouldReturnStderr() {
        val req = CodeExecutionRequest(
            code = """
                int main() {
                    throw 42;
                }
            """.trimIndent(),
            language = "cpp",
            input = ""
        )

        val result = service.run(req)
        assertNotEquals(0, result.exitCode)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.isNotBlank())
    }

    @Test
    fun runTestWithWhileTrue_shouldReturnTimeout() {
        val req = CodeExecutionRequest(
            code = """
                int main() {
                    while (true) {}
                }
            """.trimIndent(),
            language = "cpp",
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
            code = """
                #include <vector>

                int main() {
                    std::vector<int*> v;
                    while (true) {
                        v.push_back(new int[1000000]);
                    }
                }
            """.trimIndent(),
            language = "cpp",
            memoryLimitMb = 32,
            input = ""
        )

        val result = service.run(req)
        assertEquals(MEMORY_LIMIT_EXCEEDED_EXIT_CODE, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals(MEMORY_LIMIT_EXCEEDED_MESSAGE, result.stderr)
    }

}