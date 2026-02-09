package com.deeperdungeons.backend.configurations

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler {
    data class RestError(val message: String?, val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) {
        fun toResponseEntity() = ResponseEntity.status(status).body(this)
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(e: Throwable): ResponseEntity<RestError> {
        logger.error(e) { "An unexpected error occurred: ${e.message}" }
        return RestError(e.message)
            .toResponseEntity()
    }
}