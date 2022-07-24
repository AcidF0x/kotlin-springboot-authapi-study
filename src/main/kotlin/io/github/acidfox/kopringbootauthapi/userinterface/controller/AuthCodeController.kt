package io.github.acidfox.kopringbootauthapi.userinterface.controller

import io.github.acidfox.kopringbootauthapi.application.request.SignUpAuthCodeIssueRequest
import io.github.acidfox.kopringbootauthapi.application.service.AuthCodeService
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/auth-code")
class AuthCodeController(
    private val authCodeService: AuthCodeService
) {
    @PostMapping("/sign-up")
    fun issueSignUpAuthCode(@RequestBody @Validated request: SignUpAuthCodeIssueRequest): String {
        // TODO : 발급 시간과 유효 기간 리턴 해주도록 변경
        authCodeService.issue(request.phoneNumber, AuthCodeType.SIGN_UP)
        return ResponseEntity.ok().body("").toString()
    }
}
