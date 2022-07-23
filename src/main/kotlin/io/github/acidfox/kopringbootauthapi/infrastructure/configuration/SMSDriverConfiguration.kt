package io.github.acidfox.kopringbootauthapi.infrastructure.configuration

import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver.AwsSNSDriver
import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver.FileDriver
import io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver.SMSSendable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SMSDriverConfiguration {
    @Value("\${config.sms.driver}")
    private lateinit var driverConfig: String

    @Bean
    fun setDriver(): SMSSendable {
        return when (driverConfig) {
            "file" -> FileDriver()
            "AWS" -> AwsSNSDriver()
            else -> throw RuntimeException("Not Setting SMS Driver")
        }
    }
}
