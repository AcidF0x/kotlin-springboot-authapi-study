package io.github.acidfox.kopringbootauthapi.domain.jwt.service

import io.github.acidfox.kopringbootauthapi.domain.jwt.exception.PasswordChangedTokenException
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.sql.Timestamp
import java.time.LocalDateTime

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
        val passwordChangedAt = LocalDateTime.now()

        // When
        val result = jwtTokenService.createJWTToken(email, passwordChangedAt)

        // Then
        val token = Jwts.parser()
            .setSigningKey(jwtTokenService.jwtSecretKey)
            .parseClaimsJws(result)
            .body

        Assertions.assertEquals(email, token.subject)
        Assertions.assertEquals(Timestamp.valueOf(passwordChangedAt).toString(), token.id)
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
    @DisplayName("토큰에서 JWT Claims을 파싱 할 수있다")
    fun testParseClaimsFromJWTToken() {
        // Given
        val email = "mail@test.com"
        val passwordChangedAt = LocalDateTime.now()

        val token = jwtTokenService.createJWTToken(email, passwordChangedAt)

        // When
        val result = jwtTokenService.parseClaimsFromJWTToken(token)

        // Then
        Assertions.assertEquals(email, result.subject)
    }

    @Test
    @DisplayName("jwt claim으로 비밀번호 변경 여부를 확인 할 수 있다")
    fun testValidatePasswordChanged() {
        // Given
        val email = "mail@test.com"
        val passwordChangedAt = LocalDateTime.now()
        val claims = Jwts.claims().setSubject(email).setId(Timestamp.valueOf(passwordChangedAt).toString())

        // When
        val result = jwtTokenService.validatePasswordChanged(claims, passwordChangedAt)

        // Then
        Assertions.assertSame(Unit, result)
    }

    @Test
    @DisplayName("jwt claim으로 비밀번호 변경 여부를 확인 할 수 있다 - 변경 되었다면 Exception을 발생 시킨다.")
    fun testValidatePasswordChangedThrowExceptionWhenPasswordChangedAtNotMatched() {
        // Given
        val email = "mail@test.com"
        val passwordChangedAt = LocalDateTime.now()
        val claims = Jwts.claims().setSubject(email).setId(Timestamp.valueOf(passwordChangedAt).toString())

        // When && Then

        Assertions.assertThrows(
            PasswordChangedTokenException::class.java,
            {
                jwtTokenService.validatePasswordChanged(claims, passwordChangedAt.plusDays(1))
            },
            "비밀번호가 변경되었습니다. 다시 로그인 해주세요"
        )
    }
}
