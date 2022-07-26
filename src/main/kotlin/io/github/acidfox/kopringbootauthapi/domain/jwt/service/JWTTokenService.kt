package io.github.acidfox.kopringbootauthapi.domain.jwt.service

import io.github.acidfox.kopringbootauthapi.domain.jwt.exception.ExpiredTokenException
import io.github.acidfox.kopringbootauthapi.domain.jwt.exception.InvalidTokenException
import io.jsonwebtoken.ExpiredJwtException
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

    fun createJWTToken(email: String, passwordChangedAt: LocalDateTime?): String {
        val now = LocalDateTime.now()
        val claims = Jwts.claims().setSubject(email)
        val id: String = if (passwordChangedAt === null) "" else Timestamp.valueOf(passwordChangedAt).toString()
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Timestamp.valueOf(now))
            .setExpiration(Timestamp.valueOf(now.plusSeconds(jwtTokenLifeTime.toLong())))
            .setId(id)
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact()
    }

    fun parseJWTTokenFromHeader(header: String): String {
        if (header.isNotBlank() && header.startsWith("Bearer ")) {
            return header.split(' ')[1]
        }

        return ""
    }

    fun parseEmailFromJWTToken(token: String): String {

        try {
            val claims = Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(token)
                .body

            return claims.subject
        } catch (_: ExpiredJwtException) {
            throw ExpiredTokenException("로그인이 만료되었습니다. 다시 로그인 해주세요")
        } catch (_: Throwable) {
            throw InvalidTokenException("유효하지 않은 로그인 토큰입니다. 다시 로그인 해주세요")
        }
    }
}
