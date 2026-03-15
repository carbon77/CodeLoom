package com.codeloom.executor.service

import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.Statistics
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class FirstStatsCallback(
    private val memoryUsageRef: AtomicReference<Long?>,
) : ResultCallbackTemplate<FirstStatsCallback, Statistics>() {
    private val received = CountDownLatch(1)

    override fun onNext(stats: Statistics) {
        val usage = stats.memoryStats.usage
        if (usage != null) {
            memoryUsageRef.compareAndSet(null, usage)
        }
        received.countDown()
        close()
    }

    fun awaitFirst(timeout: Long, unit: TimeUnit) = received.await(timeout, unit)
}