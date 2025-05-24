package com.example.sandbox.controller

import com.example.sandbox.exception.api.BadRequestException
import com.example.sandbox.exception.api.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(BadRequestException::class)
    fun handleException(e: BadRequestException): ResponseEntity<String> {
        logger.info { e }
        return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleException(e: NotFoundException): ResponseEntity<String> {
        logger.info { e }
        return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<String> {
        logger.error { e }
        return ResponseEntity("internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
