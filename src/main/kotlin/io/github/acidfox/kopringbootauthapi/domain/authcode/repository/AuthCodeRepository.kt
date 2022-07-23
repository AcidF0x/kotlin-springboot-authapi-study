package io.github.acidfox.kopringbootauthapi.domain.authcode.repository

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.model.AuthCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthCodeRepository : JpaRepository<AuthCode, Long> {
    fun findByPhoneNumberAndAuthCodeType(phoneNumber: String, authCodeType: AuthCodeType): AuthCode?
}
