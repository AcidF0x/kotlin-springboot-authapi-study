package io.github.acidfox.kopringbootauthapi.application.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthCodeIssuedResponse(
    @field: JsonProperty("expired_in")
    val expiredIn: Int
)
