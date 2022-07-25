package io.github.acidfox.kopringbootauthapi.infrastructure.configuration

import io.github.acidfox.kopringbootauthapi.userinterface.interceptor.AuthInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class BeanConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }
}
