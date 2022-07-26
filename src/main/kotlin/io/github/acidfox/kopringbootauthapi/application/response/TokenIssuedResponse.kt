package io.github.acidfox.kopringbootauthapi.application.response

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenIssuedResponse(
    @field: JsonProperty("expired_in")
    val expiredIn: Int
)
