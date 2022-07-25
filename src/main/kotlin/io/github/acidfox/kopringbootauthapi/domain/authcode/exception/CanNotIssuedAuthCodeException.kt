package io.github.acidfox.kopringbootauthapi.domain.authcode.exception

import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus

class CanNotIssuedAuthCodeException(message: String) : CustomException(HttpStatus.FORBIDDEN, message) {
    override val code: Int = 999
}
