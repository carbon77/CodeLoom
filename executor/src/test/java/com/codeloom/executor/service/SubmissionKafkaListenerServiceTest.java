package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codeloom.common.SubmissionEvent;
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
        SubmissionProcessingService processing = mock(SubmissionProcessingService.class);
        SubmissionEvent event = SubmissionEvent.builder()
                .submissionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .problemId(1)
                .build();
        when(mapper.readValue("event", SubmissionEvent.class)).thenReturn(event);
        var listener = new SubmissionKafkaListenerService(mapper, processing);
        listener.listenSubmission("event");
        verify(processing).process(event);
    }

    @Test
    void malformedWorkIsAcknowledged() {
        SubmissionProcessingService processing = mock(SubmissionProcessingService.class);
        var listener = new SubmissionKafkaListenerService(new ObjectMapper(), processing);
        assertDoesNotThrow(() -> listener.listenSubmission("["));
        verifyNoInteractions(processing);
    }

    @Test
    void processingFailurePropagates() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        SubmissionEvent event = SubmissionEvent.builder().problemId(1).build();
        when(mapper.readValue("event", SubmissionEvent.class)).thenReturn(event);
        SubmissionProcessingService processing = mock(SubmissionProcessingService.class);
        IllegalStateException failure = new IllegalStateException("failed");
        doThrow(failure).when(processing).process(event);
        var listener = new SubmissionKafkaListenerService(mapper, processing);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> listener.listenSubmission("event")));
    }
}
