package io.github.acidfox.kopringbootauthapi.domain.user.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.user.exception.CannotSignupException
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import io.github.acidfox.kopringbootauthapi.domain.user.repository.UserRepository
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

internal class UserDomainServiceTest : BaseTestCase() {
    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var userDomainService: UserDomainService

    @Test
    @DisplayName("회원 가입 할 수 있다")
    fun testSignUp() {
        // Given
        val encodedPassword = "P!A!S!S!W!O!R!D"

        val requestDto = SignUpRequest(
            "mail@example.com",
            "사용자",
            "닉네임",
            "password",
            "01011112222"
        )

        val mockUser = User(
            requestDto.email,
            requestDto.nickname,
            encodedPassword,
            requestDto.name,
            requestDto.phoneNumber
        )

        every {
            userRepository.existsByEmailEqualsOrPhoneNumberEquals(requestDto.email, requestDto.phoneNumber)
        } returns false
        every { passwordEncoder.encode(requestDto.password) } returns encodedPassword
        every {
            userRepository.save(
                match {
                    it.email == requestDto.email &&
                        it.nickname == requestDto.nickname &&
                        it.phoneNumber == requestDto.phoneNumber &&
                        it.password == encodedPassword &&
                        it.name == requestDto.name
                }
            )
        } returns mockUser

        // When
        val result = userDomainService.signUp(requestDto)

        // Then
        verify(exactly = 1) {
            userRepository.save(
                match {
                    it.email == requestDto.email &&
                        it.nickname == requestDto.nickname &&
                        it.phoneNumber == requestDto.phoneNumber &&
                        it.password == encodedPassword &&
                        it.name == requestDto.name
                }
            )
        }
        Assertions.assertSame(mockUser, result)
    }

    @Test
    @DisplayName("중복된 회원 정보가 있는경우 회원 가입 할 수 없다")
    fun testSignUpWhenDuplicateEmail() {
        // Given
        val requestDto = SignUpRequest(
            "mail@example.com",
            "사용자",
            "닉네임",
            "password",
            "01011112222"
        )

        every {
            userRepository.existsByEmailEqualsOrPhoneNumberEquals(requestDto.email, requestDto.phoneNumber)
        } returns true

        // When && Then
        Assertions.assertThrows(CannotSignupException::class.java) {
            userDomainService.signUp(requestDto)
        }
        verify { userRepository.save(allAny()) wasNot called }
    }
}
