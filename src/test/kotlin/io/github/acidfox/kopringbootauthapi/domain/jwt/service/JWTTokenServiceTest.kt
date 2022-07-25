package io.github.acidfox.kopringbootauthapi.domain.jwt.service

import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
internal class JWTTokenServiceTest {
    @Autowired
    lateinit var jwtTokenService: JWTTokenService

    @Test
    @DisplayName("이메일로 JWT 토큰을 발급 받을 수 있다")
    fun testCreateJWTToken() {
        // Given
        val email = "mail@test.com"

        // When
        val result = jwtTokenService.createJWTToken(email)

        // Then
        val token = Jwts.parser()
            .setSigningKey(jwtTokenService.jwtSecretKey)
            .parseClaimsJws(result)
            .body

        Assertions.assertEquals(email, token.subject)
    }
}
