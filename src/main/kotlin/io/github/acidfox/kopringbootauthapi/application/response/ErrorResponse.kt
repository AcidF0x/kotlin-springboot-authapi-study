package io.github.acidfox.kopringbootauthapi.application.response

data class ErrorResponse(
    val code: Int,
    val message: String
) : Responsible
