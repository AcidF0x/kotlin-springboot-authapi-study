package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory.SMSMessageFactory
import io.github.acidfox.kopringbootauthapi.domain.user.exception.UserNotFoundException
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.SMSClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthCodeService(
    private val userDomainService: UserDomainService,
    private val authCodeDomainService: AuthCodeDomainService,
    private val smsMessageFactory: SMSMessageFactory,
    private val smsClient: SMSClient
) {
    @Transactional
    fun issue(phoneNumber: String, authCodeType: AuthCodeType) {
        val isUserExists = userDomainService.existsByPhoneNumber(phoneNumber)

        authCodeDomainService.checkCanIssueAuthCode(isUserExists, authCodeType)
        val authCode = authCodeDomainService.issue(phoneNumber, authCodeType)
        val messageParams = mapOf(
            Pair("code", authCode.code), Pair("ttl", authCodeDomainService.authCodeTTL.toString())
        )

        val messageType = when (authCodeType) {
            AuthCodeType.SIGN_UP -> SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE
            AuthCodeType.RESET_PASSWORD -> SMSMessageType.PASSWORD_RESET_REQUEST_AUTH_CODE
        }

        val message = smsMessageFactory.get(messageType, phoneNumber, messageParams)
        smsClient.sendMessage(message)
    }

    fun issuePasswordResetAuthCode(phoneNumber: String, email: String) {
        val isUserExists = userDomainService.existsByEmailAndPhoneNumber(phoneNumber, email)

        if (!isUserExists) {
            throw UserNotFoundException("사용자 정보를 찾을 수 없습니다, 휴대전화 번호 또는 이메일을 확인해주세요")
        }

        this.issue(phoneNumber, AuthCodeType.RESET_PASSWORD)
    }

    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String) =
        authCodeDomainService.validate(phoneNumber, authCodeType, code)
}
