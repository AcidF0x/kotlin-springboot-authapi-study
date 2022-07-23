package io.github.acidfox.kopringbootauthapi.infrastructure.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(exception: CustomException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(exception.httpStatus)
            .body(ErrorResponse(exception.code, exception.message))
    }
}
