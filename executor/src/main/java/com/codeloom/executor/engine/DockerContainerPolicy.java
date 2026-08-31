package com.codeloom.executor.engine;

import static com.codeloom.executor.config.DockerConstraints.*;
import static com.codeloom.executor.engine.DockerExecutionDefaults.WORKSPACE_DIR;

import com.codeloom.executor.config.DockerConstraints;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;
import com.github.dockerjava.api.model.Volume;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@NullMarked
public class DockerContainerPolicy {
    private final DockerConstraints constraints;

    public HostConfig judge(String volume, long memoryBytes, boolean compilation) {
        long nanoCpus = compilation ? COMPILATION_NANO_CPUS : EXECUTION_NANO_CPUS;
        long pids = compilation ? COMPILATION_PIDS : EXECUTION_PIDS;
        return base(memoryBytes, nanoCpus, pids)
                .withBinds(new Bind(volume, new Volume(WORKSPACE_DIR)))
                .withReadonlyRootfs(READ_ONLY_ROOT_FILESYSTEM)
                .withTmpFs(Map.of(TMPFS_PATH, TMPFS_OPTIONS + constraints.tmpfsLimitBytes()));
    }

    public HostConfig helper(String volume) {
        return base(HELPER_MEMORY_BYTES, HELPER_NANO_CPUS, HELPER_PIDS)
                .withBinds(new Bind(volume, new Volume(WORKSPACE_DIR)))
                .withReadonlyRootfs(READ_ONLY_ROOT_FILESYSTEM)
                .withTmpFs(Map.of(TMPFS_PATH, TMPFS_OPTIONS + constraints.tmpfsLimitBytes()));
    }

    private HostConfig base(long memoryBytes, long nanoCpus, long pids) {
        return HostConfig.newHostConfig()
                .withMemory(memoryBytes)
                .withMemorySwap(memoryBytes)
                .withNanoCPUs(nanoCpus)
                .withPidsLimit(pids)
                .withNetworkMode(NETWORK_MODE)
                .withPrivileged(PRIVILEGED)
                .withCapDrop(Capability.ALL)
                .withSecurityOpts(List.of(SECURITY_OPTION))
                .withInit(INIT_ENABLED)
                .withShmSize(SHARED_MEMORY_BYTES)
                .withUlimits(List.of(
                        new Ulimit(OPEN_FILES_ULIMIT, constraints.maxOpenFiles(), constraints.maxOpenFiles()),
                        new Ulimit(FILE_SIZE_ULIMIT, constraints.maxFileSizeBytes(), constraints.maxFileSizeBytes()),
                        new Ulimit(CORE_DUMP_ULIMIT, CORE_DUMP_BYTES, CORE_DUMP_BYTES)));
    }
}
