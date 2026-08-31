package com.codeloom.executor.engine.callbacks;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Statistics;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PeakMemoryUsageCallback extends ResultCallbackTemplate<PeakMemoryUsageCallback, Statistics> {
    private final AtomicLong peak = new AtomicLong();
    private final CountDownLatch sampled = new CountDownLatch(1);

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
        sampled.countDown();
    }

    public long peak() {
        return peak.get();
    }

    public boolean awaitSample(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return sampled.await(timeout, timeUnit);
    }
}
