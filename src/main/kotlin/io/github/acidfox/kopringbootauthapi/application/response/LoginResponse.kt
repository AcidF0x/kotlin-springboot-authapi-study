package io.github.acidfox.kopringbootauthapi.application.response

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    val token: String
) : Responsible {
    @JsonProperty("token_type")
    val tokenType = "Bearer"
}
