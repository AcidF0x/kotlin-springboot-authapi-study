package io.github.acidfox.kopringbootauthapi.domain.user.repository

import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmailEqualsOrPhoneNumberEquals(email: String, phoneNumber: String): Boolean
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByEmail(email: String): User?
}
