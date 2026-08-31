package com.codeloom.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("codeloom.executor")
public record ExecutorProperties(int stdoutLimitBytes, int stderrLimitBytes) {}
