package io.github.acidfox.kopringbootauthapi.domain.user.service

import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserDomainService(
    private val userRepository: UserRepository
) {
    fun signUp(request: SignUpRequest) {

    }
}
