package io.github.acidfox.kopringbootauthapi.infrastructure.configuration

import io.github.acidfox.kopringbootauthapi.application.service.TokenAuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
@Configuration
class SecurityConfiguration(
    private val tokenAuthenticationService: TokenAuthenticationService
) {

    @Bean
    fun filterChain(http: HttpSecurity): DefaultSecurityFilterChain {
        http
            .headers().frameOptions().disable()
            .and()
            .formLogin().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling().authenticationEntryPoint(tokenAuthenticationService)
            .and()
            .authorizeRequests()
            .mvcMatchers("/api/auth/**").permitAll()
            .antMatchers("/").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(tokenAuthenticationService, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
