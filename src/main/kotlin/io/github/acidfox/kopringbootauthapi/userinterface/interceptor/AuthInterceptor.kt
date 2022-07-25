package io.github.acidfox.kopringbootauthapi.userinterface.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.acidfox.kopringbootauthapi.application.response.ErrorResponse
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
            val errorResponse = ErrorResponse(900, "로그인 상태에서는 요청 할 수 없습니다")
            val json = ObjectMapper().writeValueAsString(errorResponse)
            response.status = HttpStatus.BAD_REQUEST.value()
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(json)
            response.writer.flush()
            response.writer.close()

            return false
        }

        return true
    }
}
