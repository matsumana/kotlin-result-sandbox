package com.example.sandbox.exception

class NotFoundException(override val message: String) : RuntimeException(message)
