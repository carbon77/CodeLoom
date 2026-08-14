package com.codeloom.backend.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.InsufficientAuthenticationException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RestExceptionHandlerTest {
    private val handler = RestExceptionHandler()
    private val request =
        MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/v1/problems"
        }

    @Test
    fun `authentication exceptions return standard unauthorized response`() {
        val response = handler.handleException(InsufficientAuthenticationException("internal detail"), request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.body?.status)
        assertEquals("Authentication required", response.body?.message)
        assertEquals("/v1/problems", response.body?.path)
        assertNull(response.body?.payload)
    }

    @Test
    fun `access denied exceptions return standard forbidden response`() {
        val response = handler.handleException(AccessDeniedException("internal detail"), request)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.body?.status)
        assertEquals("Access denied", response.body?.message)
        assertEquals("/v1/problems", response.body?.path)
        assertNull(response.body?.payload)
    }
}
