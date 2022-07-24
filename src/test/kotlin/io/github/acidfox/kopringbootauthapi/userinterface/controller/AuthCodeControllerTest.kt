package io.github.acidfox.kopringbootauthapi.userinterface.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.github.acidfox.kopringbootauthapi.BaseControllerTestCase
import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
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

internal class AuthCodeControllerTest : BaseControllerTestCase() {
    @MockkBean
    lateinit var authCodeService: AuthCodeService

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    @DisplayName("Post 요청으로 회원 가입 인증 코드 요청을 할 수 있다")
    fun testIssueSignUpAuthCode() {
        // Given
        val url = "/api/auth/auth-code/sign-up"
        val requestDto = SignUpAuthCodeIssueRequest("01011112222")
        val json = mapper.writeValueAsString(requestDto)

        every { authCodeService.issue(requestDto.phoneNumber, AuthCodeType.SIGN_UP) } just runs

        // When
        val result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))

        // Then
        verify(exactly = 1) { authCodeService.issue(requestDto.phoneNumber, AuthCodeType.SIGN_UP) }
        result.andExpect(status().isOk)
    }

    @Test
    @DisplayName("휴대전화 형식이 아닌 번호는 인증 코드 요청을 할 수 없다")
    fun testIssueExceptionWhenPhoneNumberIsInvalidFormat() {
        // Given
        val url = "/api/auth/auth-code/sign-up"
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
}
