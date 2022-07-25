package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.application.request.LoginRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.github.acidfox.kopringbootauthapi.domain.jwt.service.JWTTokenService
import io.github.acidfox.kopringbootauthapi.domain.user.exception.UserNotFoundException
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authCodeDomainService: AuthCodeDomainService,
    private val userDomainService: UserDomainService,
    private val jwtTokenService: JWTTokenService,
) {
    @Transactional
    fun signUp(requestDto: SignUpRequest): Boolean {
        authCodeDomainService.verifyValidation(requestDto.phoneNumber, AuthCodeType.SIGN_UP)
        userDomainService.signUp(requestDto)
        authCodeDomainService.delete(requestDto.phoneNumber, AuthCodeType.SIGN_UP)
        return true
    }

    @Transactional
    fun login(requestDto: LoginRequest): String {
        val isUserExists = userDomainService.existsByEmailAndPassword(requestDto.email, requestDto.password)

        if (!isUserExists) {
            throw UserNotFoundException("사용자를 찾을 수 없습니다, 이메일 또는 비밀번호를 확인 해 주세요")
        }

        return jwtTokenService.createJWTToken(requestDto.email)
    }
}
