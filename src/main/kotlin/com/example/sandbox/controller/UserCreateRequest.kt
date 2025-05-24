package com.example.sandbox.controller

import com.example.sandbox.valueobject.Position

data class UserCreateRequest(
    val name: String,
    val position: Position
)
