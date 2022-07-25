package io.github.acidfox.kopringbootauthapi.application.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import java.time.LocalDateTime

data class UserInfoResponse(
    val email: String,

    val nickname: String,

    val name: String,

    @JsonProperty("phone_number")
    val phoneNumber: String,

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val createdAt: LocalDateTime,
) {
    constructor(user: User) : this(user.email, user.nickname, user.name, user.phoneNumber, user.createdAt)
}
