package io.github.acidfox.kopringbootauthapi.application.request

data class SignUpRequest(
    val email: String,
    val name: String,
    val nickname: String,
    val password: String,
    val phoneNumber: String,
)
