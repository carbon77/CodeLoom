package com.codeloom.executor.service.executor.callbacks

import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.Statistics
import java.util.concurrent.atomic.AtomicLong

class PeakMemoryUsageCallback : ResultCallbackTemplate<PeakMemoryUsageCallback, Statistics>() {
    private val peakUsage = AtomicLong(0)

    override fun onNext(stats: Statistics) {
        val usage = stats.memoryStats?.usage ?: return
        val cache = (stats.memoryStats.stats?.cache as? Number)?.toLong()
            ?: (stats.memoryStats.stats?.inactiveFile as? Number)?.toLong()
            ?: 0L
        val effectiveUsage = (usage - cache).coerceAtLeast(0)
        peakUsage.updateAndGet { current -> maxOf(current, effectiveUsage) }
    }

    fun peak(): Long = peakUsage.get()
}