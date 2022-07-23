package io.github.acidfox.kopringbootauthapi.domain.smsmessage.factory

import io.github.acidfox.kopringbootauthapi.domain.smsmessage.dto.SMSMessageDto
import io.github.acidfox.kopringbootauthapi.domain.smsmessage.enum.SMSMessageType
import org.springframework.stereotype.Service

@Service
class SMSMessageFactory {
    fun get(messageType: SMSMessageType, phoneNumber: String, payload: Map<String, String>): SMSMessageDto {
        // TODO : 템플릿 엔진이나 i8n 라이브러리 같은걸로 리펙토링 하자..

        val subjectAndBody = when (messageType) {
            SMSMessageType.SIGN_UP_REQUEST_AUTH_CODE -> Pair(
                "회원가입 인증코드",
                "회원 가입 인증 코드 [${payload["code"]}] ${payload["ttl"]}분 안에 입력하세요"
            )
            SMSMessageType.PASSWORD_RESET_REQUEST_AUTH_CODE -> Pair(
                "비밀번호 초기화 인증코드",
                "비밀번호 초기화 인증코드 [${payload["code"]}] ${payload["ttl"]}분 안에 입력하세요"
            )
        }

        return SMSMessageDto(phoneNumber, subjectAndBody.first, subjectAndBody.second)
    }
}
