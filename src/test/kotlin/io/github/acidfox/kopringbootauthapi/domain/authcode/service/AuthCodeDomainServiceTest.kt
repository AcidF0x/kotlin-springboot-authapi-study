package io.github.acidfox.kopringbootauthapi.domain.authcode.service

import io.github.acidfox.kopringbootauthapi.BaseTestCase
import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.InvalidAuthCodeException
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.NotValidatedAuthCodeException
import io.github.acidfox.kopringbootauthapi.domain.authcode.exception.TooManyAuthCodeRequestException
import io.github.acidfox.kopringbootauthapi.domain.authcode.model.AuthCode
import io.github.acidfox.kopringbootauthapi.domain.authcode.repository.AuthCodeRepository
import io.mockk.Called
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AuthCodeDomainServiceTest() : BaseTestCase() {
    @MockK
    lateinit var authCodeRepository: AuthCodeRepository

    @InjectMockKs
    lateinit var authCodeDomainService: AuthCodeDomainService

    private val now: LocalDateTime = LocalDateTime.of(2022, 7, 23, 23, 30, 5)

    @BeforeEach
    fun setup() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns now
    }

    @Test
    @DisplayName("인증코드 발급 기록이 없는 경우 인증 코드를 발급 할 수 있다")
    fun testIssue() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val expectedAuthCode = AuthCode(phoneNumber, authCodeType, "123123", 1, now)

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns null
        every {
            authCodeRepository.save(
                match {
                    it.phoneNumber == phoneNumber &&
                        it.authCodeType === authCodeType &&
                        it.sendCount == 1 &&
                        it.requestedAt.isEqual(now)
                }
            )
        } returns expectedAuthCode

        // When
        val result = authCodeDomainService.issue(phoneNumber, authCodeType)

        // Then
        Assertions.assertSame(expectedAuthCode.phoneNumber, result.phoneNumber)
        Assertions.assertSame(expectedAuthCode.authCodeType, result.authCodeType)
        Assertions.assertSame(expectedAuthCode.code, result.code)
        Assertions.assertSame(expectedAuthCode.sendCount, result.sendCount)
        Assertions.assertTrue(now.isEqual(result.requestedAt))
        Assertions.assertNull(result.validatedAt)
    }

    @Test
    @DisplayName("인증코드 발급 기록이 있더라도 일일 발송 제한량을 넘지 않으면 발급 할 수있다 ")
    fun testIssueWhenAlreadyIssued() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val preSendCount = authCodeDomainService.issueLimitPerDay - 1
        val code = "123123"
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            code,
            preSendCount,
            now.minusMinutes(2)
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode
        every {
            authCodeRepository.save(
                match {
                    it.phoneNumber == phoneNumber &&
                        it.authCodeType === authCodeType &&
                        it.sendCount == preSendCount + 1 &&
                        it.requestedAt.isEqual(now)
                }
            )
        } returns authCode

        // When
        val result = authCodeDomainService.issue(phoneNumber, authCodeType)

        // Then
        Assertions.assertSame(phoneNumber, result.phoneNumber)
        Assertions.assertSame(authCodeType, result.authCodeType)
        Assertions.assertNotSame(code, result.code)
        Assertions.assertSame(preSendCount + 1, result.sendCount)
        Assertions.assertTrue(now.isEqual(result.requestedAt))
        Assertions.assertNull(result.validatedAt)
    }

    @Test
    @DisplayName("인증코드 일일 발송 제한량을 넘은 발급 기록이 있더라도 다음날이면 발급 할수있다")
    fun testIssueWhenAlreadyLimitedButNotSameDay() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val code = "222222"
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            code,
            authCodeDomainService.issueLimitPerDay,
            now.minusDays(1)
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode
        every {
            authCodeRepository.save(
                match {
                    it.phoneNumber == phoneNumber &&
                        it.authCodeType === authCodeType &&
                        it.sendCount == 1 &&
                        it.requestedAt.isEqual(now)
                }
            )
        } returns authCode

        // When
        val result = authCodeDomainService.issue(phoneNumber, authCodeType)

        // Then
        Assertions.assertSame(phoneNumber, result.phoneNumber)
        Assertions.assertSame(authCodeType, result.authCodeType)
        Assertions.assertNotSame(code, result.code)
        Assertions.assertSame(1, result.sendCount)
        Assertions.assertTrue(now.isEqual(result.requestedAt))
        Assertions.assertNull(result.validatedAt)
    }
    @Test
    @DisplayName("인증코드 일일 발송 제한량을 넘은 발급 기록이 있더라도 인증이 완료된 기록이면 발급 가능하다")
    fun testIssueWhenAlreadyLimitedButVerified() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val code = "444444"
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            code,
            authCodeDomainService.issueLimitPerDay,
            now.minusHours(1)
        )
        authCode.validatedAt = LocalDateTime.now()

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode
        every {
            authCodeRepository.save(
                match {
                    it.phoneNumber == phoneNumber &&
                        it.authCodeType === authCodeType &&
                        it.sendCount == 1 &&
                        it.requestedAt.isEqual(now) &&
                        it.validatedAt === null
                }
            )
        } returns authCode

        // When
        val result = authCodeDomainService.issue(phoneNumber, authCodeType)

        // Then
        Assertions.assertSame(phoneNumber, result.phoneNumber)
        Assertions.assertSame(authCodeType, result.authCodeType)
        Assertions.assertNotSame(code, result.code)
        Assertions.assertSame(1, result.sendCount)
        Assertions.assertTrue(now.isEqual(result.requestedAt))
        Assertions.assertNull(result.validatedAt)
    }

    @Test
    @DisplayName("인증코드 일일 발송 제한량을 넘은 경우 발급하지 않는다")
    fun testIssueWhenAlreadyLimited() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            "123123",
            authCodeDomainService.issueLimitPerDay,
            now
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            TooManyAuthCodeRequestException::class.java,
            {
                authCodeDomainService.issue(phoneNumber, authCodeType)
            },
            "일일 요청 제한을 초과하였습니다"
        )
        verify { authCodeRepository.save(authCode) wasNot Called }
    }

    @Test
    @DisplayName("인증 코드를 검증 - 발급 받은 코드와 일치하고 유효 기간 내의 경우 true를 리턴한다")
    fun testValidate() {
        // Given
        val expectedCode = "111222"
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            expectedCode,
            authCodeDomainService.issueLimitPerDay,
            now
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode
        every {
            authCodeRepository.save(
                match {
                    it.phoneNumber == phoneNumber &&
                        it.authCodeType === authCodeType &&
                        it.validatedAt!!.isEqual(now)
                }
            )
        } returns authCode

        // When
        val result = authCodeDomainService.validate(phoneNumber, authCodeType, expectedCode)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("인증 코드를 검증 - 인증코드를 발급 내역이 없는 경우 exception을 발생시킨다")
    fun testValidateWhenNotIssued() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns null

        // When && Then
        Assertions.assertThrows(
            InvalidAuthCodeException::class.java,
            {
                authCodeDomainService.validate(phoneNumber, authCodeType, "000000")
            },
            "먼저 인증코드를 발급 받아 주세요"
        )
        verify { authCodeRepository.save(allAny()) wasNot Called }
    }

    @Test
    @DisplayName("인증 코드를 검증 - 발급 받은 코드와 일치하지 않는 경우 exception을 발생시킨다")
    fun testValidateWhenInvalidCode() {
        // Given
        val expectedCode = "111222"
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            expectedCode,
            authCodeDomainService.issueLimitPerDay,
            now
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            InvalidAuthCodeException::class.java,
            {
                authCodeDomainService.validate(phoneNumber, authCodeType, "000000")
            },
            "유효하지 않은 인증 코드입니다"
        )
        verify { authCodeRepository.save(authCode) wasNot Called }
    }

    @Test
    @DisplayName("인증 코드를 검증 - 코드의 일치 여부와 상관 없이 유효 기간이 지난 경우 exception을 발생시킨다")
    fun testValidateWhenExpired() {
        // Given
        val expectedCode = "111222"
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            expectedCode,
            authCodeDomainService.issueLimitPerDay,
            now.minusMinutes(authCodeDomainService.authCodeTTL.toLong())
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            InvalidAuthCodeException::class.java,
            {
                authCodeDomainService.validate(phoneNumber, authCodeType, expectedCode)
            },
            "유효하지 않은 인증 코드입니다"
        )
        verify { authCodeRepository.save(authCode) wasNot Called }
    }

    @Test
    @DisplayName("인증 코드를 검증 - 이미 인증 받은 코드의 경우 Exception을 발생시킨다")
    fun testValidateWhenAlreadyValidated() {
        // Given
        val expectedCode = "111222"
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            expectedCode,
            authCodeDomainService.issueLimitPerDay,
            now.minusMinutes(authCodeDomainService.authCodeTTL.toLong())
        )

        authCode.validatedAt = LocalDateTime.now()

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            InvalidAuthCodeException::class.java,
            {
                authCodeDomainService.validate(phoneNumber, authCodeType, expectedCode)
            },
            "유효하지 않은 인증 코드입니다"
        )
        verify { authCodeRepository.save(authCode) wasNot Called }
    }

    @Test
    @DisplayName("인증 코드 검증 여부를 확인 할 수 있다")
    fun testVerifyValidation() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            "123",
            authCodeDomainService.issueLimitPerDay,
            now.minusMinutes(authCodeDomainService.authCodeTTL.toLong())
        )

        authCode.validatedAt = LocalDateTime.now()

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When
        val result = authCodeDomainService.verifyValidation(phoneNumber, authCodeType)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("인증 코드 검증이 안된 경우 Exception을 발생시킨다")
    fun testVerifyValidationExceptionWhenNotValidated() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            "123",
            authCodeDomainService.issueLimitPerDay,
            now.minusMinutes(authCodeDomainService.authCodeTTL.toLong())
        )

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            NotValidatedAuthCodeException::class.java,
            {
                authCodeDomainService.verifyValidation(phoneNumber, authCodeType)
            },
            "먼저 휴대전화 번호를 인증 해주세요"
        )
    }

    @Test
    @DisplayName("인증 코드를 발급 받지 않은 경우 Exception을 발생 시킨다")
    fun testVerifyValidationExceptionWhenNotIssued() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns null

        // When && Then
        Assertions.assertThrows(
            NotValidatedAuthCodeException::class.java,
            {
                authCodeDomainService.verifyValidation(phoneNumber, authCodeType)
            },
            "먼저 휴대전화 번호를 인증 해주세요"
        )
        verify { authCodeRepository.save(allAny()) wasNot called }
    }

    @Test
    @DisplayName("인증 코드 검증후 유효 시간이 지난 경우 Exception을 발생시킨다")
    fun testVerifyValidationExceptionWhenOverLifeTime() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD
        val authCode = AuthCode(
            phoneNumber,
            authCodeType,
            "123",
            authCodeDomainService.issueLimitPerDay,
            now.minusMinutes(authCodeDomainService.authCodeTTL.toLong())
        )
        authCode.validatedAt = LocalDateTime.now()
            .minusMinutes(authCodeDomainService.authCodeValidatedLifeTime.toLong())

        every { authCodeRepository.findByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } returns authCode

        // When && Then
        Assertions.assertThrows(
            NotValidatedAuthCodeException::class.java,
            {
                authCodeDomainService.verifyValidation(phoneNumber, authCodeType)
            },
            "휴대전화 번호 인증이 만료되었습니다. 다시 인증 해주세요"
        )
    }

    @Test
    @DisplayName("휴대전화 번호와 인증코드 타입이 같은 인증 코드 내역이 있으면 삭제한다")
    fun testDelete() {
        // Given
        val phoneNumber = "01011112222"
        val authCodeType = AuthCodeType.RESET_PASSWORD

        every { authCodeRepository.deleteByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) } just runs

        // When
        authCodeDomainService.delete(phoneNumber, authCodeType)

        // Then
        verify(exactly = 1) { authCodeRepository.deleteByPhoneNumberAndAuthCodeType(phoneNumber, authCodeType) }
    }
}
