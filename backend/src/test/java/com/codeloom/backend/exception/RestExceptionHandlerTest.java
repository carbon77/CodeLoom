package com.codeloom.backend.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class RestExceptionHandlerTest {
  RestExceptionHandler handler = new RestExceptionHandler();
  MockHttpServletRequest request = new MockHttpServletRequest();

  @BeforeEach
  void setup() {
    request.setMethod("GET");
    request.setRequestURI("/v1/problems");
  }

  @Test
  void authenticationExceptionsReturnUnauthorized() {
    var r = handler.handleException(new InsufficientAuthenticationException("detail"), request);
    assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    assertEquals("Authentication required", r.getBody().message());
    assertEquals("/v1/problems", r.getBody().path());
    assertNull(r.getBody().payload());
  }

  @Test
  void accessDeniedReturnsForbidden() {
    var r = handler.handleException(new AccessDeniedException("detail"), request);
    assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
    assertEquals("Access denied", r.getBody().message());
  }
}
