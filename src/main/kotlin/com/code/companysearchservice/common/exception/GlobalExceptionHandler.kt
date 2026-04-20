package com.code.companysearchservice.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @ExceptionHandler(Exception::class)
    fun handleDefaultExceptionArgument(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        return ResponseEntity.internalServerError().body(
            ErrorResponse(
                message = ex.message ?: "An unexpected error occurred",
                status = 500
            )
        )
    }

    @ExceptionHandler(HttpStatusException::class)
    fun handleHttpStatusException(ex: HttpStatusException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(ex.status).body(
            ErrorResponse(message = ex.message, timestamp = ex.timestamp, status = ex.status.value())
        )

}
