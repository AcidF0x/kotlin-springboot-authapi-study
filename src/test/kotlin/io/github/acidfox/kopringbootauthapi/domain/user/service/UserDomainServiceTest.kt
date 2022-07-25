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
        Assertions.assertThrows(
            CannotSignupException::class.java,
            {
                userDomainService.signUp(requestDto)
            },
            "이미 가입된 유저 입니다."
        )
        verify { userRepository.save(allAny()) wasNot called }
    }

    @Test
    @DisplayName("휴대 전화 번호로 가입된 사용자인지 확인 할 수 있다")
    fun testExistsByPhoneNumber() {
        // Given
        val phoneNumber = "01011112222"
        every { userRepository.existsByPhoneNumber(phoneNumber) } returns true

        // When
        val result = userDomainService.existsByPhoneNumber(phoneNumber)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("회원 정보 인증 - 이메일과 패스워드가 일치하는 사용자가 있는지 확인 할 수 있다")
    fun testExistsByEmailAndPasswordMatched() {
        // Given
        val email = "mail@mail.com"
        val password = "111112222233"
        val mockUser = User(
            email,
            "nickname",
            "password",
            "name",
            "01011112222"
        )

        every { userRepository.findByEmail(email) } returns mockUser
        every { passwordEncoder.matches(password, mockUser.password) } returns true

        // When
        val result = userDomainService.existsByEmailAndPassword(email, password)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("회원 정보 인증 - 이메일이 일치하는 사용자가 있더라도 패스워드가 일치하지 않으면 false를 리턴한다")
    fun testExistsByEmailAndPasswordReturnFalseWhenPasswordNotMatched() {
        // Given
        val email = "mail@mail.com"
        val password = "111112222233"
        val mockUser = User(
            email,
            "nickname",
            "password",
            "name",
            "01011112222"
        )

        every { userRepository.findByEmail(email) } returns mockUser
        every { passwordEncoder.matches(password, mockUser.password) } returns false

        // When
        val result = userDomainService.existsByEmailAndPassword(email, password)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    @DisplayName("회원 정보 인증 - 이메일이 일치하는 사용자 없으면 false를 리턴한다")
    fun testExistsByEmailAndPasswordReturnFalseWhenEmailNotFound() {
        // Given
        val email = "mail@mail.com"
        val password = "111112222233"
        every { userRepository.findByEmail(email) } returns null

        // When
        val result = userDomainService.existsByEmailAndPassword(email, password)

        // Then
        verify { passwordEncoder wasNot called }
        Assertions.assertFalse(result)
    }
}
