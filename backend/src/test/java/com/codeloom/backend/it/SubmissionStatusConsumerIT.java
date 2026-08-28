package com.codeloom.backend.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.submission.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.security.UserRole;
import com.codeloom.common.SubmissionStatus;
import com.codeloom.common.event.SubmissionStatusChangedEvent;
import com.codeloom.common.event.SubmissionStatusPayload;
import com.codeloom.common.event.TestCaseResultDto;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Testcontainers
class SubmissionStatusConsumerIT extends BackendIntegrationTestSupport {
    @Container
    @ServiceConnection
    static ConfluentKafkaContainer kafka =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.7"));

    @Autowired
    ProblemRepository problemRepository;

    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    TestCaseResultRepository testCaseResultRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Value("${codeloom.kafka.submission-status-topic}")
    String topic;

    long problemId;
    UUID submissionId;
    TestCase publicTestCase;

    @BeforeEach
    void setUp() {
        testCaseResultRepository.deleteAll();
        submissionRepository.deleteAll();
        testCaseRepository.deleteAll();
        problemRepository.deleteAll();

        var problem = problemRepository.save(Problem.builder()
                .title("Two Sum")
                .slug("two_sum_" + UUID.randomUUID())
                .build());
        problemId = problem.getId();
        publicTestCase = testCaseRepository.save(TestCase.builder()
                .problemId(problemId)
                .input("1")
                .expectedOutput("1")
                .isPublic(true)
                .build());
        testCaseRepository.save(TestCase.builder()
                .problemId(problemId)
                .input("2")
                .expectedOutput("2")
                .isPublic(false)
                .build());
        submissionId = submissionRepository
                .save(Submission.builder()
                        .userId(TEST_USER_ID)
                        .problemId(problemId)
                        .code("print(input())")
                        .status(SubmissionStatus.PENDING)
                        .language("python")
                        .build())
                .getId();
    }

    @Test
    void updatesStatusAndPersistsResults() throws Exception {
        kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get(20, TimeUnit.SECONDS);
        awaitStatus(SubmissionStatus.ACCEPTED);
        awaitResultCount(1);

        var result = testCaseResultRepository.findAll().iterator().next();
        assertThat(result.getId()).isNotNull();
        assertNotEquals(publicTestCase.getId(), result.getId());
        assertEquals(submissionId, result.getSubmissionId());
        assertEquals("1", result.getInput());
        assertEquals("1", result.getExpectedOutput());
        assertEquals("1", result.getStdout());
        assertEquals("", result.getStderr());
        assertEquals(12L, result.getExecutionTimeMs());
        assertEquals(1024L, result.getBytesUsed());
    }

    @Test
    void persistsIndependentResultsForTheSamePublicTestCaseAcrossSubmissions() throws Exception {
        var secondSubmissionId = submissionRepository
                .save(Submission.builder()
                        .userId(TEST_USER_ID)
                        .problemId(problemId)
                        .code("print(input())")
                        .status(SubmissionStatus.PENDING)
                        .language("python")
                        .build())
                .getId();

        kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get(20, TimeUnit.SECONDS);
        kafkaTemplate
                .send(topic, secondSubmissionId.toString(), acceptedEvent(secondSubmissionId))
                .get(20, TimeUnit.SECONDS);

        awaitResultCount(submissionId, 1);
        awaitResultCount(secondSubmissionId, 1);
        var results = StreamSupport.stream(testCaseResultRepository.findAll().spliterator(), false)
                .toList();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(result -> result.getId()).doesNotHaveDuplicates();
        assertThat(results).allMatch(result -> !publicTestCase.getId().equals(result.getId()));
    }

    @Test
    void sendsCommittedStatusAndResultsToAllOwnersSseConnectionsOnly() throws Exception {
        MvcResult firstOwnerConnection = openSseConnection(TEST_USER_ID);
        MvcResult secondOwnerConnection = openSseConnection(TEST_USER_ID);
        MvcResult otherUserConnection = openSseConnection(UUID.randomUUID());
        try {
            kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get(20, TimeUnit.SECONDS);

            awaitSseStatus(firstOwnerConnection, SubmissionStatus.ACCEPTED);
            awaitSseStatus(secondOwnerConnection, SubmissionStatus.ACCEPTED);
            awaitResultCount(1);
            assertEquals(
                    SubmissionStatus.ACCEPTED,
                    submissionRepository.findById(submissionId).orElseThrow().getStatus());
            assertThat(otherUserConnection.getResponse().getContentAsString()).isEmpty();
        } finally {
            complete(firstOwnerConnection);
            complete(secondOwnerConnection);
            complete(otherUserConnection);
        }
    }

    @Test
    void absentPayloadDoesNotPersistResults() throws Exception {
        kafkaTemplate
                .send(topic, submissionId.toString(), statusEvent(SubmissionStatus.COMPILING))
                .get(20, TimeUnit.SECONDS);
        awaitStatus(SubmissionStatus.COMPILING);
        Thread.sleep(1000);
        assertEquals(0, testCaseResultRepository.count());
    }

    @Test
    void persistsSubmissionError() throws Exception {
        var event = SubmissionStatusChangedEvent.builder()
                .submissionId(submissionId)
                .problemId(problemId)
                .userId(UUID.randomUUID())
                .newStatus(SubmissionStatus.COMPILE_ERROR)
                .payload(SubmissionStatusPayload.builder()
                        .error("compiler failed")
                        .build())
                .build();

        kafkaTemplate
                .send(topic, submissionId.toString(), objectMapper.writeValueAsString(event))
                .get(20, TimeUnit.SECONDS);
        awaitStatus(SubmissionStatus.COMPILE_ERROR);

        assertEquals(
                "compiler failed",
                submissionRepository.findById(submissionId).orElseThrow().getErrorMessage());
    }

    @Test
    void laterEventReplacesPersistedResults() throws Exception {
        kafkaTemplate.send(topic, submissionId.toString(), wrongAnswerEvent()).get(20, TimeUnit.SECONDS);
        awaitStatus(SubmissionStatus.WRONG_ANSWER);
        awaitResultCount(1);

        kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get(20, TimeUnit.SECONDS);
        awaitStatus(SubmissionStatus.ACCEPTED);
        awaitResultCount(1);
        assertEquals("1", testCaseResultRepository.findAll().iterator().next().getStdout());
    }

    @Test
    void ignoresUnknownSubmission() throws Exception {
        MvcResult connection = openSseConnection(TEST_USER_ID);
        var unknownId = UUID.randomUUID();
        var event =
                SubmissionStatusChangedEvent.builder().submissionId(unknownId).build();
        var json = objectMapper.writeValueAsString(event);
        try {
            kafkaTemplate.send(topic, unknownId.toString(), json).get(20, TimeUnit.SECONDS);
            Thread.sleep(2000);
            assertEquals(0, testCaseResultRepository.count());
            assertEquals(
                    SubmissionStatus.PENDING,
                    submissionRepository.findById(submissionId).orElseThrow().getStatus());
            assertThat(connection.getResponse().getContentAsString()).isEmpty();
        } finally {
            complete(connection);
        }
    }

    private String event(SubmissionStatus status, String stdout, String stderr) {
        return resultEvent(
                status,
                List.of(TestCaseResultDto.builder()
                        .id(publicTestCase.getId())
                        .problemId(problemId)
                        .input("1")
                        .expectedOutput("1")
                        .stdout(stdout)
                        .stderr(stderr)
                        .executionTimeMs(12)
                        .memoryUsageBytes(1024)
                        .build()));
    }

    private String resultEvent(SubmissionStatus status, List<TestCaseResultDto> testCaseResults) {
        SubmissionStatusChangedEvent event = SubmissionStatusChangedEvent.builder()
                .submissionId(submissionId)
                .problemId(problemId)
                .userId(UUID.randomUUID())
                .newStatus(status)
                .payload(SubmissionStatusPayload.builder()
                        .testCaseResults(testCaseResults)
                        .build())
                .build();
        return objectMapper.writeValueAsString(event);
    }

    private String statusEvent(SubmissionStatus status) {
        return resultEvent(status, List.of());
    }

    private String acceptedEvent() {
        return acceptedEvent(submissionId);
    }

    private String acceptedEvent(UUID eventSubmissionId) {
        SubmissionStatusChangedEvent event = SubmissionStatusChangedEvent.builder()
                .submissionId(eventSubmissionId)
                .problemId(problemId)
                .userId(TEST_USER_ID)
                .newStatus(SubmissionStatus.ACCEPTED)
                .payload(SubmissionStatusPayload.builder()
                        .testCaseResults(List.of(TestCaseResultDto.builder()
                                .id(publicTestCase.getId())
                                .problemId(problemId)
                                .input("1")
                                .expectedOutput("1")
                                .stdout("1")
                                .stderr("")
                                .executionTimeMs(12)
                                .memoryUsageBytes(1024)
                                .build()))
                        .build())
                .build();
        return objectMapper.writeValueAsString(event);
    }

    private String wrongAnswerEvent() {
        return event(SubmissionStatus.WRONG_ANSWER, "0", "wrong");
    }

    private void awaitStatus(SubmissionStatus expected) {
        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> submissionRepository
                                .findById(submissionId)
                                .orElseThrow()
                                .getStatus()
                        == expected);
    }

    private void awaitResultCount(int expected) {
        awaitResultCount(submissionId, expected);
    }

    private void awaitResultCount(UUID expectedSubmissionId, int expected) {
        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var results = testCaseResultRepository.findAll();
                    long count = StreamSupport.stream(results.spliterator(), false)
                            .filter(result -> result.getSubmissionId().equals(expectedSubmissionId))
                            .count();
                    assertThat(count).isEqualTo(expected);
                });
    }

    private MvcResult openSseConnection(UUID userId) throws Exception {
        return mockMvc.perform(get("/v1/submissions/sse").principal(user(userId, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    private void awaitSseStatus(MvcResult connection, SubmissionStatus status) {
        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var body = connection.getResponse().getContentAsString();
                    assertThat(body).contains("event:submission-status");
                    assertThat(body).contains("\"submissionId\":\"" + submissionId + "\"");
                    assertThat(body).contains("\"status\":\"" + status + "\"");
                });
    }

    private void complete(MvcResult connection) {
        connection.getRequest().getAsyncContext().complete();
    }
}
