package com.codeloom.backend.security;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint
    implements AuthenticationEntryPoint, AccessDeniedHandler {
  private final HandlerExceptionResolver resolver;

  public CustomAuthenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver r) {
    resolver = r;
  }

  public void commence(HttpServletRequest q, HttpServletResponse s, AuthenticationException e) {
    resolver.resolveException(q, s, null, e);
  }

  public void handle(HttpServletRequest q, HttpServletResponse s, AccessDeniedException e) {
    resolver.resolveException(q, s, null, e);
  }
}
