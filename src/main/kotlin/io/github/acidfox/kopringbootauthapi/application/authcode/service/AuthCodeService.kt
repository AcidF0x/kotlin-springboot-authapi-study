package io.github.acidfox.kopringbootauthapi.application.authcode.service

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import org.springframework.stereotype.Service

@Service
class AuthCodeService(
    private val authCodeDomainService: AuthCodeDomainService
) {
    fun issue(phoneNumber: String, authCodeType: AuthCodeType) = authCodeDomainService.issue(phoneNumber, authCodeType)
    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String)
        = authCodeDomainService.validate(phoneNumber, authCodeType, code)
}
