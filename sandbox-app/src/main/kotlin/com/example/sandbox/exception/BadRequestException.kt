package com.example.sandbox.exception

class BadRequestException(override val message: String) : RuntimeException(message)
