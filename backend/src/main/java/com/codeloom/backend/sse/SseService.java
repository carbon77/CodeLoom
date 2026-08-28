package com.codeloom.backend.sse;

import com.codeloom.backend.dto.SubmissionStatusDto;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

    private final Long SSE_EMITTER_TIMEOUT = 30 * 60_000L; // 30 minutes
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createConnection(UUID userId) {
        log.info("Establishing sse connection: userId={}", userId);

        SseEmitter emitter = createEmitter(userId);

        log.info("SSE connection established: userId={}", userId);
        return emitter;
    }

    public void sendSubmissionStatusEvent(UUID userId, SubmissionStatusDto dto) {
        log.info("Sending SSE event: userId={}", userId);

        var userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            log.info("There are no SSE connections: userId={}", userId);
            return;
        }

        var event = SseEmitter.event().data(dto, MediaType.APPLICATION_JSON).name("submission-status");

        for (var entry : userEmitters.entrySet()) {
            SseEmitter emitter = entry.getValue();

            try {
                emitter.send(event);
            } catch (Exception e) {
                removeEmitter(userId, entry.getKey());
                emitter.completeWithError(e);
                log.error("SSE connection failed: userId={}, e={}", userId, e.getMessage());
            }
        }
        log.info("SSE event sent: userId={}", userId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionStatusCommitted(SubmissionStatusCommittedEvent event) {
        sendSubmissionStatusEvent(
                event.getUserId(),
                SubmissionStatusDto.builder()
                        .submissionId(event.getSubmissionId())
                        .status(event.getStatus())
                        .build());
    }

    private void removeEmitter(UUID userId, UUID emitterId) {
        var userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitterId);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    private SseEmitter createEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
        UUID emitterId = UUID.randomUUID();

        emitter.onCompletion(() -> {
            log.info("SSE connection with user id={} completed", userId);
            removeEmitter(userId, emitterId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection with user id={} timed out", userId);
            removeEmitter(userId, emitterId);
        });

        emitters.putIfAbsent(userId, new ConcurrentHashMap<>());
        var userEmitters = emitters.get(userId);

        userEmitters.put(emitterId, emitter);
        return emitter;
    }
}
