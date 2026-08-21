package com.codeloom.executor.engine;

public final class CodeExecutionConstants {
  private CodeExecutionConstants() {}

  public static final long ERROR_EXIT_CODE = 1,
      TIMEOUT_EXIT_CODE = 124,
      DEFAULT_TIMEOUT_MS = 30000,
      MEMORY_LIMIT_EXCEEDED_EXIT_CODE = 137,
      DEFAULT_MEMORY_LIMIT_BYTES = 256L * 1024 * 1024;
  public static final String TIMEOUT_MESSAGE = "Execution timed out",
      MEMORY_LIMIT_EXCEEDED_MESSAGE = "Memory limit exceeded",
      WORKSPACE_DIR = "/workspace",
      HELPER_CONTAINER_IMAGE_NAME = "busybox:1.38";
}
