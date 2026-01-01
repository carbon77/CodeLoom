package com.codeloom.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.invoke {
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize("/docs/**", permitAll)
                authorize("/v1/problems", hasAnyRole("USER", "ADMIN"))
                authorize(anyRequest, hasRole("ADMIN"))
            }

            oauth2ResourceServer {
                jwt {}
            }
        }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        converter.setPrincipalClaimName("preferred_username")
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = authoritiesConverter.convert(jwt) ?: emptyList()
            val realmAccess = jwt.getClaimAsMap("realm_access")

            if (realmAccess["roles"] == null) {
                return@setJwtGrantedAuthoritiesConverter emptyList()
            }

            val roles = (realmAccess["roles"] as Collection<String>)
                .filter { role -> role.startsWith("ROLE_") }
                .map { role -> SimpleGrantedAuthority(role) }
                .toList()
            return@setJwtGrantedAuthoritiesConverter authorities + roles
        }

        return converter
    }
}