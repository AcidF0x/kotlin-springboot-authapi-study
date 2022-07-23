package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.model.AuthCode
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory.SMSMessageFactory
import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.SMSClient
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AuthCodeServiceTest() : BaseTestCase() {
    @RelaxedMockK
    lateinit var authCodeDomainService: AuthCodeDomainService
    @RelaxedMockK
    lateinit var smsMessageFactory: SMSMessageFactory
    @RelaxedMockK
    lateinit var smsClient: SMSClient
    @InjectMockKs
    lateinit var authCodeService: AuthCodeService

    @Test
    fun testIssue() {
        // Given
        val phoneNumber = "01012341234"
        val authCodeType = AuthCodeType.SIGN_UP
        val mockAuthCode = AuthCode(phoneNumber, authCodeType, "123123", 3, LocalDateTime.now())
        val mockMessageDto = SMSMessageDto(phoneNumber, "제목", "내용")
        val mockAuthCodeTTL = 100
        every { authCodeDomainService.getProperty("authCodeTTL") } returns mockAuthCodeTTL
        every { authCodeDomainService.issue(phoneNumber, authCodeType) } returns mockAuthCode
        every {
            smsMessageFactory.get(SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE, phoneNumber, any())
        } returns mockMessageDto

        // When
        authCodeService.issue(phoneNumber, authCodeType)

        // Then
        verify(exactly = 1) { authCodeDomainService.issue(phoneNumber, authCodeType) }
        verify(exactly = 1) {
            smsMessageFactory.get(
                SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE,
                phoneNumber,
                match { it["code"] == mockAuthCode.code && it["ttl"] == mockAuthCodeTTL.toString() }
            )
        }
        verify(exactly = 1) { smsClient.sendMessage(mockMessageDto) }
    }

    @Test
    fun testValidate() {
        // Given
        val phoneNumber = "01012341234"
        val authCodeType = AuthCodeType.SIGN_UP
        val code = "123123"
        // When
        authCodeService.validate(phoneNumber, authCodeType, code)

        // Then
        verify(exactly = 1) { authCodeDomainService.validate(phoneNumber, authCodeType, code) }
    }
}
