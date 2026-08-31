package com.codeloom.executor.engine.callbacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.model.MemoryStatsConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StatsConfig;
import org.junit.jupiter.api.Test;

class PeakMemoryUsageCallbackTest {
    @Test
    void tracksPeakUsageWithoutCache() {
        var callback = new PeakMemoryUsageCallback();

        callback.onNext(statistics(1_000L, 200L, null));
        callback.onNext(statistics(900L, 50L, null));

        assertEquals(850L, callback.peak());
    }

    @Test
    void fallsBackToInactiveFileAndNeverReportsNegativeUsage() {
        var callback = new PeakMemoryUsageCallback();

        callback.onNext(statistics(1_000L, null, 300L));
        callback.onNext(statistics(100L, 200L, null));

        assertEquals(700L, callback.peak());
    }

    @Test
    void ignoresStatisticsWithoutMemoryUsage() {
        var callback = new PeakMemoryUsageCallback();

        callback.onNext(new Statistics());

        assertEquals(0L, callback.peak());
    }

    private Statistics statistics(long usage, Long cache, Long inactiveFile) {
        Statistics statistics = mock(Statistics.class);
        MemoryStatsConfig memory = mock(MemoryStatsConfig.class);
        StatsConfig stats = mock(StatsConfig.class);
        when(statistics.getMemoryStats()).thenReturn(memory);
        when(memory.getUsage()).thenReturn(usage);
        when(memory.getStats()).thenReturn(stats);
        when(stats.getCache()).thenReturn(cache);
        when(stats.getInactiveFile()).thenReturn(inactiveFile);
        return statistics;
    }
}
