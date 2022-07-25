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
    fun issue(phoneNumber: String, authCodeType: AuthCodeType) {
        val isUserExists = userDomainService.existsByPhoneNumber(phoneNumber)

        authCodeDomainService.checkCanIssueAuthCode(isUserExists, authCodeType)
        val authCode = authCodeDomainService.issue(phoneNumber, authCodeType)
        val messageParams = mapOf(
            Pair("code", authCode.code), Pair("ttl", authCodeDomainService.authCodeTTL.toString())
        )
        val message = smsMessageFactory.get(SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE, phoneNumber, messageParams)
        smsClient.sendMessage(message)
    }

    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String) =
        authCodeDomainService.validate(phoneNumber, authCodeType, code)
}
