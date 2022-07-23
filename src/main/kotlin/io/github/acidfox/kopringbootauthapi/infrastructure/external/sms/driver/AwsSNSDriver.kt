package io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver

import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto

class AwsSNSDriver : SMSSendable {
    override fun sendMessage(message: SMSMessageDto): Boolean {
        TODO("Not yet implemented")
    }
}
