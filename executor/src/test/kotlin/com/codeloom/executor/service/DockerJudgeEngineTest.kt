package com.codeloom.executor.service

import com.codeloom.executor.engine.SubmissionContext
import com.codeloom.executor.languages.LanguageSpec
import com.codeloom.executor.model.TestCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DockerJudgeEngineTest : DockerTestBase() {

    @AfterEach
    fun tearDown() {
        dockerClient.removeVolumeCmd("submission-$submissionId").exec()
    }

    @Nested
    inner class Compile {
        @Test
        fun testCorrectPython_shouldNotCompile() {
            val result = dockerJudgeEngine.compile(
                SubmissionContext(
                    submissionId = submissionId,
                    language = LanguageSpec.PYTHON,
                    code = "print('Hello world')"
                )
            )

            assertEquals(true, result.isSuccessful)
            assertEquals("", result.stderr)
        }

        @Test
        fun testCorrectJava_shouldCompile() {
            val result = dockerJudgeEngine.compile(
                SubmissionContext(
                    submissionId = submissionId,
                    language = LanguageSpec.JAVA,
                    code = """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("Hello world");
                        }
                    }
                """.trimIndent()
                )
            )
            assertEquals(true, result.isSuccessful)
            assertEquals("", result.stderr)
        }

        @Test
        fun testIncorrectJava_shouldCompileError() {
            val result = dockerJudgeEngine.compile(
                SubmissionContext(
                    submissionId = submissionId,
                    language = LanguageSpec.JAVA,
                    code = """
                    public class Main {
                    
                            System.out.println("Hello world");
                        }
                    }
                """.trimIndent()
                )
            )
            assertEquals(false, result.isSuccessful)
            assertNotEquals("", result.stderr)
        }

        @Test
        fun testCorrectCpp_shouldCompile() {
            val context = SubmissionContext(
                submissionId = submissionId,
                language = LanguageSpec.CPP,
                code = """
#include <iostream>

int main() {
    int firstNumber, secondNumber, sum;
    std::cin >> firstNumber >> secondNumber;
    sum = firstNumber + secondNumber;
    std::cout << sum;
    return 0;
}
                """.trimIndent()
            )
            val compileResult = dockerJudgeEngine.compile(context)
            assertEquals(true, compileResult.isSuccessful)
            assertEquals("", compileResult.stderr)
        }
    }

    @Nested
    inner class Run {
        private val testCase = TestCase(
            id = UUID.randomUUID(),
            problemId = 1,
            input = "2 3",
            expectedOutput = "5",
            isPublic = true,
        )

        @Test
        fun testCorrectPython_shouldReturn() {
            val context = SubmissionContext(
                submissionId = submissionId,
                language = LanguageSpec.PYTHON,
                code = "print(sum(map(int, input().split())), end='')"
            )
            dockerJudgeEngine.compile(context)
            val result = dockerJudgeEngine.runTestCase(
                context = context,
                testCase = testCase
            )

            assertEquals(0, result.exitCode)
            assertEquals("5", result.stdout)
            assertEquals("", result.stderr)
        }

        @Test
        fun testCorrectJava_shouldReturn() {
            val context = SubmissionContext(
                submissionId = submissionId,
                language = LanguageSpec.JAVA,
                code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner in = new Scanner(System.in);
                            System.out.print(in.nextInt() + in.nextInt());
                        }
                    }
                """.trimIndent()
            )
            val compileResult = dockerJudgeEngine.compile(context)
            assertEquals(true, compileResult.isSuccessful)
            assertEquals("", compileResult.stderr)

            val result = dockerJudgeEngine.runTestCase(
                context = context,
                testCase = testCase,
            )
            assertEquals(0, result.exitCode)
            assertEquals("5", result.stdout)
            assertEquals("", result.stderr)
        }

        @Test
        fun testCorrectCpp_shouldReturn() {
            val context = SubmissionContext(
                submissionId = submissionId,
                language = LanguageSpec.CPP,
                code = """
#include <iostream>

int main() {
    int firstNumber, secondNumber, sum;
    std::cin >> firstNumber >> secondNumber;
    sum = firstNumber + secondNumber;
    std::cout << sum;
    return 0;
}
                """.trimIndent()
            )
            val compileResult = dockerJudgeEngine.compile(context)
            assertEquals(true, compileResult.isSuccessful)
            assertEquals("", compileResult.stderr)

            val result = dockerJudgeEngine.runTestCase(context, testCase)
            assertEquals(0, result.exitCode)
            assertEquals("5", result.stdout)
            assertEquals("", result.stderr)
        }
    }
}