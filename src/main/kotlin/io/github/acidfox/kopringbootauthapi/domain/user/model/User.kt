package io.github.acidfox.kopringbootauthapi.domain.user.model

import io.github.acidfox.kopringbootauthapi.domain.model.BaseEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var nickname: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var phoneNumber: String,

    var passwordChangedAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity(), UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? {
        return null
    }

    override fun getPassword(): String {
        return password
    }

    fun setPassword(password: String): User {
        this.password = password
        return this
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
