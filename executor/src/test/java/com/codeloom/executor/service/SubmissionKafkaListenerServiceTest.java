package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codeloom.common.SubmissionKafkaEvent;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.KafkaListener;
import tools.jackson.databind.ObjectMapper;

class SubmissionKafkaListenerServiceTest {
    @Test
    void listenerUsesConfiguredTopicAndGroup() throws Exception {
        Method method = SubmissionKafkaListenerService.class.getMethod("listenSubmission", String.class);
        KafkaListener annotation = method.getAnnotation(KafkaListener.class);
        assertArrayEquals(new String[] {"${codeloom.kafka.topics.submission}"}, annotation.topics());
        assertEquals("${spring.kafka.consumer.group-id}", annotation.groupId());
    }

    @Test
    void successfulWorkIsProcessedSynchronously() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        SubmissionJudge judge = mock(SubmissionJudge.class);
        SubmissionKafkaEvent event = SubmissionKafkaEvent.builder()
                .submissionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .problemId(1)
                .build();
        when(mapper.readValue("event", SubmissionKafkaEvent.class)).thenReturn(event);
        var listener = new SubmissionKafkaListenerService(mapper, judge);
        listener.listenSubmission("event");
        verify(judge).judge(event);
    }

    @Test
    void malformedWorkIsAcknowledged() {
        SubmissionJudge judge = mock(SubmissionJudge.class);
        var listener = new SubmissionKafkaListenerService(new ObjectMapper(), judge);
        assertDoesNotThrow(() -> listener.listenSubmission("["));
        verifyNoInteractions(judge);
    }

    @Test
    void processingFailurePropagates() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        SubmissionKafkaEvent event = SubmissionKafkaEvent.builder().problemId(1).build();
        when(mapper.readValue("event", SubmissionKafkaEvent.class)).thenReturn(event);
        SubmissionJudge judge = mock(SubmissionJudge.class);
        IllegalStateException failure = new IllegalStateException("failed");
        doThrow(failure).when(judge).judge(event);
        var listener = new SubmissionKafkaListenerService(mapper, judge);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> listener.listenSubmission("event")));
    }
}
