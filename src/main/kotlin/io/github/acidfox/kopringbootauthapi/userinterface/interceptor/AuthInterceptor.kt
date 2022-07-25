package io.github.acidfox.kopringbootauthapi.userinterface.interceptor

import io.github.acidfox.kopringbootauthapi.application.response.ErrorResponse
import io.github.acidfox.kopringbootauthapi.infrastructure.helper.extension.send
import io.github.acidfox.kopringbootauthapi.userinterface.aop.NotLoginOnly
import org.springframework.http.HttpStatus
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerMethod: HandlerMethod? = handler as? HandlerMethod

        if (handlerMethod === null) {
            return true
        }
        val isNotLoginMethod = handlerMethod.hasMethodAnnotation(NotLoginOnly::class.java)
        val hasAuthHeader = request.getHeader("Authorization") !== null
        if (isNotLoginMethod && hasAuthHeader) {
            response.send(ErrorResponse(900, "로그인 상태에서는 요청 할 수 없습니다"), HttpStatus.BAD_REQUEST)
            return false
        }

        return true
    }
}
