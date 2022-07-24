package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authCodeDomainService: AuthCodeDomainService,
    private val userDomainService: UserDomainService
) {

    @Transactional
    fun signUp(requestDto: SignUpRequest): Boolean {
        authCodeDomainService.verifyValidation(requestDto.phoneNumber, AuthCodeType.SIGN_UP)
        userDomainService.signUp(requestDto)

        return true
    }
}
