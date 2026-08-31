package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoundedLogCallback extends ResultCallbackTemplate<BoundedLogCallback, Frame> {
    private final LimitedBytes stdout;
    private final LimitedBytes stderr;
    private final Runnable overflowAction;
    private final AtomicBoolean overflowHandled = new AtomicBoolean();
    private volatile StreamType exceededStream;

    public BoundedLogCallback(int stdoutLimit, int stderrLimit, Runnable overflowAction) {
        stdout = new LimitedBytes(stdoutLimit);
        stderr = new LimitedBytes(stderrLimit);
        this.overflowAction = overflowAction;
    }

    @Override
    public void onNext(Frame frame) {
        LimitedBytes target = frame.getStreamType() == StreamType.STDERR ? stderr : stdout;
        if (target.append(frame.getPayload()) && overflowHandled.compareAndSet(false, true)) {
            exceededStream = frame.getStreamType() == StreamType.STDERR ? StreamType.STDERR : StreamType.STDOUT;
            overflowAction.run();
        }
    }

    public String stdout() {
        return stdout.utf8();
    }

    public String stderr() {
        return stderr.utf8();
    }

    public boolean exceeded() {
        return exceededStream != null;
    }

    public String exceededName() {
        return exceededStream == StreamType.STDERR ? "stderr" : "stdout";
    }

    private static final class LimitedBytes {
        private final int limit;
        private final ByteArrayOutputStream bytes;
        private int received;

        private LimitedBytes(int limit) {
            this.limit = limit;
            this.bytes = new ByteArrayOutputStream(Math.min(limit, 8192));
        }

        private synchronized boolean append(byte[] payload) {
            int remaining = Math.max(0, limit - bytes.size());
            bytes.write(payload, 0, Math.min(remaining, payload.length));
            received += payload.length;
            return received > limit;
        }

        private synchronized String utf8() {
            try {
                return StandardCharsets.UTF_8
                        .newDecoder()
                        .onMalformedInput(CodingErrorAction.IGNORE)
                        .onUnmappableCharacter(CodingErrorAction.IGNORE)
                        .decode(ByteBuffer.wrap(bytes.toByteArray()))
                        .toString();
            } catch (CharacterCodingException impossible) {
                throw new IllegalStateException(impossible);
            }
        }
    }
}
