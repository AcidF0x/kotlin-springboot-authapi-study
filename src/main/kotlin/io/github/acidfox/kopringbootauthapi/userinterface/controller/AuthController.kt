package io.github.acidfox.kopringbootauthapi.userinterface.controller

import io.github.acidfox.kopringbootauthapi.application.request.AuthCodeValidateRequest
import io.github.acidfox.kopringbootauthapi.application.request.LoginRequest
import io.github.acidfox.kopringbootauthapi.application.request.PasswordChangeRequest
import io.github.acidfox.kopringbootauthapi.application.request.PasswordResetAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.application.response.AuthCodeIssuedResponse
import io.github.acidfox.kopringbootauthapi.application.response.AuthCodeValidateResponse
import io.github.acidfox.kopringbootauthapi.application.response.LoginResponse
import io.github.acidfox.kopringbootauthapi.application.response.OkResponse
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
import io.github.acidfox.kopringbootauthapi.application.service.AuthService
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.userinterface.aop.NotLoginOnly
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
    @NotLoginOnly
    fun issueSignUpAuthCode(@RequestBody @Validated request: SignUpAuthCodeIssueRequest): AuthCodeIssuedResponse {
        return authCodeService.issueSignupAuthCode(request.phoneNumber)
    }

    @PostMapping("/auth-code/signup/validate")
    @NotLoginOnly
    fun validateSignupAuthCode(@RequestBody @Validated request: AuthCodeValidateRequest): AuthCodeValidateResponse {
        return authCodeService.validate(request.phoneNumber, AuthCodeType.SIGN_UP, request.code)
    }

    @PostMapping("/auth-code/password-reset")
    @NotLoginOnly
    fun issuePasswordResetAuthCode(
        @RequestBody @Validated request: PasswordResetAuthCodeIssueRequest
    ): AuthCodeIssuedResponse {
        return authCodeService.issuePasswordResetAuthCode(request.phoneNumber, request.email)
    }

    @PostMapping("/auth-code/password-reset/validate")
    @NotLoginOnly
    fun validatePasswordResetAuthCode(@RequestBody @Validated request: AuthCodeValidateRequest): AuthCodeValidateResponse {
        return authCodeService.validate(request.phoneNumber, AuthCodeType.RESET_PASSWORD, request.code)
    }

    @PostMapping("/signup")
    @NotLoginOnly
    fun signUp(@RequestBody @Validated request: SignUpRequest): OkResponse {
        authService.signUp(request)
        return OkResponse()
    }

    @PostMapping("/login")
    @NotLoginOnly
    fun login(@RequestBody @Validated request: LoginRequest): LoginResponse {
        return authService.login(request)
    }

    @PostMapping("/password-reset")
    @NotLoginOnly
    fun passwordChange(@RequestBody @Validated request: PasswordChangeRequest): OkResponse {
        authService.passwordChanged(request)
        return OkResponse()
    }
}
