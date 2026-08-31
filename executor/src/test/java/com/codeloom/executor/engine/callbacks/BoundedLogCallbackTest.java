package com.codeloom.executor.engine.callbacks;

import static org.junit.jupiter.api.Assertions.*;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BoundedLogCallbackTest {
    @Test
    void capturesAtLimitWithoutKilling() {
        AtomicInteger kills = new AtomicInteger();
        var callback = new BoundedLogCallback(4, 4, kills::incrementAndGet);
        callback.onNext(frame(StreamType.STDOUT, "ab"));
        callback.onNext(frame(StreamType.STDOUT, "cd"));
        assertEquals("abcd", callback.stdout());
        assertFalse(callback.exceeded());
        assertEquals(0, kills.get());
    }

    @Test
    void accountsForStreamsIndependentlyAndKillsOnce() {
        AtomicInteger kills = new AtomicInteger();
        var callback = new BoundedLogCallback(3, 2, kills::incrementAndGet);
        callback.onNext(frame(StreamType.STDOUT, "abc"));
        callback.onNext(frame(StreamType.STDERR, "xy"));
        callback.onNext(frame(StreamType.STDOUT, "d"));
        callback.onNext(frame(StreamType.STDERR, "z"));
        assertEquals("abc", callback.stdout());
        assertEquals("xy", callback.stderr());
        assertEquals("stdout", callback.exceededName());
        assertEquals(1, kills.get());
    }

    @Test
    void dropsIncompleteUtf8Suffix() {
        AtomicInteger kills = new AtomicInteger();
        var callback = new BoundedLogCallback(4, 4, kills::incrementAndGet);
        callback.onNext(new Frame(StreamType.STDOUT, "a😀".getBytes(StandardCharsets.UTF_8)));
        assertEquals("a", callback.stdout());
        assertTrue(callback.exceeded());
        assertEquals(1, kills.get());
    }

    private Frame frame(StreamType type, String value) {
        return new Frame(type, value.getBytes(StandardCharsets.UTF_8));
    }
}
