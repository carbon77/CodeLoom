package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Statistics;
import java.util.concurrent.atomic.AtomicLong;

public class PeakMemoryUsageCallback extends ResultCallbackTemplate<PeakMemoryUsageCallback, Statistics> {
    private final AtomicLong peak = new AtomicLong();

    @Override
    public void onNext(Statistics s) {
        if (s.getMemoryStats() == null || s.getMemoryStats().getUsage() == null) return;
        Long cache = s.getMemoryStats().getStats() == null
                ? null
                : s.getMemoryStats().getStats().getCache();
        if (cache == null && s.getMemoryStats().getStats() != null)
            cache = s.getMemoryStats().getStats().getInactiveFile();
        long use = Math.max(0, s.getMemoryStats().getUsage() - (cache == null ? 0 : cache));
        peak.accumulateAndGet(use, Math::max);
    }

    public long peak() {
        return peak.get();
    }
}
