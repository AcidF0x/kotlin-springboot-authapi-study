package io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver

import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto

interface SMSSendable {
    fun sendMessage(message: SMSMessageDto): Boolean
}
