package com.example.sandbox.controller

import com.example.sandbox.valueobject.Position

data class UserGetResponse(
    val name: String,
    val position: Position
)
