package com.codeloom.executor.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.executor.model.TestCase;
import com.codeloom.executor.repository.TestCaseRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SubmissionProcessingServiceTest extends DockerTestBase {
    @MockitoBean
    private TestCaseRepository testCaseRepository;

    @MockitoBean
    private EventService eventService;

    private SubmissionProcessingService service;

    @BeforeEach
    void setUp() {
        service = spy(new SubmissionProcessingService(testCaseRepository, dockerJudgeEngine, eventService));
        when(testCaseRepository.findByProblemId(1))
                .thenReturn(List.of(
                        new TestCase(UUID.randomUUID(), 1, "2 3", "5", true),
                        new TestCase(UUID.randomUUID(), 1, "123 321", "444", true),
                        new TestCase(UUID.randomUUID(), 1, "10 -20", "-10", true),
                        new TestCase(UUID.randomUUID(), 1, "-45 -60", "-105", true)));
    }

    private SubmissionEvent event(String code, Long time, Long memory) {
        return SubmissionEvent.builder()
                .submissionId(submissionId)
                .userId(UUID.randomUUID())
                .problemId(1)
                .code(code)
                .language("java")
                .executionTimeLimitMs(time)
                .memoryUsageLimitBytes(memory)
                .build();
    }

    private void occurred(SubmissionStatus status) {
        verify(service).changeSubmissionStatus(any(), eq(status), nullable(SubmissionStatusPayload.class));
    }

    @Nested
    class JavaPrograms {
        @Test
        void correctProgramIsAccepted() {
            service.process(event(
                    "import java.util.*; public class Main{public static void main(String[]x){Scanner s=new Scanner(System.in);System.out.print(s.nextInt()+s.nextInt());}}",
                    null,
                    null));
            occurred(SubmissionStatus.COMPILING);
            occurred(SubmissionStatus.RUNNING);
            occurred(SubmissionStatus.ACCEPTED);
        }

        @Test
        void wrongProgramIsWrongAnswer() {
            service.process(event(
                    "import java.util.*; public class Main{public static void main(String[]x){Scanner s=new Scanner(System.in);System.out.print(s.nextInt()-s.nextInt());}}",
                    null,
                    null));
            occurred(SubmissionStatus.WRONG_ANSWER);
        }

        @Test
        void runtimeFailureIsReported() {
            service.process(
                    event("public class Main{public static void main(String[]x){System.out.print(1/0);}}", null, null));
            occurred(SubmissionStatus.RUNTIME_ERROR);
        }

        @Test
        void timeoutIsReported() {
            service.process(event("public class Main{public static void main(String[]x){while(true){}}}", 5000L, null));
            occurred(SubmissionStatus.TIME_LIMIT_EXCEEDED);
        }

        @Test
        void memoryLimitIsReported() {
            service.process(event(
                    "public class Main{public static void main(String[]x){int[] a=new int[1000000];}}",
                    null,
                    10L * 1024 * 1024));
            occurred(SubmissionStatus.MEMORY_LIMIT_EXCEEDED);
        }

        @Test
        void compilerFailureIsReported() {
            service.process(event("public class Main { broken", null, null));
            occurred(SubmissionStatus.COMPILE_ERROR);
            verify(service, never()).changeSubmissionStatus(any(), eq(SubmissionStatus.RUNNING), any());
        }
    }
}
