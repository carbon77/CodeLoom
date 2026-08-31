package com.codeloom.executor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

class DockerConstraintsConfigurationTest {
    @Test
    void bindsDocumentedDefaultsFromApplicationConfiguration() throws IOException {
        var environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        var resource = new ClassPathResource("application.yaml");
        new YamlPropertySourceLoader().load("application", resource).forEach(environment.getPropertySources()::addLast);

        var constraints = Binder.get(environment)
                .bind("codeloom.executor", DockerConstraints.class)
                .orElseThrow(() -> new IllegalStateException("Docker constraints were not bound"));

        assertEquals(67_108_864L, constraints.tmpfsLimitBytes());
        assertEquals(1024L, constraints.maxOpenFiles());
        assertEquals(67_108_864L, constraints.maxFileSizeBytes());
    }
}
