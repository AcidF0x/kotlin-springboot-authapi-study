package io.github.acidfox.kopringbootauthapi.application.service

import io.github.acidfox.kopringbootauthapi.application.response.ErrorResponse
import io.github.acidfox.kopringbootauthapi.domain.jwt.service.JWTTokenService
import io.github.acidfox.kopringbootauthapi.domain.user.service.UserDomainService
import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import io.github.acidfox.kopringbootauthapi.infrastructure.helper.extension.send
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class TokenAuthenticationService(
    private val jwtTokenService: JWTTokenService,
    private val userDomainService: UserDomainService,
) : OncePerRequestFilter(), AuthenticationEntryPoint {

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val token = jwtTokenService.parseJWTTokenFromHeader(header)
        try {
            val claims = jwtTokenService.parseClaimsFromJWTToken(token)
            val user = userDomainService.findByEmail(claims.subject)
            if (user !== null) {
                jwtTokenService.validatePasswordChanged(claims, user.passwordChangedAt)
                SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    null
                )
            }
            filterChain.doFilter(request, response)
        } catch (e: CustomException) {
            response.send(e)
        }
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.send(ErrorResponse(400, "로그인이 필요합니다."), HttpStatus.UNAUTHORIZED)
    }
}
