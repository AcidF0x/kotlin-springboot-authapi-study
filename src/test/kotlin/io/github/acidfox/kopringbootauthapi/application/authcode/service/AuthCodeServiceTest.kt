package io.github.acidfox.kopringbootauthapi.application.authcode.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.service.AuthCodeDomainService
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class AuthCodeServiceTest(): BaseTestCase() {
    @RelaxedMockK
    lateinit var authCodeDomainService: AuthCodeDomainService
    @InjectMockKs
    lateinit var authCodeService: AuthCodeService

    @Test
    fun testIssue()
    {
        // Given
        val phoneNumber = "01012341234"
        val authCodeType = AuthCodeType.SIGN_UP

        // When
        authCodeService.issue(phoneNumber, authCodeType)

        // Then
        verify(exactly = 1) { authCodeDomainService.issue(phoneNumber, authCodeType) }
    }

    @Test
    fun testValidate()
    {
        // Given
        val phoneNumber = "01012341234"
        val authCodeType = AuthCodeType.SIGN_UP
        val code = "123123"
        // When
        authCodeService.validate(phoneNumber, authCodeType, code)

        // Then
        verify(exactly = 1) { authCodeDomainService.validate(phoneNumber, authCodeType, code) }
    }
}
