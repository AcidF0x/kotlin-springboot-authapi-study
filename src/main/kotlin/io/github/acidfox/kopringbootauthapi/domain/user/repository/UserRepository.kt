package io.github.acidfox.kopringbootauthapi.domain.user.repository

import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
