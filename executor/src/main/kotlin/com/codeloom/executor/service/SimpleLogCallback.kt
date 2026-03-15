package com.codeloom.executor.service

import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.StreamType

class SimpleLogCallback(
    private val stdout: StringBuilder,
    private val stderr: StringBuilder,
) : ResultCallbackTemplate<SimpleLogCallback, Frame>() {
    override fun onNext(frame: Frame) {
        val message = String(frame.payload, Charsets.UTF_8)
        if (frame.streamType == StreamType.STDERR) {
            stderr.append(message)
        } else {
            stdout.append(message)
        }
    }
}