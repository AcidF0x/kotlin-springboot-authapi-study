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

    @Test
    @DisplayName("Bearer가 포함된 문자열에서 토큰만 분리 할 수 있다")
    fun testParseJWTTokenFromHeader() {
        // Given
        val token = "wow_this_is_token_wow"

        // When
        val result = jwtTokenService.parseJWTTokenFromHeader("Bearer $token")

        // Then
        Assertions.assertEquals(token, result)
    }

    @Test
    @DisplayName("Bearer가 포함된 문자열이 아니면 빈 String을 리턴한다")
    fun testParseJWTTokenFromHeaderReturnEmptyStringWhenInvalidParams() {
        // Given
        val token = "wow_this_is_token_wow"

        // When
        val result = jwtTokenService.parseJWTTokenFromHeader(token)

        // Then
        Assertions.assertTrue(result.isBlank())
    }


    @Test
    @DisplayName("토큰에서 이메일을 가져 올 수 있다")
    fun testParseEmailFromJWTToken() {
        // Given
        val email = "mail@test.com"
        val token = jwtTokenService.createJWTToken(email)

        // When
        val result = jwtTokenService.parseEmailFromJWTToken(token)

        // Then
        Assertions.assertEquals(email, result)
    }
}
