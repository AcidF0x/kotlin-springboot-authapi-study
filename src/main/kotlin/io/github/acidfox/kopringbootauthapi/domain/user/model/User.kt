package io.github.acidfox.kopringbootauthapi.domain.user.model

import io.github.acidfox.kopringbootauthapi.domain.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var nickname: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var phoneNumber: String,
) : BaseEntity()
