package io.github.acidfox.kopringbootauthapi.domain.jwt.exception

import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus

class InvalidTokenException(message: String) : CustomException(HttpStatus.UNAUTHORIZED, message) {
    override val code: Int = 400
}
