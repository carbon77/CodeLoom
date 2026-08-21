package com.codeloom.backend.security;

public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");
    private final String roleName;

    UserRole(String n) {
        roleName = n;
    }

    public String getRoleName() {
        return roleName;
    }
}
