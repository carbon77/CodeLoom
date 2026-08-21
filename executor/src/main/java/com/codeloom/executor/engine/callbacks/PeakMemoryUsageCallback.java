package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Statistics;
import java.util.concurrent.atomic.AtomicLong;

public class PeakMemoryUsageCallback extends ResultCallbackTemplate<PeakMemoryUsageCallback, Statistics> {
    private final AtomicLong peak = new AtomicLong();

    @Override
    public void onNext(Statistics stats) {
        if (stats.getMemoryStats() == null || stats.getMemoryStats().getUsage() == null) {
            return;
        }

        Long cache = stats.getMemoryStats().getStats() == null
                ? null
                : stats.getMemoryStats().getStats().getCache();
        if (cache == null && stats.getMemoryStats().getStats() != null) {
            cache = stats.getMemoryStats().getStats().getInactiveFile();
        }

        long use = Math.max(0, stats.getMemoryStats().getUsage() - (cache == null ? 0 : cache));
        peak.accumulateAndGet(use, Math::max);
    }

    public long peak() {
        return peak.get();
    }
}
