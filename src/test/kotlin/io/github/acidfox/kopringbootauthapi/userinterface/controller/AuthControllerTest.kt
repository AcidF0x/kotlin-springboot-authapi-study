package io.github.acidfox.kopringbootauthapi.userinterface.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.github.acidfox.kopringbootauthapi.BaseControllerTestCase
import io.github.acidfox.kopringbootauthapi.application.request.LoginRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeValidateRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.application.response.LoginResponse
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
import io.github.acidfox.kopringbootauthapi.application.service.AuthService
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.mockk.Called
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class AuthControllerTest : BaseControllerTestCase() {
    @MockkBean
    lateinit var authCodeService: AuthCodeService

    @MockkBean
    lateinit var authService: AuthService

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    @DisplayName("Post 요청으로 회원 가입 인증 코드 요청을 할 수 있다")
    fun testIssueSignUpAuthCode() {
        // Given
        val url = "/api/auth/auth-code/signup"
        val requestDto = SignUpAuthCodeIssueRequest("01011112222")
        val json = mapper.writeValueAsString(requestDto)

        every { authCodeService.issueSignupAuthCode(requestDto.phoneNumber) } just runs

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify(exactly = 1) { authCodeService.issueSignupAuthCode(requestDto.phoneNumber) }
        result.andExpect(status().isOk)
    }

    @Test
    @DisplayName("휴대전화 형식이 아닌 번호는 인증 코드 요청을 할 수 없다")
    fun testIssueExceptionWhenPhoneNumberIsInvalidFormat() {
        // Given
        val url = "/api/auth/auth-code/signup"
        val requestDto = SignUpAuthCodeIssueRequest("02-119-119")
        val json = mapper.writeValueAsString(requestDto)
        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify { authCodeService wasNot Called }
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("-1"))
            .andExpect(jsonPath("$.message").value("휴대전화 번호를 확인해주세요"))
    }

    @Test
    @DisplayName("회원 가입 인증 코드를 검증 할 수 있다")
    fun testValidateSignupAuthCode() {
        // Given
        val url = "/api/auth/auth-code/signup/validate"
        val requestDto = SignUpAuthCodeValidateRequest("01011112222", "123123")
        val json = mapper.writeValueAsString(requestDto)
        every { authCodeService.validate(requestDto.phoneNumber, AuthCodeType.SIGN_UP, requestDto.code) } returns true

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify(exactly = 1) {
            authCodeService.validate(requestDto.phoneNumber, AuthCodeType.SIGN_UP, requestDto.code)
        }
        result.andExpect(status().isOk)
    }

    @Test
    @DisplayName("올바르지 않은 휴대 전화 번호는 회원 가입 인증 코드를 검증 할 수 없다")
    fun testValidateExceptionWhenInvalidPhoneNumber() {
        // Given
        val url = "/api/auth/auth-code/signup/validate"
        val requestDto = SignUpAuthCodeValidateRequest("02-119-119", "123123")
        val json = mapper.writeValueAsString(requestDto)

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify { authCodeService wasNot Called }
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("-1"))
            .andExpect(jsonPath("$.message").value("휴대전화 번호를 확인해주세요"))
    }

    @Test
    @DisplayName("인증코드 없이 회원 가입 인증 코드를 검증 할 수 없다")
    fun testValidateExceptionWhenEmptyCode() {
        // Given
        val url = "/api/auth/auth-code/signup/validate"
        val requestDto = SignUpAuthCodeValidateRequest("01011112222", "")
        val json = mapper.writeValueAsString(requestDto)

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify { authCodeService wasNot Called }
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("-1"))
            .andExpect(jsonPath("$.message").value("코드를 입력해주세요"))
    }

    @Test
    @DisplayName("회원 가입시 필요 정보가 모두 정상적이어야 회원 가입 할 수 있다")
    fun testSignupExceptionWhenInvalidParams() {
        // Given
        val url = "/api/auth/signup"

        val requestDto = SignUpRequest(
            "test@mail.com",
            "name",
            "nickname",
            "password",
            "01011112222"
        )

        // fieldName, changed value, expectedMessage
        val testCase = listOf(
            Triple("email", "", "이메일을 입력 해주세요"),
            Triple("email", "not-completed-email@", "올바른 이메일이 아닙니다"),
            Triple("name", "", "이름을 입력 해주세요"),
            Triple("nickname", "", "닉네임을 입력 해주세요"),
            Triple("password", "short", "비밀번호는 6자 이상 입력해주세요"),
            Triple("password", "", "비밀번호는 6자 이상 입력해주세요"),
            Triple("phoneNumber", "00012341234", "휴대전화 번호를 확인해주세요"),
            Triple("phoneNumber", "", "휴대전화 번호를 확인해주세요"),
        )

        for (i in testCase) {
            var oldValue: String
            val property = requestDto.javaClass.getDeclaredField(i.first)

            property.isAccessible = true
            oldValue = property.get(requestDto) as String
            property.set(requestDto, i.second)

            val json = mapper.writeValueAsString(requestDto)

            // When
            val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

            // Then
            verify { authService wasNot Called }
            result.andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("-1"))
                .andExpect(jsonPath("$.message").value(i.third))

            property.set(requestDto, oldValue)
            property.isAccessible = false
        }
    }

    @Test
    @DisplayName("회원 가입시 필요 정보가 모두 정상적이면 회원 가입 할 수 있다")
    fun testSignup() {
        // Given
        val url = "/api/auth/signup"

        val requestDto = SignUpRequest(
            "test@mail.com",
            "name",
            "nickname",
            "password",
            "01011112222"
        )

        val json = mapper.writeValueAsString(requestDto)

        every { authService.signUp(requestDto) } returns true

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify(exactly = 1) { authService.signUp(requestDto) }
        result.andExpect(status().isOk)
    }

    @Test
    @DisplayName("이메일과 패스워드가 정상적이면 로그인 할 수 있다")
    fun testLogin() {
        // Given
        val url = "/api/auth/login"

        val requestDto = LoginRequest(
            "test@mail.com",
            "this_is_password"
        )

        val json = mapper.writeValueAsString(requestDto)
        val mockResponse = LoginResponse("this is jwt token :)")

        every { authService.login(requestDto) } returns mockResponse

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify(exactly = 1) { authService.login(requestDto) }
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(mockResponse.token))
            .andExpect(jsonPath("$.token_type").value(mockResponse.tokenType))
    }

    @Test
    @DisplayName("이메일과 패스워드가 정상적이지 않으면 로그인 할 수 없다")
    fun testLoginExceptionWhenInvalidParams() {

        val url = "/api/auth/login"

        val requestDto = LoginRequest(
            "test@mail.com",
            "this_is_password"
        )

        // fieldName, changed value, expectedMessage
        val testCase = listOf(
            Triple("email", "", "이메일을 입력 해주세요"),
            Triple("email", "not-completed-email@", "올바른 이메일이 아닙니다"),
            Triple("password", "short", "비밀번호는 6자 이상 입력해주세요"),
            Triple("password", "", "비밀번호는 6자 이상 입력해주세요"),
        )

        for (i in testCase) {
            var oldValue: String
            val property = requestDto.javaClass.getDeclaredField(i.first)

            property.isAccessible = true
            oldValue = property.get(requestDto) as String
            property.set(requestDto, i.second)

            val json = mapper.writeValueAsString(requestDto)

            // When
            val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

            // Then
            verify { authService wasNot Called }
            result.andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("-1"))
                .andExpect(jsonPath("$.message").value(i.third))

            property.set(requestDto, oldValue)
            property.isAccessible = false
        }
    }
}
