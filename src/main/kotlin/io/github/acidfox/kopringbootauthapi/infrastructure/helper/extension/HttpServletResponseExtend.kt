package io.github.acidfox.kopringbootauthapi.infrastructure.helper.extension

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.acidfox.kopringbootauthapi.application.response.ErrorResponse
import io.github.acidfox.kopringbootauthapi.infrastructure.error.CustomException
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletResponse

fun HttpServletResponse.send(response: ErrorResponse, statusCode: HttpStatus) {
    val json = ObjectMapper().writeValueAsString(response)
    this.status = statusCode.value()
    this.contentType = "application/json"
    this.characterEncoding = "UTF-8"
    this.writer.write(json)
}

fun HttpServletResponse.send(exception: CustomException) {
    val errorResponse = ErrorResponse(exception.code, exception.message)
    val json = ObjectMapper().writeValueAsString(errorResponse)
    this.status = exception.httpStatus.value()
    this.contentType = "application/json"
    this.characterEncoding = "UTF-8"
    this.writer.write(json)
}
