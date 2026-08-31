package com.codeloom.executor.engine;

import static com.codeloom.executor.engine.DockerExecutionDefaults.HELPER_CONTAINER_IMAGE_NAME;
import static com.codeloom.executor.engine.DockerExecutionDefaults.WORKSPACE_DIR;

import com.github.dockerjava.api.DockerClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DockerVolumeFileIO {
    private final DockerClient docker;
    private final DockerImageManager images;
    private final DockerContainerPolicy policy;

    public void writeFile(String volume, String file, byte[] content) {
        images.pullImageIfAbsent(HELPER_CONTAINER_IMAGE_NAME);
        String id = null;
        try {
            id = docker.createContainerCmd(HELPER_CONTAINER_IMAGE_NAME)
                    .withHostConfig(policy.helper(volume))
                    .withWorkingDir(WORKSPACE_DIR)
                    .withCmd("sleep", "30")
                    .exec()
                    .getId();
            docker.copyArchiveToContainerCmd(id)
                    .withRemotePath(WORKSPACE_DIR)
                    .withTarInputStream(createTar(file, content))
                    .exec();
        } finally {
            if (id != null) docker.removeContainerCmd(id).withForce(true).exec();
        }
    }

    private ByteArrayInputStream createTar(String file, byte[] content) {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            try (var tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {
                var entry = new TarArchiveEntry(file);
                entry.setSize(content.length);

                tarArchiveOutputStream.putArchiveEntry(entry);
                tarArchiveOutputStream.write(content);
                tarArchiveOutputStream.closeArchiveEntry();
            }
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
