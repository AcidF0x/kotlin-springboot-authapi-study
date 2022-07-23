package io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto

data class SMSMessageDto(
    val phoneNumber: String,
    val subject: String,
    val body: String
)
