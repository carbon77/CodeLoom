package com.codeloom.executor.service

import com.codeloom.executor.engine.SubmissionState
import com.codeloom.executor.event.SubmissionKafkaEvent
import com.codeloom.executor.model.TestCase
import com.codeloom.executor.repository.TestCaseRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

class SubmissionProcessingServiceTest : DockerTestBase() {

    @MockitoBean
    private val testCaseRepository: TestCaseRepository = mock()

    private lateinit var service: SubmissionProcessingService

    @BeforeEach
    fun setUp() {
        service = spy(SubmissionProcessingService(testCaseRepository, dockerJudgeEngine))

        `when`(testCaseRepository.findByProblemId(1))
            .thenReturn(
                listOf(
                    TestCase(UUID.randomUUID(), 1, "2 3", "5", true),
                    TestCase(UUID.randomUUID(), 1, "123 321", "444", true),
                    TestCase(UUID.randomUUID(), 1, "10 -20", "-10", true),
                    TestCase(UUID.randomUUID(), 1, "-45 -60", "-105", true),
                )
            )
    }

    @Nested
    inner class Java {
        @Test
        fun `correct java program, should submission accepted`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner in = new Scanner(System.in);
                            System.out.print(in.nextInt() + in.nextInt());
                        }
                    }
                """.trimIndent(),
                language = "java",
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                0 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                0 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                1 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }

        @Test
        fun `wrong java program, should wrong answer`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner in = new Scanner(System.in);
                            System.out.print(in.nextInt() - in.nextInt());
                        }
                    }
                """.trimIndent(),
                language = "java",
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                0 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                0 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                1 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                0 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }

        @Test
        fun `wrong java program, should runtime error`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner in = new Scanner(System.in);
                            System.out.print(1 / 0);
                        }
                    }
                """.trimIndent(),
                language = "java",
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                0 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                0 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                0 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }

        @Test
        fun `wrong java program, time limit exceeded`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            while (true) {}
                        }
                    }
                """.trimIndent(),
                language = "java",
                executionTimeLimitMs = 5 * 1000L
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                0 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                1 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                0 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }

        @Test
        fun `wrong java program, should memory limit exceeded`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            int[] x = new int[1000000];
                        }
                    }
                """.trimIndent(),
                language = "java",
                memoryUsageLimitBytes = 10 * 1024 * 1024L,
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                0 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                1 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                0 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                1 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                0 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }

        @Test
        fun `wrong java program, should compile error`() {
            val event = SubmissionKafkaEvent(
                submissionId = submissionId,
                problemId = 1,
                userId = UUID.randomUUID(),
                code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                """.trimIndent(),
                language = "java",
            )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILING) }
                1 * { changeSubmissionState(submissionId, SubmissionState.COMPILE_ERROR) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNNING) }

                0 * { changeSubmissionState(submissionId, SubmissionState.RUNTIME_ERROR) }
                0 * { changeSubmissionState(submissionId, SubmissionState.TIME_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.MEMORY_LIMIT_EXCEEDED) }
                0 * { changeSubmissionState(submissionId, SubmissionState.WRONG_ANSWER) }

                0 * { changeSubmissionState(submissionId, SubmissionState.ACCEPTED) }
            }
        }
    }

}