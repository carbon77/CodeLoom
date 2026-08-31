package com.codeloom.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("codeloom.executor")
public record DockerConstraints(long tmpfsLimitBytes, long maxOpenFiles, long maxFileSizeBytes) {
    public static final long COMPILATION_NANO_CPUS = 2_000_000_000L;
    public static final long EXECUTION_NANO_CPUS = 1_000_000_000L;

    public static final long HELPER_NANO_CPUS = 500_000_000L;
    public static final long COMPILATION_PIDS = 128L;

    public static final long EXECUTION_PIDS = 64L;
    public static final long HELPER_PIDS = 32L;

    public static final long DEFAULT_MEMORY_BYTES = 256L * 1024 * 1024;
    public static final long HELPER_MEMORY_BYTES = 64L * 1024 * 1024;
    public static final long SHARED_MEMORY_BYTES = 16L * 1024 * 1024;

    public static final long CORE_DUMP_BYTES = 0L;
    public static final boolean PRIVILEGED = false;
    public static final boolean READ_ONLY_ROOT_FILESYSTEM = true;
    public static final boolean INIT_ENABLED = true;
    public static final String NETWORK_MODE = "none";
    public static final String SECURITY_OPTION = "no-new-privileges";
    public static final String TMPFS_PATH = "/tmp";
    public static final String TMPFS_OPTIONS = "rw,noexec,nosuid,size=";
    public static final String OPEN_FILES_ULIMIT = "nofile";
    public static final String FILE_SIZE_ULIMIT = "fsize";
    public static final String CORE_DUMP_ULIMIT = "core";
}
