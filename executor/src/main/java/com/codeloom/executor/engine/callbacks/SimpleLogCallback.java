package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.*;
import java.nio.charset.StandardCharsets;

public class SimpleLogCallback extends ResultCallbackTemplate<SimpleLogCallback, Frame> {
  private final StringBuilder out, err;

  public SimpleLogCallback(StringBuilder o, StringBuilder e) {
    out = o;
    err = e;
  }

  public void onNext(Frame f) {
    String m = new String(f.getPayload(), StandardCharsets.UTF_8);
    (f.getStreamType() == StreamType.STDERR ? err : out).append(m);
  }
}
