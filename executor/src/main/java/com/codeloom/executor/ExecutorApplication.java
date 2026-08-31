package com.codeloom.executor;

import com.codeloom.executor.config.DockerConstraints;
import com.codeloom.executor.config.ExecutorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ExecutorProperties.class, DockerConstraints.class})
public class ExecutorApplication {
    public static void main(String[] a) {
        SpringApplication.run(ExecutorApplication.class, a);
    }
}
