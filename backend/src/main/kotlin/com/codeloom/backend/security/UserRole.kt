package com.codeloom.backend.security

enum class UserRole(
    val roleName: String,
) {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
}