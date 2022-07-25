package io.github.acidfox.kopringbootauthapi.domain.user.exception

import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus

class UserNotFoundException(message: String) : CustomException(HttpStatus.NOT_FOUND, message) {
    override val code: Int = 300
}
