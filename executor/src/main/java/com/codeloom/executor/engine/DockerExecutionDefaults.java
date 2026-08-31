package com.codeloom.executor.engine;

public final class DockerExecutionDefaults {
    public static final int TIMEOUT_MS = 30_000;
    public static final String WORKSPACE_DIR = "/workspace";
    public static final String HELPER_CONTAINER_IMAGE_NAME = "busybox:1.38";

    private DockerExecutionDefaults() {}
}
