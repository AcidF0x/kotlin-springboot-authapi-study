package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class AuthServiceTest : BaseTestCase() {
    @MockK
    lateinit var authCodeDomainService: AuthCodeDomainService

    @MockK
    lateinit var userDomainService: UserDomainService

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
}
