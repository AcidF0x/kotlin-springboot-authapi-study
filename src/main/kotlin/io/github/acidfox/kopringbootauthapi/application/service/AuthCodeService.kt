package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory.SMSMessageFactory
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
    fun issueSignupAuthCode(phoneNumber: String) {
        val isUserExists = userDomainService.existsByPhoneNumber(phoneNumber)
        authCodeDomainService.checkCanIssueAuthCode(isUserExists, AuthCodeType.SIGN_UP)
        issue(phoneNumber, AuthCodeType.SIGN_UP)
    }

    @Transactional
    fun issuePasswordResetAuthCode(phoneNumber: String, email: String) {
        val isUserExists = userDomainService.existsByEmailAndPhoneNumber(email, phoneNumber)
        authCodeDomainService.checkCanIssueAuthCode(isUserExists, AuthCodeType.RESET_PASSWORD)
        issue(phoneNumber, AuthCodeType.RESET_PASSWORD)
    }

    private fun issue(phoneNumber: String, type: AuthCodeType) {
        val authCode = authCodeDomainService.issue(phoneNumber, type)
        val messageParams = mapOf(
            Pair("code", authCode.code), Pair("ttl", authCodeDomainService.authCodeTTL.toString())
        )

        val messageType = when (type) {
            AuthCodeType.SIGN_UP -> SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE
            AuthCodeType.RESET_PASSWORD -> SMSMessageType.PASSWORD_RESET_REQUEST_AUTH_CODE
        }

        val message = smsMessageFactory.get(messageType, phoneNumber, messageParams)
        smsClient.sendMessage(message)
    }

    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String) =
        authCodeDomainService.validate(phoneNumber, authCodeType, code)
}
