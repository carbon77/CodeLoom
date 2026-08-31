package com.codeloom.executor.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.codeloom.executor.config.DockerConstraints;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DockerContainerPolicyTest {
    private final DockerContainerPolicy policy =
            new DockerContainerPolicy(new DockerConstraints(67_108_864, 1024, 67_108_864));

    @Test
    void compilationPolicyUsesBalancedLimits() {
        var host = policy.judge("submission-test", 1234, true);
        assertEquals(1234, host.getMemory());
        assertEquals(1234, host.getMemorySwap());
        assertEquals(2_000_000_000L, host.getNanoCPUs());
        assertEquals(128, host.getPidsLimit());
        assertEquals(16L * 1024 * 1024, host.getShmSize());
        assertSandboxed(host);
    }

    @Test
    void executionAndHelperPoliciesAreProportionate() {
        var run = policy.judge("submission-test", 5678, false);
        assertEquals(1_000_000_000L, run.getNanoCPUs());
        assertEquals(64, run.getPidsLimit());
        assertEquals(5678, run.getMemory());
        assertEquals(5678, run.getMemorySwap());
        assertSandboxed(run);

        var helper = policy.helper("submission-test");
        assertEquals(500_000_000L, helper.getNanoCPUs());
        assertEquals(32, helper.getPidsLimit());
        assertEquals(64L * 1024 * 1024, helper.getMemory());
        assertEquals(64L * 1024 * 1024, helper.getMemorySwap());
        assertSandboxed(helper);
    }

    private void assertSandboxed(HostConfig host) {
        assertEquals("none", host.getNetworkMode());
        assertFalse(host.getPrivileged());
        assertTrue(host.getReadonlyRootfs());
        assertTrue(host.getInit());
        assertArrayEquals(new Capability[] {Capability.ALL}, host.getCapDrop());
        assertTrue(host.getSecurityOpts().contains("no-new-privileges"));
        assertEquals("rw,noexec,nosuid,size=67108864", host.getTmpFs().get("/tmp"));
        var ulimits = Arrays.stream(host.getUlimits()).collect(Collectors.toMap(u -> u.getName(), Function.identity()));
        assertEquals(3, ulimits.size());
        assertEquals(1024L, ulimits.get("nofile").getSoftLong());
        assertEquals(1024L, ulimits.get("nofile").getHardLong());
        assertEquals(67_108_864L, ulimits.get("fsize").getSoftLong());
        assertEquals(67_108_864L, ulimits.get("fsize").getHardLong());
        assertEquals(0L, ulimits.get("core").getSoftLong());
        assertEquals(0L, ulimits.get("core").getHardLong());
    }
}
