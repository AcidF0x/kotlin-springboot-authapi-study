package io.github.acidfox.kopringbootauthapi.application.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginRequest(
    @field: Email(message = "올바른 이메일이 아닙니다")
    @field: NotBlank(message = "이메일을 입력 해주세요")
    val email: String,

    @field: Size(min = 6, message = "비밀번호는 6자 이상 입력해주세요")
    val password: String,
)
