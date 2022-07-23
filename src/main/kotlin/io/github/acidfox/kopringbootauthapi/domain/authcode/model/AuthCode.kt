package io.github.acidfox.kopringbootauthapi.domain.authcode.model

import io.github.acidfox.kopringbootauthapi.domain.authcode.enum.AuthCodeType
import io.github.acidfox.kopringbootauthapi.domain.model.BaseEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class AuthCode(
    @Column(nullable = false, length = 11)
    var phoneNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var authCodeType: AuthCodeType,

    @Column(nullable = false, length = 6)
    var code: String,

    @Column(nullable = false)
    var sendCount: Int,

    @Column(nullable = false)
    var requestedAt: LocalDateTime
) : BaseEntity() {
    var verifiedAt: LocalDateTime? = null
}
