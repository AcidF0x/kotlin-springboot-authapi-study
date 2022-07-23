package io.github.acidfox.kopringbootauthapi.infrastructure.external.sms.driver

import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto
import java.io.File
import java.time.LocalDateTime

class FileDriver : SMSSendable {
    companion object {
        const val FILE_NAME = "sms_output.txt"
    }

    override fun sendMessage(message: SMSMessageDto): Boolean {
        val outputText = """
            ==============================================================
            Date: ${LocalDateTime.now()}
            TO : ${message.phoneNumber}
            Subject : ${message.subject}
            body: ${message.body}
            ==============================================================

        """.trimIndent()

        try {
            File(FILE_NAME).appendText(outputText)
            return true
        } catch (_: Throwable) {
            return false
        }
    }
}
