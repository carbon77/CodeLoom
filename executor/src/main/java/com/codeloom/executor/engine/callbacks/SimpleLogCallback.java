package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleLogCallback extends ResultCallbackTemplate<SimpleLogCallback, Frame> {
    private final StringBuilder out, err;

    public void onNext(Frame frame) {
        String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);

        if (frame.getStreamType() == StreamType.STDERR) {
            err.append(payload);
        } else {
            out.append(payload);
        }
    }
}
