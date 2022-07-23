package io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory

import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class SMSMessageFactoryTest() {
    @Autowired
    lateinit var smsMessageFactory: SMSMessageFactory

    @Test
    @DisplayName("회원 가입시 사용하는 인증 코드 SMS Message를 요청 할 수 있다")
    fun testGetSignUpRequestAuthCode() {
        // Given
        val messageType = SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE
        val params = mapOf(Pair("code", "123123"), Pair("ttl", "99"))
        val phoneNumber = "01012341234"

        // When
        val result = smsMessageFactory.get(messageType, phoneNumber, params)

        // Then
        Assertions.assertSame(phoneNumber, result.phoneNumber)
        Assertions.assertSame("회원가입 인증코드", result.subject)
        Assertions.assertTrue(result.body.contains("123123"))
        Assertions.assertTrue(result.body.contains("99"))
    }

    @Test
    @DisplayName("비밀번호 초기화시 사용하는 인증 코드 SMS Message를 요청 할 수 있다")
    fun testGetPasswordResetRequestAuthCode() {
        // Given
        val messageType = SMSMessageType.PASSWORD_RESET_REQUEST_AUTH_CODE
        val params = mapOf(Pair("code", "000000"), Pair("ttl", "44"))
        val phoneNumber = "01012341234"

        // When
        val result = smsMessageFactory.get(messageType, phoneNumber, params)

        // Then
        Assertions.assertSame(phoneNumber, result.phoneNumber)
        Assertions.assertSame("비밀번호 초기화 인증코드", result.subject)
        Assertions.assertTrue(result.body.contains("000000"))
        Assertions.assertTrue(result.body.contains("44"))
    }
}
