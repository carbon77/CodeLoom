package com.codeloom.backend.security;

import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class AuthenticationUtils {
    private AuthenticationUtils() {}

    public static UUID getUserId(Authentication a) {
        return UUID.fromString(
                Objects.requireNonNull(((JwtAuthenticationToken) a).getToken().getSubject()));
    }

    public static boolean hasRole(Authentication a, UserRole r) {
        return a.getAuthorities().stream().anyMatch(x -> Objects.equals(x.getAuthority(), r.getRoleName()));
    }

    public static boolean isRegularUser(Authentication a) {
        return !hasRole(a, UserRole.ADMIN) && hasRole(a, UserRole.USER);
    }

    public static boolean isAdmin(Authentication a) {
        return hasRole(a, UserRole.ADMIN) && hasRole(a, UserRole.USER);
    }
}
