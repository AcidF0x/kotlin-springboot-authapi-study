package io.github.acidfox.kopringbootauthapi.domain.jwt.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Base64
import javax.annotation.PostConstruct

@Service
class JWTTokenService(
    @Value("\${config.auth.jwt.token-secret-key}")
    var jwtSecretKey: String,
    @Value("\${config.auth.jwt.token-life-time-seconds}")
    var jwtTokenLifeTime: Int
) {

    @PostConstruct
    private fun encodeSecretKey() {
        jwtSecretKey = Base64.getEncoder().encodeToString(jwtSecretKey.toByteArray())
    }

    fun createJWTToken(email: String): String {
        val now = LocalDateTime.now()
        val claims = Jwts.claims().setSubject(email)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Timestamp.valueOf(now))
            .setExpiration(Timestamp.valueOf(now.plusSeconds(jwtTokenLifeTime.toLong())))
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact()
    }
}
