package io.github.acidfox.kopringbootauthapi.domain.authcode.exception

import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus

class TooManyAuthCodeRequestException(message: String) : CustomException(HttpStatus.TOO_MANY_REQUESTS, message) {
    override val code: Int = 100
}
