package com.codeloom.backend.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.web.servlet.HandlerExceptionResolver

class CustomAuthenticationEntryPointTest {
    private val resolver = mock<HandlerExceptionResolver>()
    private val request = mock<HttpServletRequest>()
    private val response = mock<HttpServletResponse>()
    private val handler = CustomAuthenticationEntryPoint(resolver)

    @Test
    fun `authentication failures are delegated to mvc exception handling`() {
        val exception = InsufficientAuthenticationException("Authentication required")

        handler.commence(request, response, exception)

        verify(resolver).resolveException(request, response, null, exception)
    }

    @Test
    fun `access denied failures are delegated to mvc exception handling`() {
        val exception = AccessDeniedException("Access denied")

        handler.handle(request, response, exception)

        verify(resolver).resolveException(request, response, null, exception)
    }
}
