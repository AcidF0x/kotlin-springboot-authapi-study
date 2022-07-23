package io.github.acidfox.kopringbootauthapi.infrastructure.external.sms

import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver.SMSSendable
import org.springframework.stereotype.Component

@Component
class SMSClient(private val driver: SMSSendable): SMSSendable by driver
