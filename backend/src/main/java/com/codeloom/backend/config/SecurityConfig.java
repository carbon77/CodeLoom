package com.codeloom.backend.config;

import com.codeloom.backend.security.CustomAuthenticationEntryPoint;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint entry;

    public SecurityConfig(CustomAuthenticationEntryPoint e) {
        entry = e;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity h) throws Exception {
        h.cors(c -> {})
                .csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers("/error", "/docs/**")
                        .permitAll()
                        .requestMatchers("/v1/submissions/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/topics/**", "/v1/problems/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/problems/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/problems/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/problems/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/problems/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .hasRole("ADMIN"))
                .oauth2ResourceServer(o -> o.authenticationEntryPoint(entry).jwt(j -> {}))
                .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(entry));
        return h.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173"));
        c.setAllowedMethods(List.of("*"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter base = new JwtGrantedAuthoritiesConverter();
        c.setPrincipalClaimName("preferred_username");
        c.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<org.springframework.security.core.GrantedAuthority> a = new ArrayList<>(base.convert(jwt));
            Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
            if (realm != null && realm.get("roles") instanceof Collection<?> roles)
                roles.stream()
                        .map(Object::toString)
                        .filter(x -> x.startsWith("ROLE_"))
                        .map(SimpleGrantedAuthority::new)
                        .forEach(a::add);
            return a;
        });
        return c;
    }
}
