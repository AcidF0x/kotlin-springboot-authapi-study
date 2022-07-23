package io.github.acidfox.kopringbootauthapi.infrastructure.error

import io.github.acidfox.kopringbootauthapi.application.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(exception: CustomException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(exception.httpStatus)
            .body(ErrorResponse(exception.code, exception.message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseErrorException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(-1, "invalid request"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrorException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(-1, ex.bindingResult.allErrors[0].defaultMessage ?: ""))
    }
}
