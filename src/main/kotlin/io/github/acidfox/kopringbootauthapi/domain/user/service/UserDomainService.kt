package io.github.acidfox.kopringbootauthapi.domain.user.service

import io.github.acidfox.kopringbootauthapi.application.request.SignUpRequest
import io.github.acidfox.kopringbootauthapi.domain.user.exception.CannotSignupException
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import io.github.acidfox.kopringbootauthapi.domain.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserDomainService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
) {
    fun signUp(request: SignUpRequest): User {
        val exists = userRepository.existsByEmailEqualsOrPhoneNumberEquals(request.email, request.phoneNumber)

        if (exists) {
            throw CannotSignupException("이미 가입된 유저 입니다.")
        }

        val user = User(
            request.email,
            request.nickname,
            passwordEncoder.encode(request.password),
            request.name,
            request.phoneNumber
        )

        return userRepository.save(user)
    }

    fun existsByPhoneNumber(phoneNumber: String) = userRepository.existsByPhoneNumber(phoneNumber)
}
