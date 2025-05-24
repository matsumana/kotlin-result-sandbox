package com.example.sandbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun exceptionHandler(e: Exception): ResponseEntity<String> {
        logger.error { e }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("internal server error")
    }
}
