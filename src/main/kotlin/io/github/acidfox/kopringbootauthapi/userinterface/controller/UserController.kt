package io.github.acidfox.kopringbootauthapi.userinterface.controller

import io.github.acidfox.kopringbootauthapi.application.response.UserInfoResponse
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: User): UserInfoResponse {
        return UserInfoResponse(user)
    }
}
