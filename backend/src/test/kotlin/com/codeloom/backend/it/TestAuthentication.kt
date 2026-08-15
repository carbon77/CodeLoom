package com.codeloom.backend.it

import com.codeloom.backend.security.UserRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import java.util.UUID

val TEST_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

fun MockHttpServletRequestDsl.initUser(
    userId: UUID = TEST_USER_ID,
    role: UserRole = UserRole.ADMIN,
) {
    val jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(userId.toString())
            .build()
    principal = JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority(role.roleName)))
}
