package com.codeloom.backend.config;

import com.codeloom.backend.security.CustomAuthenticationEntryPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint entry;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(configurer -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> registry.requestMatchers(HttpMethod.OPTIONS, "/**")
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
                .oauth2ResourceServer(
                        configurer -> configurer.authenticationEntryPoint(entry).jwt(jwtConfigurer -> {}))
                .exceptionHandling(
                        configurer -> configurer.authenticationEntryPoint(entry).accessDeniedHandler(entry));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter base = new JwtGrantedAuthoritiesConverter();

        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(base.convert(jwt));
            Map<String, Object> claims = jwt.getClaimAsMap("realm_access");

            if (claims != null && claims.get("roles") instanceof Collection<?> roles)
                roles.stream()
                        .map(Object::toString)
                        .filter(role -> role.startsWith("ROLE_"))
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            return authorities;
        });
        return converter;
    }
}
