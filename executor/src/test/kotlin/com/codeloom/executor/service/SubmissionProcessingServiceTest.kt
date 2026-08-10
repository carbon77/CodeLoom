package com.codeloom.executor.service

import com.codeloom.common.SubmissionEvent
import com.codeloom.common.SubmissionStatus
import com.codeloom.executor.model.TestCase
import com.codeloom.executor.repository.TestCaseRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

class SubmissionProcessingServiceTest : DockerTestBase() {
    @MockitoBean
    private val testCaseRepository: TestCaseRepository = mock()

    @MockitoBean
    private val eventService: EventService = mock()

    private lateinit var service: SubmissionProcessingService

    @BeforeEach
    fun setUp() {
        service = spy(SubmissionProcessingService(testCaseRepository, dockerJudgeEngine, eventService))

        `when`(testCaseRepository.findByProblemId(1))
            .thenReturn(
                listOf(
                    TestCase(UUID.randomUUID(), 1, "2 3", "5", true),
                    TestCase(UUID.randomUUID(), 1, "123 321", "444", true),
                    TestCase(UUID.randomUUID(), 1, "10 -20", "-10", true),
                    TestCase(UUID.randomUUID(), 1, "-45 -60", "-105", true),
                ),
            )
    }

    @Nested
    inner class Java {
        @Test
        fun `correct java program, should submission accepted`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
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
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }

        @Test
        fun `wrong java program, should wrong answer`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
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
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }

        @Test
        fun `wrong java program, should runtime error`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
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
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }

        @Test
        fun `wrong java program, time limit exceeded`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
                        public class Main {
                            public static void main(String[] args) {
                                while (true) {}
                            }
                        }
                        """.trimIndent(),
                    language = "java",
                    executionTimeLimitMs = 5 * 1000L,
                )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }

        @Test
        fun `wrong java program, should memory limit exceeded`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
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
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }

        @Test
        fun `wrong java program, should compile error`() {
            val event =
                SubmissionEvent(
                    submissionId = submissionId,
                    problemId = 1,
                    userId = UUID.randomUUID(),
                    code =
                        """
                        import java.util.Scanner;
                        public class Main {
                            public static void main(String[] args) {
                        """.trimIndent(),
                    language = "java",
                )
            service.process(event)

            verify(service) {
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILING), anyOrNull()) }
                1 * { changeSubmissionStatus(any(), eq(SubmissionStatus.COMPILE_ERROR), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.RUNTIME_ERROR), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.TIME_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.MEMORY_LIMIT_EXCEEDED), anyOrNull()) }
                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.WRONG_ANSWER), anyOrNull()) }

                0 * { changeSubmissionStatus(any(), eq(SubmissionStatus.ACCEPTED), anyOrNull()) }
            }
        }
    }
}
