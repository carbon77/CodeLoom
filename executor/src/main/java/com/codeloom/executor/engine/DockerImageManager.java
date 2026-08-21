package com.codeloom.executor.engine;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerImageManager {
    private final DockerClient docker;

    public void pullImageIfAbsent(String image) {
        pullImageIfAbsent(image, 120);
    }

    public void pullImageIfAbsent(String image, long timeout) {
        try {
            docker.inspectImageCmd(image).exec();
        } catch (NotFoundException e) {
            log.info("Image {} not found. Pulling...", image);
            try {
                docker.pullImageCmd(image).start().awaitCompletion(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(x);
            }
            log.info("Image {} is pulled!", image);
        }
    }
}
