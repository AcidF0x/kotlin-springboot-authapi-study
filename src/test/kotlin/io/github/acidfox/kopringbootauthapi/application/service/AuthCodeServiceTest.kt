package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.application.response.AuthCodeIssuedResponse
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.model.AuthCode
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory.SMSMessageFactory
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.SMSClient
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

internal class AuthCodeServiceTest() : BaseTestCase() {
    @RelaxedMockK
    lateinit var authCodeDomainService: AuthCodeDomainService
    @RelaxedMockK
    lateinit var smsMessageFactory: SMSMessageFactory
    @RelaxedMockK
    lateinit var smsClient: SMSClient
    @RelaxedMockK
    lateinit var userDomainService: UserDomainService
    @InjectMockKs
    lateinit var authCodeService: AuthCodeService

    @Test
    @DisplayName("회원 가입 인증 코드 발급 - 인증 코드 검증 로직 확인후 발급 한다")
    fun issueSignupAuthCode() {
        // Given
        val phoneNumber = "01012341234"
        val spyAuthCodeService = spyk(
            AuthCodeService(userDomainService, authCodeDomainService, smsMessageFactory, smsClient),
            recordPrivateCalls = true
        )

        every { spyAuthCodeService.issueSignupAuthCode(phoneNumber) } answers { callOriginal() }
        every { userDomainService.existsByPhoneNumber(phoneNumber) } returns false
        every { authCodeDomainService.checkCanIssueAuthCode(false, AuthCodeType.SIGN_UP) } returns true

        // When
        spyAuthCodeService.issueSignupAuthCode(phoneNumber)

        // Then
        verify(exactly = 1) { userDomainService.existsByPhoneNumber(phoneNumber) }
        verify(exactly = 1) { authCodeDomainService.checkCanIssueAuthCode(false, AuthCodeType.SIGN_UP) }
        verify(exactly = 1) {
            spyAuthCodeService invoke "issue" withArguments listOf(phoneNumber, AuthCodeType.SIGN_UP)
        }
    }

    @Test
    @DisplayName("비밀 번호 변경 인증 코드 발급 - 인증 코드 검증 로직 확인후 발급 한다")
    fun issuePasswordResetAuthCode() {
        // Given
        val phoneNumber = "01012341234"
        val email = "test@example.com"
        val spyAuthCodeService = spyk(
            AuthCodeService(userDomainService, authCodeDomainService, smsMessageFactory, smsClient),
            recordPrivateCalls = true
        )

        every { spyAuthCodeService.issuePasswordResetAuthCode(phoneNumber, email) } answers { callOriginal() }
        every { userDomainService.existsByEmailAndPhoneNumber(email, phoneNumber) } returns true
        every {
            authCodeDomainService.checkCanIssueAuthCode(true, AuthCodeType.RESET_PASSWORD)
        } returns true

        // When
        spyAuthCodeService.issuePasswordResetAuthCode(phoneNumber, email)

        // Then
        verify(exactly = 1) { userDomainService.existsByEmailAndPhoneNumber(email, phoneNumber) }
        verify(exactly = 1) {
            authCodeDomainService.checkCanIssueAuthCode(true, AuthCodeType.RESET_PASSWORD)
        }
        verify(exactly = 1) {
            spyAuthCodeService invoke "issue" withArguments listOf(phoneNumber, AuthCodeType.RESET_PASSWORD)
        }
    }

    @Test
    @DisplayName("인증 코드 발급 로직 - 회원가입 인증 코드를 생성후 SMS 메세지를 발송 한다")
    fun testIssueWithSignup() {
        // Given
        val phoneNumber = "01012341234"
        val type = AuthCodeType.SIGN_UP
        val mockAuthCode = AuthCode(phoneNumber, type, "123", 1, LocalDateTime.now())
        val reflectionMethod = AuthCodeService::class.declaredMemberFunctions.find { it.name == "issue" }
        reflectionMethod!!.isAccessible = true

        val mockMessage = SMSMessageDto(phoneNumber, "subject", "this is sms message")
        val codeTTL = 1

        every { authCodeDomainService.issue(phoneNumber, type) } returns mockAuthCode
        every { authCodeDomainService getProperty "authCodeTTL" } returns codeTTL
        every {
            smsMessageFactory.get(
                SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE,
                phoneNumber,
                match { it["code"] == mockAuthCode.code && it["ttl"] == authCodeDomainService.authCodeTTL.toString() }
            )
        } returns mockMessage

        // When
        val result = reflectionMethod.call(this.authCodeService, phoneNumber, type) as AuthCodeIssuedResponse

        // Then
        verify(exactly = 1) { smsMessageFactory.get(any(), any(), any()) }
        verify { smsClient.sendMessage(mockMessage) }
        Assertions.assertSame(codeTTL * 60, result.expiredIn)
    }

    @Test
    @DisplayName("인증 코드 발급 로직 - 비밀번호 초기화 인증 코드를 생성후 SMS 메세지를 발송 한다")
    fun testIssueWitPasswordReset() {
        // Given
        val phoneNumber = "01012341234"
        val type = AuthCodeType.RESET_PASSWORD
        val mockAuthCode = AuthCode(phoneNumber, type, "123", 1, LocalDateTime.now())
        val reflectionMethod = AuthCodeService::class.declaredMemberFunctions.find { it.name == "issue" }
        reflectionMethod!!.isAccessible = true

        val mockMessage = SMSMessageDto(phoneNumber, "subject", "this is sms message")
        val codeTTL = 1

        every { authCodeDomainService.issue(phoneNumber, type) } returns mockAuthCode
        every { authCodeDomainService getProperty "authCodeTTL" } returns codeTTL
        every {
            smsMessageFactory.get(
                SMSMessageType.PASSWORD_RESET_REQUEST_AUTH_CODE,
                phoneNumber,
                match { it["code"] == mockAuthCode.code && it["ttl"] == authCodeDomainService.authCodeTTL.toString() }
            )
        } returns mockMessage

        // When
        val result = reflectionMethod.call(this.authCodeService, phoneNumber, type) as AuthCodeIssuedResponse

        // Then
        verify(exactly = 1) { smsMessageFactory.get(any(), any(), any()) }
        verify { smsClient.sendMessage(mockMessage) }
        Assertions.assertSame(codeTTL * 60, result.expiredIn)
    }

    @Test
    fun testValidate() {
        // Given
        val phoneNumber = "01012341234"
        val authCodeType = AuthCodeType.SIGN_UP
        val code = "123123"
        val mockValidateLifeTime = 20
        every { authCodeDomainService getProperty "authCodeValidatedLifeTime" } returns mockValidateLifeTime

        // When
        val result = authCodeService.validate(phoneNumber, authCodeType, code)

        // Then
        verify(exactly = 1) { authCodeDomainService.validate(phoneNumber, authCodeType, code) }
        Assertions.assertEquals(mockValidateLifeTime * 60, result.expiredIn)
    }
}
