package com.codeloom.backend.config

import com.codeloom.backend.security.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@Profile("!test")
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { }
            csrf { disable() }

            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                authorize("/error", permitAll)
                authorize("/docs/**", permitAll)
                authorize("/v1/submissions/**", hasAnyRole("USER", "ADMIN"))
                authorize(HttpMethod.GET, "/v1/topics/**", hasAnyRole("USER", "ADMIN"))
                authorize(HttpMethod.GET, "/v1/problems/**", hasAnyRole("USER", "ADMIN"))
                authorize(HttpMethod.POST, "/v1/problems/**", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/v1/problems/**", hasRole("ADMIN"))
                authorize(HttpMethod.PATCH, "/v1/problems/**", hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/v1/problems/**", hasRole("ADMIN"))
                authorize(anyRequest, hasRole("ADMIN"))
            }

            oauth2ResourceServer {
                authenticationEntryPoint = customAuthenticationEntryPoint
                jwt {}
            }

            exceptionHandling {
                authenticationEntryPoint = customAuthenticationEntryPoint
                accessDeniedHandler = customAuthenticationEntryPoint
            }
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf("http://localhost:5173")
                allowedMethods = listOf("*")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        converter.setPrincipalClaimName("preferred_username")
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = authoritiesConverter.convert(jwt) ?: emptyList()
            val realmAccess = jwt.getClaimAsMap("realm_access")

            if (realmAccess?.get("roles") == null) {
                return@setJwtGrantedAuthoritiesConverter emptyList()
            }

            val roles =
                (realmAccess["roles"] as Collection<String>)
                    .filter { role -> role.startsWith("ROLE_") }
                    .map { role -> SimpleGrantedAuthority(role) }
                    .toList()
            return@setJwtGrantedAuthoritiesConverter authorities + roles
        }

        return converter
    }
}
