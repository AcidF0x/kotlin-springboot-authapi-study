package io.github.acidfox.kopringbootauthapi.domain.authcode.service

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.TooManyAuthCodeRequestException
import io.github.acidfox.kopringbootauthapi.domain.authcode.model.AuthCode
import io.github.acidfox.kopringbootauthapi.domain.authcode.repository.AuthCodeRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class AuthCodeDomainService(
    private val authCodeRepository: AuthCodeRepository
) {
    @Value("\${config.auth.auth-code-issue-limit-per-day}")
    var issueLimitPerDay: Int = 10
    @Value("\${config.auth.auth-code-time-to-live-minute}")
    var authCodeTTL: Int = 3

    fun issue(phoneNumber: String, authCodeType: AuthCodeType): AuthCode {
        val now: LocalDateTime = LocalDateTime.now()

        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)
        val code = Random.nextInt(100000, 999999).toString()
        if (authCode === null) {
            return authCodeRepository.save(AuthCode(phoneNumber, authCodeType, code, 1, now))
        }

        if (now.toLocalDate().isEqual(authCode.requestedAt.toLocalDate())) {
            if (authCode.sendCount >= issueLimitPerDay && authCode.verifiedAt === null) {
                throw TooManyAuthCodeRequestException()
            }
            authCode.sendCount = if (authCode.verifiedAt === null) authCode.sendCount + 1 else 1
        } else {
            authCode.sendCount = 1
        }

        authCode.requestedAt = LocalDateTime.now()
        authCode.verifiedAt = null

        return authCodeRepository.save(authCode)
    }

    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String): Boolean {
        val now: LocalDateTime = LocalDateTime.now()

        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)
        if (Duration.between(authCode!!.requestedAt, now).toMinutes() >= authCodeTTL) {
            return false
        }

        if (authCode.code !== code) {
            return false
        }

        authCode.verifiedAt = LocalDateTime.now()
        authCodeRepository.save(authCode)

        return true
    }
}
