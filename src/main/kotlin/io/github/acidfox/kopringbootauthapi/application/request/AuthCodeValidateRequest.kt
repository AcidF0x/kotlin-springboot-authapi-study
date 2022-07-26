package io.github.acidfox.kopringbootauthapi.application.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class AuthCodeValidateRequest(
    @field: Pattern(regexp = "^01([0|1|6|7|8|9])([0-9]{7,8})$", message = "휴대전화 번호를 확인해주세요")
    @JsonProperty("phone_number")
    val phoneNumber: String,

    @field: NotBlank(message = "코드를 입력해주세요")
    val code: String
)
