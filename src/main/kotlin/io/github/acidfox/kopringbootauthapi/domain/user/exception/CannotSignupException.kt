package io.github.acidfox.kopringbootauthapi.domain.user.exception

import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus

class CannotSignupException(message: String) : CustomException(HttpStatus.BAD_REQUEST, message) {
    override val code: Int = 200
}
