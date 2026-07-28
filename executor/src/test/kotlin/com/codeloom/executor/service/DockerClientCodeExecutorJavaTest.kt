package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_EXIT_CODE
import com.codeloom.executor.service.executor.MEMORY_LIMIT_EXCEEDED_MESSAGE
import com.codeloom.executor.service.executor.TIMEOUT_EXIT_CODE
import com.codeloom.executor.service.executor.TIMEOUT_MESSAGE
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockerClientCodeExecutorJavaTest : BaseDockerCodeExecutorTest() {

    @Test
    fun runTest_shouldReturnOutput() {
        val req = CodeExecutionRequest(
            code = """
                public class Main {
                    public static void main(String[] args) {
                        System.out.print("Hello, World!");
                    }
                }
            """.trimIndent(),
            language = "java",
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
                import java.util.Scanner;

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int x = sc.nextInt();
                        int y = sc.nextInt();
                        System.out.println((long) Math.pow(x, y));
                    }
                }
            """.trimIndent(),
            language = "java",
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
                import java.nio.file.Files;
                import java.nio.file.Path;

                public class Main {
                    public static void main(String[] args) throws Exception {
                        String text = Files.readString(Path.of("input.txt")).trim();
                        String[] nums = text.split("\\s+");
                        int sum = 0;
                        for (String n : nums) {
                            sum += Integer.parseInt(n);
                        }
                        System.out.print(sum);
                    }
                }
            """.trimIndent(),
            language = "java",
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
                public class Main {
                    public static void main(String[] args) {
                        int x = 1 / 0;
                    }
                }
            """.trimIndent(),
            language = "java",
            input = ""
        )

        val result = service.run(req)
        assertEquals(1, result.exitCode)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("ArithmeticException"))
    }

    @Test
    fun runTestWithWhileTrue_shouldReturnTimeout() {
        val req = CodeExecutionRequest(
            code = """
                public class Main {
                    public static void main(String[] args) {
                        while (true) {}
                    }
                }
            """.trimIndent(),
            language = "java",
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
                import java.util.ArrayList;
                import java.util.List;

                public class Main {
                    public static void main(String[] args) {
                        List<int[]> list = new ArrayList<>();
                        while (true) {
                            list.add(new int[1_000_000]);
                        }
                    }
                }
            """.trimIndent(),
            language = "java",
            memoryLimitMb = 32,
            input = ""
        )

        val result = service.run(req)
        assertEquals(MEMORY_LIMIT_EXCEEDED_EXIT_CODE, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals(MEMORY_LIMIT_EXCEEDED_MESSAGE, result.stderr)
    }
}