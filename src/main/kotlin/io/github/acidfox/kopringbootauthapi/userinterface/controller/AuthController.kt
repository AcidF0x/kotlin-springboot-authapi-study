package io.github.acidfox.kopringbootauthapi.userinterface.controller

import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeValidateRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
import io.github.acidfox.kopringbootauthapi.application.service.AuthService
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authCodeService: AuthCodeService,
    private val authService: AuthService,
) {
    @PostMapping("/auth-code/signup")
    fun issueSignUpAuthCode(@RequestBody @Validated request: SignUpAuthCodeIssueRequest): String {
        // TODO : 발급 시간과 유효 기간 리턴 해주도록 변경
        authCodeService.issue(request.phoneNumber, AuthCodeType.SIGN_UP)
        return ResponseEntity.ok().body("").toString()
    }

    @PostMapping("/auth-code/signup/validate")
    fun validateSignupAuthCode(@RequestBody @Validated request: SignUpAuthCodeValidateRequest): String {
        authCodeService.validate(request.phoneNumber, AuthCodeType.SIGN_UP, request.code)
        return ResponseEntity.ok().body("").toString()
    }

    @PostMapping("/signup")
    fun signUp(@RequestBody @Validated request: SignUpRequest): String {
        authService.signUp(request)
        return ResponseEntity.ok().body("").toString()
    }
}
