package com.codeloom.backend;

import com.codeloom.common.language.LanguageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableJdbcAuditing
@EnableConfigurationProperties(LanguageProperties.class)
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
