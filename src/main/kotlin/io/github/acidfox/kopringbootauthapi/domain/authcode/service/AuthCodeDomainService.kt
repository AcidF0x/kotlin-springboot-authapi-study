package io.github.acidfox.kopringbootauthapi.domain.authcode.service

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.InvalidAuthCodeException
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.NotValidatedAuthCodeException
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
    @Value("\${config.auth.auth-code-validated-live-time-minute}")
    var authCodeValidatedLifeTime: Int = 30

    fun issue(phoneNumber: String, authCodeType: AuthCodeType): AuthCode {
        val now: LocalDateTime = LocalDateTime.now()

        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)
        val code = Random.nextInt(100000, 999999).toString()
        if (authCode === null) {
            return authCodeRepository.save(AuthCode(phoneNumber, authCodeType, code, 1, now))
        }

        if (now.toLocalDate().isEqual(authCode.requestedAt.toLocalDate())) {
            if (authCode.sendCount >= issueLimitPerDay && authCode.validatedAt === null) {
                throw TooManyAuthCodeRequestException("일일 요청 제한을 초과하였습니다")
            }
            authCode.sendCount = if (authCode.validatedAt === null) authCode.sendCount + 1 else 1
        } else {
            authCode.sendCount = 1
        }

        authCode.code = code
        authCode.requestedAt = LocalDateTime.now()
        authCode.validatedAt = null

        return authCodeRepository.save(authCode)
    }

    fun validate(phoneNumber: String, authCodeType: AuthCodeType, code: String): Boolean {
        val now: LocalDateTime = LocalDateTime.now()

        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)

        if (authCode === null) {
            throw InvalidAuthCodeException("먼저 인증코드를 발급 받아 주세요")
        }

        if (Duration.between(authCode.requestedAt, now).toMinutes() >= authCodeTTL) {
            throw InvalidAuthCodeException("유효하지 않은 인증 코드입니다")
        }

        if (authCode.code != code || authCode.validatedAt !== null) {
            throw InvalidAuthCodeException("유효하지 않은 인증 코드입니다")
        }

        authCode.validatedAt = LocalDateTime.now()
        authCodeRepository.save(authCode)

        return true
    }

    fun verifyValidation(phoneNumber: String, authCodeType: AuthCodeType): Boolean {
        val now: LocalDateTime = LocalDateTime.now()

        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)

        if (authCode === null || authCode.validatedAt === null) {
            throw NotValidatedAuthCodeException("먼저 휴대전화 번호를 인증 해주세요")
        }

        if (Duration.between(authCode.validatedAt, now).toMinutes() >= authCodeValidatedLifeTime) {
            throw NotValidatedAuthCodeException("휴대전화 번호 인증이 만료되었습니다. 다시 인증 해주세요")
        }

        return true
    }

    fun delete(phoneNumber: String, authCodeType: AuthCodeType) {
        val authCode = authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType)

        if (authCode !== null) {
            authCodeRepository.delete(authCode)
        }
    }
}
