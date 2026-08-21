package com.codeloom.backend.it;

import com.codeloom.backend.security.UserRole;
import java.util.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

final class TestAuthentication {
  static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private TestAuthentication() {}

  static JwtAuthenticationToken user(UserRole... roles) {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(TEST_USER_ID.toString())
            .build();
    return new JwtAuthenticationToken(
        jwt, Arrays.stream(roles).map(r -> new SimpleGrantedAuthority(r.getRoleName())).toList());
  }

  static JwtAuthenticationToken admin() {
    return user(UserRole.USER, UserRole.ADMIN);
  }
}
