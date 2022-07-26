package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.application.request.LoginRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.jwt.service.JWTTokenService
import io.github.acidfox.kopringbootauthapi.domain.user.exception.UserNotFoundException
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class AuthServiceTest : BaseTestCase() {
    @MockK
    lateinit var authCodeDomainService: AuthCodeDomainService

    @MockK
    lateinit var userDomainService: UserDomainService

    @MockK
    lateinit var jwtTokenService: JWTTokenService

    @InjectMockKs
    lateinit var authService: AuthService

    @Test
    @DisplayName("회원 가입 할 수 있다")
    fun testSignUp() {
        // Given
        val request = SignUpRequest(
            "email@example.com",
            "1234567890abcdefghijklmnopqr",
            "1234567890abcdefghijklmnopqr",
            "password",
            "01011112222"
        )

        val mockUser = User(
            request.email,
            request.nickname,
            request.password,
            request.name,
            request.password
        )

        every { authCodeDomainService.verifyValidation(request.phoneNumber, AuthCodeType.SIGN_UP) } returns true
        every { userDomainService.signUp(request) } returns mockUser
        every { authCodeDomainService.delete(request.phoneNumber, AuthCodeType.SIGN_UP) } just runs

        // When
        val result = authService.signUp(request)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("로그인 할 수 있다")
    fun testLogin() {
        // Given
        val request = LoginRequest(
            "email@example.com",
            "1234567890abcdefghijklmnopqr",
        )

        val mockJWTToken = "this is jwtToken...."

        val mockUser = User(
            request.email,
            "nickname",
            request.password,
            "name",
            "01011112222"
        )

        every { userDomainService.findByEmailAndPassword(request.email, request.password) } returns mockUser
        every { jwtTokenService.createJWTToken(request.email, mockUser.passwordChangedAt) } returns mockJWTToken

        // When
        val response = authService.login(request)

        // Then
        Assertions.assertSame(mockJWTToken, response.token)
    }

    @Test
    @DisplayName("사용자 정보가 일치하지 않는 경우 로그인 할 수 없다")
    fun testLoginExceptionWhenUserInformationNotMatched() {
        // Given
        val request = LoginRequest(
            "email@example.com",
            "1234567890abcdefghijklmnopqr",
        )

        every { userDomainService.findByEmailAndPassword(request.email, request.password) } returns null

        // When && Then
        Assertions.assertThrows(
            UserNotFoundException::class.java,
            {
                authService.login(request)
            },
            "사용자를 찾을 수 없습니다, 이메일 또는 비밀번호를 확인 해 주세요"
        )
        verify { jwtTokenService wasNot Called }
    }
}
