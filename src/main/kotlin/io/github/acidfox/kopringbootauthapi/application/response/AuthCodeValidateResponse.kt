package io.github.acidfox.kopringbootauthapi.application.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthCodeValidateResponse(
    @field: JsonProperty("expired_in")
    val expiredIn: Int
)
