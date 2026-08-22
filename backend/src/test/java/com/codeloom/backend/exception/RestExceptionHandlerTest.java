package com.codeloom.backend.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class RestExceptionHandlerTest {
    private final RestExceptionHandler restExceptionHandler = new RestExceptionHandler();
    private final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

    @BeforeEach
    void setup() {
        httpServletRequest.setMethod("GET");
        httpServletRequest.setRequestURI("/v1/problems");
    }

    @Test
    void authenticationExceptionsReturnUnauthorized() {
        var r = restExceptionHandler.handleException(
                new InsufficientAuthenticationException("detail"), httpServletRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
        assert r.getBody() != null;
        assertEquals("Authentication required", r.getBody().message());
        assertEquals("/v1/problems", r.getBody().path());
        assertNull(r.getBody().payload());
    }

    @Test
    void accessDeniedReturnsForbidden() {
        var r = restExceptionHandler.handleException(new AccessDeniedException("detail"), httpServletRequest);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        assert r.getBody() != null;
        assertEquals("Access denied", r.getBody().message());
    }
}
