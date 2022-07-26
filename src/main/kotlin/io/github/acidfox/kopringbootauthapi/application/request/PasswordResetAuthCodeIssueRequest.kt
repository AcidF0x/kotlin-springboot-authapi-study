package io.github.acidfox.kopringbootauthapi.application.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class PasswordResetAuthCodeIssueRequest(
    @field: Pattern(regexp = "^01([0|1|6|7|8|9])([0-9]{7,8})$", message = "휴대전화 번호를 확인해주세요")
    @JsonProperty("phone_number")
    val phoneNumber: String,

    @field: Email(message = "올바른 이메일이 아닙니다")
    @field: NotBlank(message = "이메일을 입력 해주세요")
    val email: String,
)
