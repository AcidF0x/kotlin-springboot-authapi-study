package io.github.acidfox.kopringbootauthapi.infrastructure.error

import org.springframework.http.HttpStatus

abstract class CustomException(
    val httpStatus: HttpStatus,
    override val message: String
) : RuntimeException() {
    abstract val code: Int
}
