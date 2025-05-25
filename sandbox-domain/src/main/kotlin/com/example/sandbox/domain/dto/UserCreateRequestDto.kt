package com.example.sandbox.domain.dto

data class UserCreateRequestDto(
    val name: String,
    val position: String,
    val mailAddress: String
)
