package io.github.acidfox.kopringbootauthapi.application.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class SignUpRequest(
    @field: Email(message = "올바른 이메일이 아닙니다")
    val email: String,

    @field: NotBlank(message = "이름을 입력 해주세요")
    val name: String,

    @field: NotBlank(message = "닉네임을 입력 해주세요")
    val nickname: String,

    @field: NotBlank(message = "비밀번호를 입력 해주세요")
    @field: Size(min = 6, message = "비밀번호는 6자 이상 입력해주세요")
    val password: String,

    @field: Pattern(regexp = "^01([0|1|6|7|8|9])([0-9]{7,8})$", message = "휴대전화 번호를 확인해주세요")
    @JsonProperty("phone_number")
    val phoneNumber: String,
)
