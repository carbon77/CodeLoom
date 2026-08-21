package com.codeloom.backend.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity h) throws Exception {
    h.authorizeHttpRequests(a -> a.anyRequest().authenticated());
    return h.build();
  }
}
