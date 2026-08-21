package com.codeloom.executor.engine;

import static com.codeloom.executor.engine.CodeExecutionConstants.*;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.*;
import java.io.*;
import java.util.List;
import org.apache.commons.compress.archivers.tar.*;
import org.springframework.stereotype.Component;

@Component
public class DockerVolumeFileIO {
  private final DockerClient docker;
  private final DockerImageManager images;

  public DockerVolumeFileIO(DockerClient d, DockerImageManager i) {
    docker = d;
    images = i;
  }

  public void writeFile(String volume, String file, byte[] content) {
    images.pullImageIfAbsent(HELPER_CONTAINER_IMAGE_NAME);
    String id = null;
    try {
      id =
          docker
              .createContainerCmd(HELPER_CONTAINER_IMAGE_NAME)
              .withHostConfig(
                  HostConfig.newHostConfig()
                      .withMounts(
                          List.of(
                              new Mount()
                                  .withType(MountType.VOLUME)
                                  .withSource(volume)
                                  .withTarget(WORKSPACE_DIR))))
              .withWorkingDir(WORKSPACE_DIR)
              .withCmd("sleep", "30")
              .exec()
              .getId();
      docker
          .copyArchiveToContainerCmd(id)
          .withRemotePath(WORKSPACE_DIR)
          .withTarInputStream(new ByteArrayInputStream(createTar(file, content)))
          .exec();
    } finally {
      if (id != null) docker.removeContainerCmd(id).withForce(true).exec();
    }
  }

  private byte[] createTar(String file, byte[] content) {
    try {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      try (TarArchiveOutputStream t = new TarArchiveOutputStream(b)) {
        TarArchiveEntry e = new TarArchiveEntry(file);
        e.setSize(content.length);
        t.putArchiveEntry(e);
        t.write(content);
        t.closeArchiveEntry();
      }
      return b.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
